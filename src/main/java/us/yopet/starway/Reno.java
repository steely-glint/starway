/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.FileNotFoundException;

/**
 *
 * @author thp
 */
public class Reno extends Controller {

    public static void main(String args[]) {
        Log.setLevel(Log.DEBUG);
        String filename = "starway.json";
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

    private final RFID _rfid;

    Reno(String confFile) throws FileNotFoundException {
        super(confFile);
        String arduino = _conf.getRFID();

        _rfid = new RFID(arduino) {
            @Override
            void cardDeleteEvent(String rfid) {
                Star star = pickStar(rfid);
                star.setColour(ColourMap.BRIGHT);
                _onStars.remove(star);
            }

            @Override
            void cardAddEvent(String rfid) {
                if (!_performing) {
                    Star star = pickStar(rfid);
                    Log.debug("Star " + star.getName());
                    _onStars.add(star);
                } else {
                    Log.debug("Already performing - so ignoring id " + rfid);
                }
            }
        };

    }

    @Override
    boolean hasSelectedCard() {
        return _rfid.currentCards().length > 0;
    }
}
