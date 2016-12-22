package com.sam.hab.ground.main;

import com.sam.hab.ground.gui.GUI;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class GroundMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        Calendar cal = Calendar.getInstance();
        JFrame frame = new JFrame("Prototype 2-Way HAB Comms");
        GUI gui = new GUI();
        gui.init();
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.setContentPane(gui.getPanelMain());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        CycleManager cm = new CycleManager(false) {
            @Override
            public void handleTelemetry(ReceivedTelemetry telem) {
                gui.getAlt().setText(String.valueOf(telem.alt));
                gui.getLat().setText(String.valueOf(telem.lat));
                gui.getLon().setText(String.valueOf(telem.lon));
                gui.getLastpckt().setText(cal.getTime().toString());

                gui.writeRx(telem.raw + "\n");
            }

            @Override
            public void handleImage(BufferedImage pic, int iID, int pID) {
                Image resized = pic.getScaledInstance(640, 480, 0);
                gui.getImg().setIcon(new ImageIcon(resized));
                gui.writeRx("Image no. " + iID + " packet no. " + pID + " received.\n");
            }

            @Override
            public void handle2Way(ReceivedPacket packet) {
                switch (packet.type) {
                    case CMD:
                        if (packet.data.equals("TRA")) {
                            try {
                                this.switchMode(Mode.TX);
                            } catch (IOException e) {
                                //Error?
                            }
                        }
                    case SHELL:
                        gui.getConsoleOutput().append(packet.data);
                        break;
                    case DIAG:
                        String[] data = packet.data.split("/");
                        gui.getControlResults().append(data[0] + ": " + data[1] + "\n");
                        break;
                    case NACK:
                        String[] ids = packet.data.split("/");
                        String[] transmitted = getTransmitted();
                        for (String id : Arrays.asList(ids)) {
                            try {
                                addToTx(transmitted[Integer.parseInt(String.valueOf(id))].getBytes());
                            } catch (NumberFormatException e) {
                                //Error?
                            } catch (ArrayIndexOutOfBoundsException e) {
                                //Error?
                            }
                        }
                        break;
                    case OTHER:
                        //Not really sure what to do here? How about you?
                }
            }

            @Override
            public String getTelemetry() {
                return null;
            }

            @Override
            public String getImagePacket() {
                return null;
            }
        };
        cm.switchMode(Mode.RX);
    }
}
