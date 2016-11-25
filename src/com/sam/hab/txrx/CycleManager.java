package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;
import com.sam.hab.lora.LoRa;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class CycleManager {

    private Queue<String> transmitQueue = new LinkedList<String>();
    private Queue<String> receiveQueue = new LinkedList<String>();

    private LoRa lora;

    private boolean transmitting;

    public CycleManager() {
        try {
            lora = new LoRa(869.850, Bandwidth.BW250, (short)7, CodingRate.CR4_5, true);
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
            lora.setMode(Mode.SLEEP);
            String[] transmit = new String[10];
            for (int i = 0; i < 10; i++) {
                if (transmitQueue.size() <= 0) {
                    break;
                }
                transmit[i] = transmitQueue.poll();
            }
            lora.send(transmit);
        } else if (mode == Mode.TX) {

        }
    }

    public void addToRx(byte[] payload) {
        receiveQueue.add(String.valueOf(payload));
    }
}
