package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            byte[] bytes = cm.getNextReceived();
            if (bytes != null) {
                String packet = new String(bytes);
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

                    //Logic to upload and log goes here.

                } else if (packet.charAt(0) == '$') {
                    ReceivedTelemetry telem = PacketParser.parseTelemetry(packet);

                    if (telem != null) {

                        cm.gui.getAlt().setText(String.valueOf(telem.alt));
                        cm.gui.getLat().setText(String.valueOf(telem.lat));
                        cm.gui.getLon().setText(String.valueOf(telem.lon));
                        cm.gui.getLastpckt().setText(cal.getTime().toString());

                        cm.gui.writeRx(packet + "\n");

                        //Logic to upload to server goes here.
                    }
                } else {
                    int[] res = PacketParser.parseSSDV(bytes);
                    BufferedImage pic = null;
                    try {
                        pic = ImageIO.read(new FileInputStream(new File("current.jpg")));
                        Image resized = pic.getScaledInstance(640, 480, 0);
                        cm.gui.getImg().setIcon(new ImageIcon(resized));
                        cm.gui.writeRx("Image no. " + res[0] + " packet no. " + res[1] + " received.\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Error?
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        //Error?
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //Error?
            }
        }
    }
}
