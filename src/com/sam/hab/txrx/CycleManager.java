package com.sam.hab.txrx;

import com.sam.hab.lora.Constants;
import com.sam.hab.lora.Constants.*;
import com.sam.hab.lora.LoRa;
import com.sam.hab.lora.LoRaReceiver;
import com.sam.hab.lora.LoRaTransmitter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class CycleManager {

    private Queue<String> transmitQueue = new LinkedList<String>();
    private Queue<String> receiveQueue = new LinkedList<String>();

    private LoRaTransmitter loraSend;
    private LoRaReceiver loraReceive;

    private boolean transmitting;

    public CycleManager() {
        try {
            loraSend = new LoRaTransmitter(869.850, Bandwidth.BW250, (short)7, CodingRate.CR4_5, true);
            loraReceive = new LoRaReceiver(869.850, Bandwidth.BW250, (short)7, CodingRate.CR4_5, true);
        } catch (IOException e) {
            throw new RuntimeException("LoRa module contact not established, check your wiring?");
        }
    }

    public boolean isTxFull() {
        return transmitQueue.size() >= 10;
    }

    public void addToTx(String payload) {
        if (!isTxFull()) {
            transmitQueue.add(payload);
        }
    }

    public String getNextReceived() {
        if (receiveQueue.size() > 0) {
            return receiveQueue.poll();
        }
        return null;
    }

    public void switchMode(Mode mode) throws IOException {
        if (mode == Mode.TX) {

        } else if (mode == Mode.TX) {

        }
    }

    public void addToRx(byte[] payload) {
        receiveQueue.add(String.valueOf(payload));
    }
}
