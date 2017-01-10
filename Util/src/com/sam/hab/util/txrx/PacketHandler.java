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
                    if (packet.charAt(0) == '>') {
                        ReceivedPacket pckt = PacketParser.parseTwoWay(packet, cm.key);

                        cm.handle2Way(pckt);

                        //Logic to upload and log goes here.

                    } else if (packet.charAt(0) == '$') {
                        ReceivedTelemetry telem = PacketParser.parseTelemetry(packet);

                        if (telem != null) {

                            cm.handleTelemetry(telem);

                            //Logic to upload to server goes here.
                        }
                    } else {
                        System.out.println("-1");
                        int[] res = PacketParser.parseSSDV(bytes);
                        System.out.println("0");
                        BufferedImage pic = null;
                        try {
                            cm.handleImage(res[0], res[1]);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            //Error?
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    //Error?
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
