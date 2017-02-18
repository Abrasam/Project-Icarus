package com.sam.hab.ground.main;

import com.sam.hab.ground.gui.GUI;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class GroundMain {

    public static void main(String[] args) throws InterruptedException {

        //Load configuration from config.yml (or create new if none exists).
        final Config conf = new Config();

        //Window setup.

        JFrame frame = new JFrame("Prototype 2-Way HAB Comms");
        GUI gui = new GUI(conf);
        gui.init();
        frame.setPreferredSize(new Dimension(1050, 720));
        frame.setSize(new Dimension(1050,720));
        frame.setContentPane(gui.getPanelMain());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //This thread continuously updates the image being displayed on the SSDV tab so that we always have the latest image, it also deletes old images (>30minutes old).
        Thread imageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedImage pic = ImageIO.read(new FileInputStream(new File("images/current.jpg")));
                        if (pic != null) {
                            Image resized = pic.getScaledInstance(512, 384, 0);
                            gui.getImg().setIcon(new ImageIcon(resized));
                        }
                        Thread.sleep(1000);
                        File imagesFolder = new File("images");
                        File[] files = imagesFolder.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            if (System.currentTimeMillis() - files[i].lastModified() > 1800000 && files[i].getName().matches("image_(.*).bin")) {
                                files[i].delete();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        imageThread.start();

        //Ground station starts by transmitting.
        gui.cm.mainLoop(Mode.RX);
    }
}