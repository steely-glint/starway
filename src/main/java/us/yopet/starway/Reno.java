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
public class Reno extends Thread {

    private static Reno _reno;
    static String filename = "starway.json";

    HashMap<Long, Star> _sequence;
    HashMap<String, Star> _selected;
    Long _nextStar = new Long(0);

    public static void main(String args[]) {
        Log.setLevel(Log.DEBUG);
        if (args.length == 1) {
            filename = args[0];
        }
        Reno ren;
        try {
            ren = starFactory();
        } catch (FileNotFoundException ex) {
            System.err.println("Config File " + filename + " not found - giving up");
        }
    }
    public static Reno starFactory() throws FileNotFoundException{
        if (_reno == null){
            _reno  = new Reno(filename);
            _reno.setName("Reno-main");
            Log.debug("starting message thread");
            _reno.start();
        }
        return _reno;
    }
    
    private final Star[] _stars;
    private final Sender _sender;
    private final RFID _rfid;
    private ArrayList<Star> _onStars;

    Reno(String confFile) throws FileNotFoundException {
        Config conf = new Config();
        Log.debug("Loading config");

        conf.load(confFile);
        JsonArray jstars = conf.getStars();

        _stars = new Star[jstars.size()];
        Log.debug("building" + _stars.length + " stars");

        _selected = new HashMap();
        _sequence = new HashMap();

        for (int i = 0; i < _stars.length; i++) {
            _stars[i] = new Star(jstars.getJsonObject(i));
            _sequence.put(_stars[i].getSeq(), _stars[i]);
        }
        _onStars = new ArrayList();
        InetSocketAddress iad = conf.getSenderAddress();
        int allLeds = conf.getMaxLeds();
        Log.debug("reserving space for" + allLeds + " Leds");
        _sender = new Sender(iad, allLeds);
        String arduino = conf.getRFID();
        _rfid = new RFID(arduino) {
            @Override
            void cardDeleteEvent(String rfid) {
                Star star = pickStar(rfid);
                star.setColour(128, 128, 212);
                _onStars.remove(star);
            }

            @Override
            void cardAddEvent(String rfid) {
                Star star = pickStar(rfid);
                star.setColour(255, 64, 64);
                _onStars.add(star);
            }
        };
    }
    private double _power =0.0;
    public double getPower(){
        return _power;
    }
    public void clear(){
        _selected.clear();
    }
    public void run() {
        for (Star s : _stars) {
            s.setColour(8, 8, 32);
        }
        try {
            while (true) {
                try {
                    Thread.sleep(100);
                    String cards[] = _rfid.currentCards();
                    if (cards.length == 0) {
                        for (Star s : _stars) {
                            s.twinkle();
                        }
                        _power = _sender.send(_stars);

                    } else {
                        Star s[] = {};
                        _power = _sender.send(_onStars.toArray(s));
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
                    System.err.println("Sequence number " + _nextStar);
                    _nextStar = new Long(_nextStar.longValue() + 1);
                }
            }
            // last gasp - just pick a random (ish) one
            if (ret == null) {
                long l = Long.parseLong(rfid);
                int sno = (int) (l % _stars.length);
                ret = _stars[sno];
            }
            _selected.put(rfid, ret);
        }
        return ret;
    }
}
