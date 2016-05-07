/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author tim
 */
public class MkConfig {

    final static int Y = 1;
    final static int B = 2;
    final static int R = 3;
    final static int Q = 0;

    /**
     *
     * Background : This generates config files for the full Starway as at Reno
     * '16 It consists of 5 diamonds joined in the shape of a star. (A->E) Each
     * diamond nominally has 48 stars. However the first and last diamonds are
     * 'legs' and are shortened to give the illusion that Starway is partially
     * buried. Stars are named by the diamond and their number. Each Star
     * contains 3 RGB leds. A star may be one of 3 sizes (denoted by Y,B,R to
     * correspond to the colouring on the diagrams.)
     *
     * Each diamond is driven from a separate dataline on a beaglebone running
     * LEDscape - Ledscape's datamodel assumes all the lines are driving the
     * same number of Leds. In practice this means that despite the fact that
     * Diamond A actually contains 114 Leds Diamond B must start with led number
     * 144. (the remaining 30 leds are 'buried')
     *
     * This config generates a random 'sequence' for the Stars. It can be hand
     * tweaked after the fact.
     *
     */
    public static void main(String args[]) throws IOException {
        String[] buriedStars = {"E9", "E10", "E11", "E12", "E13", "E14", "E15", "E47", "E48",
            "A10", "A11", "A12", "A13", "A14", "A15", "A44", "A46", "A47", "A48"
        };
        int starSizes[] = {
            Y, R, Y, Y, B, Y, R, Y, B, R,
            Y, Y, R, B, Y, R, B, Y, R, B,
            Y, R, Y, B, Y, B, R, R, Y, R,
            Y, R, Y, Y, R, R, B, B, B, B,
            Y, R, Y, B, B, R, Y, B};
        char diamonds[] = {'A', 'B', 'C', 'D', 'E'};

        Log.setLevel(Log.VERB);
        Writer fWriter = null;
        if (args.length > 0) {
            fWriter = new FileWriter(args[0]);
        } else {
            fWriter = new java.io.OutputStreamWriter(System.out);
        }
        JsonObjectBuilder conf = Json.createObjectBuilder();
        conf.add("boneAddress", "10.0.1.4");
        conf.add("bonePort", 7890);
        conf.add("RFID", "/dev/ttyACM0");

        Random rand = new Random();

        int diamond_count = diamonds.length;
        int stars_per_diamond = 48;
        int leds_per_star = 3;
        int leds_per_diamond = (stars_per_diamond * leds_per_star);
        int total_stars = diamond_count * stars_per_diamond - buriedStars.length;
        ArrayList<Integer> seqList = new ArrayList();
        for (int i = 0; i < total_stars; i++) {
            seqList.add(new Integer(i));
        }
        conf.add("maxLeds", (diamond_count +1) * stars_per_diamond * leds_per_star);
        HashMap<String, Boolean> missing = new HashMap();
        for (String m : buriedStars) {
            missing.put(m, Boolean.TRUE);
        }
        JsonArrayBuilder stars = Json.createArrayBuilder();
        int ledno = 0;
        int ledStart = 0;
        for (char d : diamonds) {
            ledno = ledStart;
            for (int star = 1; star <= stars_per_diamond; star++) {
                String name = "" + d + star;

                if (missing.get(name) != null) {
                    Log.debug("Skipping " + name);
                } else {
                    int r = rand.nextInt(seqList.size());
                    Integer seq = seqList.remove(r);
                    JsonArrayBuilder leds = Json.createArrayBuilder();
                    for (int l = 0; l < 3; l++) {
                        leds.add(ledno++);
                    }
                    stars.add(
                            Json.createObjectBuilder()
                            .add("name", name)
                            .add("leds", leds)
                            .add("seq", seq.intValue())
                            .add("size", starSizes[star - 1])
                    );
                }
            }
            ledStart += leds_per_diamond;
        }
        // add center 'star' - a neopixel ring.
        JsonArrayBuilder leds = Json.createArrayBuilder();
        ledno = ledStart;
        for (int l = 0; l < 24; l++) {
            leds.add(ledno++);
        }
        stars.add(
                Json.createObjectBuilder()
                .add("name", "F1")
                .add("leds", leds)
                .add("seq", -1)
                .add("size", 24)
        );
        // now add the whole thing.
        conf.add("stars", stars);
        JsonObject jo = conf.build();
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(fWriter);

        jsonWriter.writeObject(jo);
        jsonWriter.close();

    }
}
