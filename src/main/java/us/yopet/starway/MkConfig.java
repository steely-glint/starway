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
* config for CCC matelight
     *
     */
    public static void main(String args[]) throws IOException {

        // assuming central star is E39
        char diamonds[] = new char[16];
        int leglengths[] = new int[16];
        for(int i=0;i<16;i++){
            diamonds[i]=(char)('A'+i);
            leglengths[i] =40;
        }
        int leds_per_star = 1;

        String feeders[] = {
        };

        Log.setLevel(Log.VERB);
        Writer fWriter = null;
        if (args.length > 0) {
            fWriter = new FileWriter(args[0]);
        } else {
            fWriter = new java.io.OutputStreamWriter(System.out);
        }
        JsonObjectBuilder conf = Json.createObjectBuilder();
        conf.add("boneAddress", "matelight.visitor.congress.ccc.de");
        conf.add("bonePort", 1377);
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
