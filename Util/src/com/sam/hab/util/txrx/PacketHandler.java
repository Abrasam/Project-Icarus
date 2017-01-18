package com.sam.hab.util.txrx;

import com.sam.hab.util.lora.Constants.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class PacketHandler implements Runnable {

    private final CycleManager cm;

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
                            System.out.println(packet);

                            ReceivedPacket pckt = PacketParser.parseTwoWay(packet, cm.key);

                            System.out.println(pckt == null);

                            cm.handle2Way(pckt);

                            //Logic to upload and log goes here.

                        } else if (packet.charAt(0) == '$') {
                            ReceivedTelemetry telem = PacketParser.parseTelemetry(packet);

                            if (telem != null) {

                                cm.handleTelemetry(telem);

                                //Logic to upload to server goes here.
                            }
                        } else {
                            int[] res = PacketParser.parseSSDV(bytes);
                            BufferedImage pic = null;
                            try {
                                cm.handleImage(res[0], res[1]);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                //Error?
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //Error?
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
