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

    private HashMap<Long, Star> _sequence;
    private HashMap<String, Star> _selected;
    private Long _nextStar = 0L;

    protected final Star[] _stars;
    private final Sender _sender;
    boolean _performing = false;
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

    void resetSequence() {
        _nextStar = 0L;
        _selected.clear();
        Log.debug("reset sequence");
        setForTwinkle();
    }

    void doFade(int[] c) {
        if (c[0] > 0 && c[2] == 0) {
            c[0]--;
            c[1]++;
        }
        if (c[1] > 0 && c[0] == 0) {
            c[1]--;
            c[2]++;
        }
        if (c[2] > 0 && c[1] == 0) {
            c[0]++;
            c[2]--;
        }
    }

    void setForTwinkle() {
        for (Star s : _stars) {
            s.setColour(ColourMap.PALE);
        }
        Log.debug("ReSet " + _selected.size() + " Selected stars to bright");
        _selected.forEach((String n, Star ss) -> {
            ss.setColour(ColourMap.BRIGHT);
        });
        _onStars.clear();
        if (_nextStar >= _sequence.size()) {
            resetSequence();
        }
    }

    public void run() {
        resetSequence();
        try {
            int perfcount = 0;
            int[] pir = {255, 0, 0};

            while (true) {
                try {
                    Thread.sleep(100);
                    _performing = hasSelectedCard();
                    if (!_performing) {
                        if (perfcount != -1) {
                            perfcount = -1;
                            setForTwinkle();
                        }
                        for (Star s : _stars) {
                            s.twinkle();
                        }
                    } else {
                        if (perfcount == -1) {
                            perfcount = 20;
                            Log.verb("perfcount = " + perfcount);
                        }
                        if (perfcount > 0) {
                            Log.verb("perfcount = " + perfcount);
                            for (Star fade : _stars) {
                                doFade(pir);
                                fade.setColour(pir);
                            }
                            perfcount--;
                        } else {
                            Log.verb("perfcount still zero - selexted star count is " + _onStars.size());
                            for (Star s : _stars) {
                                s.setColour(ColourMap.OFF);
                            }
                            for (Star s : _onStars) {
                                s.setColour(ColourMap.WISH);
                            }
                        }
                    }
                    _sender.send(_stars);

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
        Log.debug("in PickStar looking for " + rfid);
        // see if we have already allocated it
        ret = _selected.get(rfid);
        Log.debug("star from selected was " + ((ret == null) ? " null " : ret.getName()));
        if (ret == null) {
            // take the next one from the sequence
            ret = _sequence.get(_nextStar);
            Log.debug("star from sequence was " + ((ret == null) ? " null " : ret.getName()));
            if (ret != null) {
                Log.debug("Sequence number " + _nextStar);
                _nextStar++;
            } else {
                Log.debug("because nextStar is :" + _nextStar + " Sequence size is " + _sequence.size());
                _sequence.keySet().stream().forEach((Long a) -> {
                    Log.debug("->" + a);
                });
            }
            // either way remember what we did.
            _selected.put(rfid, ret);
        }
        return ret;
    }
}
