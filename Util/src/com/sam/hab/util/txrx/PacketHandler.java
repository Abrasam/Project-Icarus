package com.sam.hab.util.txrx;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class PacketHandler implements Runnable {

    private final CycleManager cm;

    /**
     * This is a simple loop which gets the latest packet from the received queue, determines its type and then calls the relevant parse and handle methods.
     * @param cm The cycle manager currently in use, needed in order to get latest received packet.
     */
    public PacketHandler(CycleManager cm) {
        this.cm = cm;
    }

    @Override
    public void run() {
        Calendar cal = Calendar.getInstance();
        while (true) {
            try {
                String packet = cm.getNextReceived();
                if (packet != null) {
                    byte[] bytes = packet.getBytes(StandardCharsets.ISO_8859_1);
                    if (packet.length() > 0) {
                        if (packet.charAt(0) == '>') {
                            ReceivedPacket pckt = PacketParser.parseTwoWay(packet, cm.key);
                            cm.handleTwoWay(pckt);
                        } else if (packet.charAt(0) == '$') {
                            ReceivedTelemetry telem = PacketParser.parseTelemetry(packet);
                            if (telem != null) {
                                cm.handleTelemetry(telem);
                            }
                        } else {
                            int[] res = PacketParser.parseSSDV(bytes);
                            try {
                                cm.handleImage(bytes, res[0], res[1]);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}