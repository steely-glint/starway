/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 *
 * @author tim
 */
public class Star {

    SecureRandom rand = new SecureRandom();
    private final RGBLed[] _leds;
    private final String _name;
    private final int _size;
    final static int TWINKLE = 10;
    private int _twinkleCount;
    private int _twinkleInterval;
    private final int _twinkleAmplitude;
    private final int _twinkleDuration;
    private Long _seq = null;

    public Star(JsonObject config) {
        _twinkleCount = 0;
        _twinkleInterval = 5 + rand.nextInt(15);
        _twinkleAmplitude = 25;
        _twinkleDuration = 1 + rand.nextInt(4);
        JsonArray leds = config.getJsonArray("leds");
        int nleds = (leds == null) ? 0 : leds.size();
        _leds = new RGBLed[nleds];
        for (int i = 0; i < nleds; i++) {
            _leds[i] = new RGBLed(leds.getInt(i));
        }
        _name = config.getString("name");
        _seq = new Long(config.getInt("seq", 0));
        _size = config.getInt("size", 0);
    }

    Star(Star clone) {
        _twinkleCount = clone._twinkleCount;
        _twinkleInterval = clone._twinkleAmplitude;
        _twinkleAmplitude = 25;
        _twinkleDuration = clone._twinkleDuration;

        _leds = new RGBLed[clone._leds.length];
        for (int i = 0; i < _leds.length; i++) {
            _leds[i] = new RGBLed(clone._leds[i]._offset);
        }
        _name = clone._name;
        _seq = clone._seq;
        _size = clone._size;
    }

    void put(ByteBuffer bb, int base) {
        for (RGBLed led : _leds) {
            led.put(bb, base);
        }
    }

    void setColour(int r, int g, int b) {
        for (RGBLed led : _leds) {
            led.red = r;
            led.blue = b;
            led.green = g;
        }
    }

    void twinkle() {
        int twinkleToes = _twinkleCount++ % _twinkleInterval;
        for (RGBLed led : _leds) {
            if (twinkleToes == 0) {
                led.red += _twinkleAmplitude;
                led.blue += _twinkleAmplitude;
                led.green += _twinkleAmplitude;
            }
            if (twinkleToes == _twinkleDuration) {
                led.red -= _twinkleAmplitude;
                led.blue -= _twinkleAmplitude;
                led.green -= _twinkleAmplitude;
            }
        }
    }

    Long getSeq() {
        return _seq;
    }

    int getSize() {
        return _size;
    }

    String getName() {
        return _name;
    }

    void cloneColour(Star dst) {
        if ((_leds != null) && (_leds.length > 0)) {
            dst.setColour(_leds[0].red, _leds[0].green, _leds[0].blue);
        }
    }

    void setColour(int[] cols) {
        if ((cols != null) && (cols.length == 3)) {
            this.setColour(cols[0], cols[1], cols[2]);
        }
    }
}
