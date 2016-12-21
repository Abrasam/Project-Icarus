package com.sam.hab.txrx;

import com.sam.hab.gui.GUI;
import com.sam.hab.lora.Constants.*;
import com.sam.hab.lora.LoRa;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Queue;

public class CycleManager {

    private Queue<byte[]> transmitQueue = new LinkedList<byte[]>();
    private Queue<byte[]> receiveQueue = new LinkedList<byte[]>();

    private LoRa lora;

    protected final GUI gui;

    public CycleManager(GUI gui) {
        this.gui = gui;
        try {
            lora = new LoRa(869.850, Bandwidth.BW250, (short)7, CodingRate.CR4_5, true, this);
        } catch (IOException e) {
            throw new RuntimeException("LoRa module contact not established, perhaps check your wiring?");
        }

        Thread packetThread = new Thread(new PacketHandler(this));
        packetThread.start();
    }

    public boolean isTxFull() {
        return transmitQueue.size() >= 10;
    }

    public void addToTx(byte[] payload) {
        if (!isTxFull()) {
            transmitQueue.add(payload);
        }
    }

    public byte[] getNextReceived() {
        if (receiveQueue.size() > 0) {
            return receiveQueue.poll();
        }
        return null;
    }

    public void switchMode(Mode mode) throws IOException {
        if (mode == Mode.TX) {
            lora.setMode(Mode.STDBY);
            String[] transmit = new String[10];
            for (int i = 0; i < 10; i++) {
                if (transmitQueue.size() <= 0) {
                    break;
                }
                transmit[i] = new String(transmitQueue.poll());
            }
            lora.send(transmit);
        } else if (mode == Mode.RX) {
            lora.setMode(Mode.RX);
            lora.setDIOMapping(DIOMode.RXDONE);
        }
    }

    public void addToRx(byte[] payload)  {
        receiveQueue.add(payload);
    }
}
