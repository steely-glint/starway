/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import java.nio.ByteBuffer;

/**
 *
 * @author thp
 */
public class RGBLed {
    int red;
    int green;
    int blue;
    final int _offset; 

    RGBLed(int offs) {
        _offset = offs;
    }
    void put(ByteBuffer bb,int base){
        int pp = base + (3*_offset);
        bb.put(pp++, (byte)((0xff) & red));
        bb.put(pp++,(byte)((0xff) &green));
        bb.put(pp,(byte)((0xff) &blue));
    }
}
