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

    private int count;

    private boolean transmitting;

    public CycleManager() {
        try {
            this.lora = new LoRa(869.850, Bandwidth.BW250, (short) 7, CodingRate.CR4_5, true) {
                @Override
                public void onRxDone() {
                    try {
                        receiveQueue.add(String.valueOf(super.readPayload()));
                        super.resetRXPtr();
                        super.setMode(Mode.RX);
                    } catch (IOException e) {
                    } catch (InterruptedException e) {
                    }
                }

                @Override
                public void onTxDone() {
                    count++;
                    if (count >= 10) {
                        count = 0;

                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("LoRa module failed to initialise, check your wiring perhaps?");
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
}
