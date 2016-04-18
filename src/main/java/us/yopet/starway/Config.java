/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author tim
 */
public class Config {

    JsonObject _config;

    public void load() throws FileNotFoundException {
        FileInputStream bin = new java.io.FileInputStream("starway.json");
        JsonReader reader = Json.createReader(bin);
        _config = (JsonObject) reader.read();
    }
    JsonArray getStars(){
        return _config.getJsonArray("stars");
    }
}
