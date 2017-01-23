package com.sam.hab.util.txrx;

import com.sam.hab.util.csum.CRC16CCITT;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.lora.LoRa;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class CycleManager {

    /*
     * This class manages the switching between transmission and receiving modes of the radio and manages the handling of all received packets and the preparation for sending for all packets.
     */

    private Queue<String> transmitQueue = new LinkedList<String>();
    private Queue<String> receiveQueue = new LinkedList<String>();
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
            lora = new LoRa(freq[0], bandwidth, sf, codingRate, explicit);
        } catch (IOException e) {
            throw new RuntimeException("LoRa module contact not established, check your wiring perhaps?");
        }

        Thread packetThread = new Thread(new PacketHandler(this));
        packetThread.start();
    }

    public void addToTx(String payload) {
        System.out.println(payload);
        transmitQueue.add(payload);
    }

    public String getNextReceived() {
        if (receiveQueue.size() > 0) {
            return receiveQueue.poll();
        }
        return null;
    }

    private boolean transmit = false;

    public void txInterrupt() {
        transmit = true;
    }

    //TODO: This will eventually stack overflow. So, you know, fix that and all.
    public void switchMode(Mode mode) throws IOException {
        if (mode == Mode.TX && lora.getMode() == mode) {
            return; //Prevent unnecessary changes and potential massive errors...
        }
        if (mode == Mode.TX) {
            transmit = false;
            lora.setMode(Mode.STDBY);
            lora.setFrequency(freq[0]);
            String[] transmit = new String[(payload ? 90 : 10)];
            for (int i = 0; i < 10; i ++) {
                if (transmitQueue.size() <= 0) {
                    transmit[i] = doPacket(">>" + callSign + "," + String.valueOf(i) + ",5,NULL", key);
                } else {
                    transmit[i] = doPacket(String.format(transmitQueue.poll(), String.valueOf(i)), key);
                    transmitted[i] = transmit[i];
                }
            }
            if (payload) {
                for (int i = 10; i < 20; i ++) {
                    transmit[i] = getTelemetry();
                }
                for (int i = 20; i < 89; i ++) {
                    transmit[i] = getImagePacket();
                }
                transmit[89] = doPacket(String.format(TwoWayPacketGenerator.generateCommand(callSign, "TRA"), String.valueOf(89)), key);
            }
            for (String pckt : transmit) {
                onSend(pckt);
            }
            lora.send(transmit);
            switchMode(Mode.RX);
        } else if (mode == Mode.RX) {
            lora.setMode(Mode.STDBY);
            lora.setFrequency(freq[1]);
            lora.setDIOMapping(DIOMode.RXDONE);
            lora.setMode(Mode.RX);
            long timeout = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < timeout && !transmit) {
                while (!lora.pollDIO0() && !transmit && System.currentTimeMillis() < timeout) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (lora.pollDIO0()) {
                    lora.clearIRQFlags();
                    byte[] payload = lora.handlePacket();
                    if (payload != null) {
                        addToRx(payload);
                        timeout = timeout + 1000;
                    }
                }
            }
            if (transmit || payload) {
                switchMode(Mode.TX);
            } else {
                switchMode(Mode.RX);
            }
        }
    }

    private String doPacket(String packet, String key) {
        return packet + "*" + CRC16CCITT.calcCsum((packet + key).getBytes()) + "\n";
    }

    public String[] getTransmitted() {
        return transmitted;
    }

    public void addToRx(byte[] payload) {
        receiveQueue.add(new String(payload, StandardCharsets.ISO_8859_1));
    }

    public abstract void handleTelemetry(ReceivedTelemetry telem);

    public abstract void onSend(String sent);

    public abstract void handleImage(int iID, int pID);

    public abstract void handle2Way(ReceivedPacket packet);

    public abstract String getTelemetry();

    public abstract String getImagePacket();
}
