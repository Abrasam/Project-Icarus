package com.sam.hab.util.txrx;

import com.sam.hab.util.csum.CRC16CCITT;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.lora.LoRa;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public abstract class CycleManager {

    /*
     * This class manages the switching between transmission and receiving modes of the radio and manages the handling of all received packets and the preparation for sending for all packets.
     */

    private Queue<byte[]> transmitQueue = new LinkedList<byte[]>();
    private Queue<byte[]> receiveQueue = new LinkedList<byte[]>();
    private String[] transmitted = new String[10];

    private final boolean payload;

    private final LoRa lora; //WON'T STAY PUBLIC!

    private final double[] freq;

    protected final String callSign;
    protected final String key;

    public CycleManager(boolean payload, String callSign, double[] frequency, Bandwidth bandwidth, short sf, CodingRate codingRate, boolean explicit, String key) { //frequency array, the 0 index is for transmit, the 1 index is for receive.
        this.payload = payload;
        this.freq = frequency;
        this.callSign = callSign;
        this.key = key;
        try {
            lora = new LoRa(freq[0], bandwidth, sf, codingRate, explicit, this);
        } catch (IOException e) {
            throw new RuntimeException("LoRa module contact not established, check your wiring perhaps?");
        }

        Thread packetThread = new Thread(new PacketHandler(this));
        packetThread.start();

        if (payload) { //Payloads should start by transmitting.
            try {
                switchMode(Mode.TX);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addToTx(byte[] payload) {
        transmitQueue.add(payload);
    }

    public byte[] getNextReceived() {
        if (receiveQueue.size() > 0) {
            return receiveQueue.poll();
        }
        return null;
    }

    public void switchMode(Mode mode) throws IOException {
        if (lora.getMode() == mode) {
            return; //Prevent unnecessary changes and potential massive errors...
        }
        if (mode == Mode.TX) {
            lora.setMode(Mode.STDBY);
            lora.setFrequency(freq[0]);
            String[] transmit = new String[(payload ? 90 : 10)];
            for (int i = 0; i < 10; i ++) {
                if (transmitQueue.size() <= 0) {
                    transmit[i] = ">>" + callSign + "," + String.valueOf(i) + ",5,NULL*" + CRC16CCITT.calcCsum((">>" + callSign + "," + String.valueOf(i) + ",5,NULL").getBytes()) + "\n";
                } else {
                    transmit[i] = String.format(new String(transmitQueue.poll()), String.valueOf(i));
                    transmitted[i] = transmit[i];
                }
            }
            if (payload) {
                for (int i = 10; i < 20; i ++) {
                    transmit[i] = getTelemetry();
                }
                for (int i = 20; i < 90; i ++) {
                    transmit[i] = getImagePacket();
                }
            }
            lora.send(transmit);
            switchMode(Mode.RX);
        } else if (mode == Mode.RX) {
            lora.setMode(Mode.STDBY);
            lora.setFrequency(freq[1]);
            lora.setMode(Mode.RX);
            lora.setDIOMapping(DIOMode.RXDONE);
            lora.receive((payload ? 10 : 90), (payload ? 10000 : - 1));
            switchMode(Mode.TX);
        }
    }

    public String[] getTransmitted() {
        return transmitted;
    }

    public void addToRx(byte[] payload) {
        receiveQueue.add(payload);
    }

    public abstract void handleTelemetry(ReceivedTelemetry telem);

    public abstract void handleImage(BufferedImage pic, int iID, int pID);

    public abstract void handle2Way(ReceivedPacket packet);

    public abstract String getTelemetry();

    public abstract String getImagePacket();
}
