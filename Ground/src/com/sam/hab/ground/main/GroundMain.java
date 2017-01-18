package com.sam.hab.ground.main;

import com.sam.hab.ground.gui.GUI;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;

public class GroundMain {

    public static void main(String[] args) throws InterruptedException {

        final Config conf = new Config();

        JFrame frame = new JFrame("Prototype 2-Way HAB Comms");
        GUI gui = new GUI(conf);
        gui.init();
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.setSize(new Dimension(1024,768));
        frame.setContentPane(gui.getPanelMain());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Thread imageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedImage pic = ImageIO.read(new FileInputStream(new File("current.jpg")));
                        if (pic != null) {
                            Image resized = pic.getScaledInstance(512, 384, 0);
                            gui.getImg().setIcon(new ImageIcon(resized));
                        }
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        imageThread.start();

        //Ground station starts by transmitting.
        try {
            gui.cm.switchMode(Mode.RX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
