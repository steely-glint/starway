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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

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
        final Sender localsender;
        final Star[] localStars;
        if (!arduino.contains("fake")) {
            InetSocketAddress lad = _conf.getLocalSenderAddress();
            int ringsz = 24;
            Log.debug("reserving space for " + ringsz + " Leds");
            localsender = new Sender(lad, 24);
            localStars = new Star[1];
            JsonArrayBuilder leds = Json.createArrayBuilder();
            for (int l = 0; l < ringsz; l++) {
                leds.add(l);
            }
            localStars[0] = new Star(Json.createObjectBuilder()
                    .add("name", "CONSOLE")
                    .add("leds", leds)
                    .add("seq", 1)
                    .add("size", ringsz).build());
            localStars[0].setColour(255, 225, 255);
        } else {
            localsender = null;
            localStars = null;
        }
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
                Log.debug("Star " + star.getName());
                _onStars.add(star);
                try {
                    if ((localsender != null) && (localStars != null)) {
                        localsender.send(localStars);
                    }
                } catch (IOException ex) {
                    Log.error("ioexception in local send");
                }

            }
        };

    }

    @Override
    boolean hasSelectedCard() {
        return _rfid.currentCards().length > 0;
    }
}
