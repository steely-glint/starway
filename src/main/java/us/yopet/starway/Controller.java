/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import javax.json.JsonArray;

/**
 *
 * @author thp
 */
abstract public class Controller extends Thread {

    HashMap<Long, Star> _sequence;
    HashMap<String, Star> _selected;
    Long _nextStar = new Long(0);

    protected final Star[] _stars;
    private final Sender _sender;
    private boolean _performing = false;
    protected ArrayList<Star> _onStars;
    protected final Config _conf;
    private Star[] _cache;

    Controller(String confFile) throws FileNotFoundException {
        _conf = new Config();
        Log.debug("Loading config");

        _conf.load(confFile);
        JsonArray jstars = _conf.getStars();

        _stars = new Star[jstars.size()];
        Log.debug("building " + _stars.length + " stars");

        _selected = new HashMap();
        _sequence = new HashMap();

        for (int i = 0; i < _stars.length; i++) {
            // special case for last one ?
            _stars[i] = new Star(jstars.getJsonObject(i));
            _sequence.put(_stars[i].getSeq(), _stars[i]);
        }
        _onStars = new ArrayList();
        InetSocketAddress iad = _conf.getSenderAddress();
        int allLeds = _conf.getMaxLeds();
        Log.debug("reserving space for " + allLeds + " Leds");
        _sender = new Sender(iad, allLeds);
    }

    abstract boolean hasSelectedCard();

    public void run() {
        for (Star s : _stars) {
            s.setColour(8, 8, 32);
        }
        try {
            int perfcount = 0;
            int r = 255, g = 0, b = 0;

            while (true) {
                try {
                    Thread.sleep(100);
                    _performing = hasSelectedCard();
                    if (!_performing) {
                        perfcount = -1;
                        for (Star s : _stars) {
                            s.twinkle();
                        }
                        _sender.send(_stars);
                    } else {
                        Star s[] = {};
                        if (perfcount == -1) {
                            perfcount = 100;
                            if (_cache == null){
                                _cache = new Star[_stars.length];
                                for(int i=0;i<_cache.length;i++){
                                    _cache[i]= new Star(_stars[i]);
                                }
                            }
                            for (int i=0;i<_stars.length;i++){
                                Star src = _stars[i];
                                Star dst = _cache[i];
                                src.cloneColour(dst);
                            }
                        }
                        if (perfcount > 0) {
                            s = _stars;
                            for (Star fade:s) {
                                if (r > 0 && b == 0) {
                                    r--;
                                    g++;
                                }
                                if (g > 0 && r == 0) {
                                    g--;
                                    b++;
                                }
                                if (b > 0 && g == 0) {
                                    r++;
                                    b--;
                                }
                                fade.setColour(r, g, b);
                            }
                            perfcount--;              
                            if (perfcount==0){
                                for (int i=0;i<_stars.length;i++){
                                    Star dst = _stars[i];
                                    Star src = _cache[i];
                                    src.cloneColour(dst);
                                } 
                            }
                        } else { 
                            _onStars.toArray(s);
                            for (Star p:s){
                                p.setColour(255, 64, 64);
                            }
                        }
                        _sender.send(s);
                    }
                } catch (InterruptedException ex) {
                    ;// who cares...
                }
            }
        } catch (IOException ex) {
            System.err.println("Quitting due to io exception " + ex.toString());
        }
    }

    Star pickStar(String rfid) {
        Star ret = null;
        // see if we have already allocated it
        ret = _selected.get(rfid);
        if (ret == null) {
            // take the next one from the sequence
            if (_sequence.size() > 0) {
                ret = _sequence.remove(_nextStar);
                if (ret != null) {
                    Log.debug("Sequence number " + _nextStar);
                    _nextStar = new Long(_nextStar.longValue() + 1);
                }
            }
            // last gasp - just pick a random (ish) one
            if (ret == null) {
                int l = rfid.hashCode();
                int sno = l % (_stars.length - 1); // last star is special.
                ret = _stars[sno];
            }
            // either way remember what we did.
            _selected.put(rfid, ret);
        }
        return ret;
    }
}
