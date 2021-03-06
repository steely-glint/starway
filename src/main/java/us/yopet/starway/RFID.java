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
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author thp
 */
abstract public class RFID extends Thread {

    private DataInputStream _rfidtty;
    private final HashMap<String, Long> _seen;
    static long EXPIRETIME = 1000;
    private Timer _tick;

    RFID(String arduino) throws FileNotFoundException {
        File tty = new File(arduino);
        if (!tty.exists() || !tty.canRead()) {
            Log.error("can't open/read " + arduino);
            throw new UnsupportedOperationException("can't open/read " + arduino);
        }
        _rfidtty = new DataInputStream(new FileInputStream(tty));

        this.setDaemon(true);
        this.setName("RFID-reader");
        this.start();
        _tick = new Timer();
        _seen = new HashMap();
    }

    abstract void cardDeleteEvent(String rfid) ;
    abstract void cardAddEvent(String rfid) ;

    private void maybeRemove(String rfid) {
        boolean fire = false;
        Log.verb("checking " + rfid);
        synchronized (_seen) {
            long then = System.currentTimeMillis() - EXPIRETIME;
            Long when = _seen.get(rfid);
            if (when < then) {
                Long rem = _seen.remove(rfid);
                Log.verb("removing " + rfid);
                fire = rem != null;
            }
        }
        if (fire) {
            cardDeleteEvent(rfid);
        }
    }

    public void run() {
        try {
            Log.debug("RFID starting ");

            while (true) {
                String line = _rfidtty.readLine();
                Log.verb("Rfid >" + line);
                if ((line !=null) && (line.contains("UID Value:"))) {
                    Log.debug("RFID seen");
                    String[] bits = line.split(":");
                    if (bits.length > 1) {
                        final String cardSerial = bits[1].trim().toLowerCase();
                        Log.debug("card serial :" + cardSerial);
                        Long now = new Long(System.currentTimeMillis());
                        Long previous = null;
                        synchronized (_seen) {
                            previous = _seen.put(cardSerial, now);
                        }
                        TimerTask tt = new TimerTask() {
                            @Override
                            public void run() {
                                maybeRemove(cardSerial);
                            }
                        };
                        _tick.schedule(tt, EXPIRETIME + 10);
                        if (previous == null) {
                            cardAddEvent(cardSerial);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(EXPIRETIME/10);
                    } catch (InterruptedException ex) {
                        ;
                    }
                }
            }
        } catch (IOException ex) {
            Log.debug("RFID problem " + ex.toString());
        }
    }

    public Set<String> currentCards() {
        Set<String> ret = null;
        synchronized (_seen) {
            ret = _seen.keySet();
        }
        return ret;
    }
}
