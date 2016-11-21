package com.sam.hab.txrx;

import com.sam.hab.lora.Constants;
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
            this.lora = new LoRa(869.850, Constants.Bandwidth.BW250, (short) 7, Constants.CodingRate.CR4_5, true) {
                @Override
                public void onRxDone() {
                    try {
                        receiveQueue.add(String.valueOf(super.readPayload()));
                    } catch (IOException e) {
                    }
                }

                @Override
                public void onTxDone() {

                }
            };
        } catch (IOException e) {
            throw new RuntimeException("LoRa module failed to initialise, check your wiring perhaos?");
        }
    }

    public boolean prepTx(String packet) {
        if (transmitQueue.size() < 10) {
            transmitQueue.add(packet);
            return true;
        }
        return false;
    }

    public String getNextReceived() {
        if (receiveQueue.size() > 0) {
            return receiveQueue.poll();
        }
        return null;
    }

    public void changeToReceive() {

    }

    public void changeToTransmit() {

    }

}
