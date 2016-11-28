package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;

import java.io.IOException;

public class PacketHandler implements Runnable {

    private final CycleManager cm;

    public PacketHandler(CycleManager cm) {
        this.cm = cm;
    }

    @Override
    public void run() {
        String packet = cm.getNextReceived();
        if (packet != null) {
            if (packet.charAt(0) == '>') {
                ReceivedPacket pckt = PacketParser.parseTwoWay(packet, "a key must go here");
                switch (pckt.type) {
                    case CMD:
                        if (pckt.data.equals("TRA")) {
                            try {
                                cm.switchMode(Mode.TX);
                            } catch (IOException e) {
                                //Error?
                            }
                        }
                    case SHELL:
                        //Do shell.
                    break;
                    case DIAG:
                        //Do diagnostic.
                        break;
                    case NACK:
                        //Do NACK.
                        break;
                    case OTHER:
                        //Not really sure what to do here either?
                }
            } else if (packet.charAt(0) == '$') {
                ReceivedTelemetry telem = PacketParser.parseTelemetry(packet);
                //Logic to upload to server goes here.
            }
        }
    }
}
