/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
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

    public void load(String filename) throws FileNotFoundException {
        FileInputStream bin = new java.io.FileInputStream(filename);
        JsonReader reader = Json.createReader(bin);
        Log.debug("Loading config from " + filename);

        _config = (JsonObject) reader.read();
        Log.debug("config is " + _config.toString());

    }

    JsonArray getStars() {
        return _config.getJsonArray("stars");
    }

    InetSocketAddress getSenderAddress() {
        String ip = _config.getString("boneAddress", "127.0.0.1");
        int portI = _config.getInt("bonePort", 7890);
        InetSocketAddress ret = new InetSocketAddress(ip, portI);
        return ret;
    }
    
    InetSocketAddress getLocalSenderAddress() {
        String ip = "127.0.0.1";
        int portI = _config.getInt("bonePort", 7890);
        InetSocketAddress ret = new InetSocketAddress(ip, portI);
        return ret;
    }

    int getMaxLeds() {
        int ret = _config.getInt("maxLeds", 24);
        return ret;
    }

    String getRFID() {
       return _config.getString("RFID", "/dev/tty");
    }
}
