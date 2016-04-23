/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import static java.util.Locale.filter;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thp
 */
public class RFID extends Thread {

    private DataInputStream _rfidtty;
    private final HashMap<String,Long> _seen;
    static long EXPIRETIME = 1000;

    RFID(String arduino) throws FileNotFoundException {
        File tty = new File(arduino);
        if (!tty.exists() || !tty.canRead()) {
            throw new UnsupportedOperationException("can't open/read " + arduino);
        }
        _rfidtty = new DataInputStream(new FileInputStream(tty));

        this.setDaemon(true);
        this.setName("RFID-reader");
        this.start();
        _seen = new HashMap();
    }

    public void run() {
        try {
            Log.debug("RFID starting ");

            while (true) {
                String line = _rfidtty.readLine();
                Log.verb("Rfid >" + line);
                if (line.startsWith("Seems to be a Mifare Classic card")) {
                    Log.verb("Mifare seen");
                    String[] bits = line.split("#");
                    if (bits.length > 1) {
                        Log.debug("card serial " + bits[1]);
                        Long now = new Long(System.currentTimeMillis());
                        synchronized (_seen){
                            _seen.put(bits[1], now);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Log.debug("RFID problem " + ex.toString());
        }
    }
    
    public String[] currentCards(){
        String[] ret = {};
        ArrayList <String> deletes = new ArrayList();
        ArrayList <String> cards = new ArrayList();
        synchronized (_seen){
            long then = System.currentTimeMillis() - EXPIRETIME;
            for(String rfid : _seen.keySet()){
                Long when = _seen.get(rfid);
                if (when < then){
                    deletes.add(rfid);
                } else {
                    cards.add(rfid);
                }
            }
            for (String k:deletes){
                _seen.remove(k);
            }
            ret = cards.toArray(ret);
        }
        return ret;
    }
}
