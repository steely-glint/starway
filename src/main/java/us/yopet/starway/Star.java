/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 *
 * @author tim
 */
public class Star {

    private final int[] _leds;
    private final String _name;
    public Star(JsonObject config){
        JsonArray leds = config.getJsonArray("leds");
        int nleds = (leds == null) ? 0: leds.size();
        _leds = new int[nleds];
        for (int i=0;i< nleds; i++){
            _leds[i] = leds.getInt(i);
        }
        _name = config.getString("name");
    }
    
}
