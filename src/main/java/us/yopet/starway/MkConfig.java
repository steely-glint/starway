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

    /**
     *
     * Background : This generates config files for the full Starway as at Reno
     * Nov '16 in the sculpture park. It consists of 5 diamonds joined in the
     * shape of a star. (A->E) Each diamond nominally has 51 stars. However the
     * first and last diamonds are 'legs' and are shortened to give the illusion
     * that Starway is partially buried. Stars are named by the diamond and
     * their number. Each Star contains 3 RGB leds. Some of the stars on each
     * leg are present but invisible (feeder boards) The leds for these boards
     * should be skipped and left unlit at all times.
     *
     * Each diamond is driven from a separate dataline on a beaglebone running
     * LEDscape - Ledscape's datamodel assumes all the lines are driving the
     * same number of Leds. In practice this means that despite the fact that
     * Diamond A actually contains 129 Leds Diamond B must start with led number
     * 153. (the remaining 30 leds are 'buried')
     *
     * This config generates a random 'sequence' for the Stars. It can be hand
     * tweaked after the fact.
     *
     */
    public static void main(String args[]) throws IOException {

        // assuming central star is E39
        char diamonds[] = {'A', 'B', 'C', 'D', 'E'};
        int leglengths[] = {43, 51, 51, 51, 39};
        int leds_per_star = 3;

        String feeders[] = {"A10", "A21", "A31", "A37",
            "B10", "B25", "B38",
            "C10", "C25", "C38",
            "D10", "D25", "D38",
            "E11", "E21", "E31"
        };

        Log.setLevel(Log.VERB);
        Writer fWriter = null;
        if (args.length > 0) {
            fWriter = new FileWriter(args[0]);
        } else {
            fWriter = new java.io.OutputStreamWriter(System.out);
        }
        JsonObjectBuilder conf = Json.createObjectBuilder();
        conf.add("boneAddress", "127.0.0.1");
        conf.add("bonePort", 7890);
        conf.add("RFID", "fakerfid");

        Random rand = new Random();

        int diamond_count = diamonds.length;
        int total_stars = 0;
        int longestleg = 0;

        for (int l : leglengths) {
            total_stars += l;
            if (l > longestleg) {
                longestleg = l;
            }
        }
        total_stars -= feeders.length;
        ArrayList<Integer> seqList = new ArrayList();
        for (int i = 0; i < total_stars; i++) {
            seqList.add(new Integer(i));
        }
        // this is the _capacity_ of the message sent - so includes all 
        // the buried stars and feeder boards.
        conf.add("maxLeds", (diamond_count) * longestleg * leds_per_star);
        HashMap<String, Boolean> skip = new HashMap();
        for (String m : feeders) {
            skip.put(m, Boolean.TRUE);
        }
        JsonArrayBuilder stars = Json.createArrayBuilder();
        int ledno = 0;
        int ledStart = 0;
        int seq = 0;
        for (int diam=0;diam<diamonds.length; diam++){
            char d = diamonds[diam];
            int starcount = leglengths[diam];
            ledno = ledStart;
            for (int star = 1; star <= starcount; star++) {
                String name = "" + d + star;

                if (skip.get(name) != null) {
                    Log.debug("Skipping feeder" + name);
                    ledno += leds_per_star;
                } else {
                    int r = rand.nextInt(seqList.size());
                    seq = seqList.remove(r);
                    JsonArrayBuilder leds = Json.createArrayBuilder();
                    for (int l = 0; l < leds_per_star; l++) {
                        leds.add(ledno++);
                    }
                    stars.add(
                            Json.createObjectBuilder()
                            .add("name", name)
                            .add("leds", leds)
                            .add("seq", seq++)
                            .add("size", "U")
                    );
                }
            }
            ledStart += (longestleg*leds_per_star);
        }
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
