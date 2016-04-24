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
import javax.json.JsonArray;

/**
 *
 * @author thp
 */
public class Reno extends Thread {

    public static void main(String args[]) {
        Log.setLevel(Log.DEBUG);
        String filename = "starway.json:";
        if (args.length == 1) {
            filename = args[0];
        }
        Reno ren;
        try {
            ren = new Reno(filename);
            ren.setName("Reno-main");
            Log.debug("starting message thread");
            ren.start();
        } catch (FileNotFoundException ex) {
            System.err.println("Config File " + filename + " not found - giving up");
        }
    }
    private final Star[] _stars;
    private final Sender _sender;
    private boolean _performing = false;
    private final RFID _rfid;
    private ArrayList<Star> _onStars;

    Reno(String confFile) throws FileNotFoundException {
        Config conf = new Config();
        Log.debug("Loading config");

        conf.load(confFile);
        JsonArray jstars = conf.getStars();

        _stars = new Star[jstars.size()];
        Log.debug("building" + _stars.length + " stars");

        for (int i = 0; i < _stars.length; i++) {
            _stars[i] = new Star(jstars.getJsonObject(i));
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
                long l = Long.parseLong(rfid);
                int sno = (int) (l % _stars.length);
                Log.debug("star no " + sno);
                _stars[sno].setColour(128, 128, 212);
                _onStars.remove(_stars[sno]);
            }

            @Override
            void cardAddEvent(String rfid) {
                long l = Long.parseLong(rfid);
                int sno = (int) (l % _stars.length);
                Log.debug("star no " + sno);
                _stars[sno].setColour(255, 64, 64);
                _onStars.add(_stars[sno]);
            }
        };
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
                        _sender.send(_stars);

                    } else {
                        Star s[] = {};
                        _sender.send(_onStars.toArray(s));
                    }
                } catch (InterruptedException ex) {
                    ;// who cares...
                }
            }
        } catch (IOException ex) {
            System.err.println("Quitting due to io exception " + ex.toString());
        }
    }
}
