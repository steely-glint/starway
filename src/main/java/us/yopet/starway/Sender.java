/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 *
 * @author thp
 */
public class Sender {

    private final InetSocketAddress _toAdd;
    private DatagramSocket _udp;
    private int _capacity;
    private ByteBuffer _buffer;

    void send(Star[] stars) throws IOException {

        short body_len = (short) (_capacity - 4);
        _buffer.put(0, (byte) 0);// channel
        _buffer.put(1, (byte) 0);// command
        _buffer.putShort(2, body_len);
        int offs = 4;
        for (Star s : stars) {
            s.put(_buffer, offs);
        }
        byte[] opcMess = _buffer.array();
        DatagramPacket dgp = new DatagramPacket(opcMess, 0, opcMess.length, this._toAdd);
        Log.verb(("sending packet size "+opcMess.length+ " to "+_toAdd));
        _udp.send(dgp);
    }

    Sender(InetSocketAddress address,int ledCount) {
        _toAdd = address;
        _capacity = (ledCount * 3 + 4);
        _buffer = ByteBuffer.allocate(_capacity);
        try {
            _udp = new DatagramSocket();
        } catch (SocketException ex) {
            throw new IllegalArgumentException("Socket problem " + ex.getMessage());
        }
    }

}
