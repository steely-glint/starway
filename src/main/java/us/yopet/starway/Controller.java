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
    HashMap<String, Star> _ids;

    Long _nextStar = new Long(0);

    protected final Star[] _stars;
    private final Sender _sender;
    private boolean _performing = false;
    protected ArrayList<Star> _onStars;
    protected final Config _conf;
    protected boolean _gala;

    Controller(String confFile) throws FileNotFoundException {
        _conf = new Config();
        Log.debug("Loading config");

        _conf.load(confFile);
        JsonArray jstars = _conf.getStars();

        _stars = new Star[jstars.size()];
        Log.debug("building " + _stars.length + " stars");

        _selected = new HashMap();
        _sequence = new HashMap();
        _ids = new HashMap();

        for (int i = 0; i < _stars.length; i++) {
            // special case for last one ?
            Star star = new Star(jstars.getJsonObject(i));
            _stars[i] = star;
            _sequence.put(star.getSeq(), star);
            if (star.getId() != null) {
                _ids.put(star.getId(), star);
            }

        }
        _onStars = new ArrayList();
        InetSocketAddress iad = _conf.getSenderAddress();
        int allLeds = _conf.getMaxLeds();
        _gala = _conf.isGalaMode();
        Log.debug("reserving space for " + allLeds + " Leds");
        _sender = new Sender(iad, allLeds);
    }

    abstract boolean hasSelectedCard();

    void napTime() throws InterruptedException {
        Thread.sleep(100);
    }

    public void run() {
        for (Star s : _stars) {
            if (s.getName().equals("F1")) {
                s.setColour(4, 64, 4); // center star is bright green .
            } else {
                s.setColour(8, 8, 32);
            }
        }
        try {
            while (true) {
                try {
                    _performing = hasSelectedCard();
                    if (!_performing) {
                        for (Star s : _stars) {
                            s.twinkle();
                        }
                        _sender.send(_stars);

                    } else {
                        Star s[] = {};
                        _sender.send(_onStars.toArray(s));
                    }
                    napTime();
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
        // in 'gala' mode we _strictly_ do rfids that are in the file
        if (_gala) {
            ret = _ids.get(rfid);
        } else {
            // see if we have already allocated it
            ret = _selected.get(rfid);
            // now see if we have an id match.
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
        }
        return ret;
    }
}
