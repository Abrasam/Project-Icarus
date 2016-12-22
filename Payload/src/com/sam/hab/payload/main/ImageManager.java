package com.sam.hab.payload.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class ImageManager {

    private Queue<byte[]> imageQueue = new LinkedList<byte[]>();

    public ImageManager() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                int count = 0;
                Runtime rt = Runtime.getRuntime();

                try {
                    rt.exec("mkdir images").waitFor();
                    while (true) {
                        String name = String.valueOf(System.currentTimeMillis() / 1000) + ".jpg";
                        rt.exec("raspistill -o images/" + name).wait();
                        rt.exec("convert -resize 768x576\\! images/" + name + " tmp.jpg").waitFor();
                        rt.exec("./ssdv -e -c " + "CALLSIGN" + " -i " + String.valueOf(count) + " " + name + " out.bin");
                        Thread.sleep(30000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public byte[] getImagePacket() {
        if (imageQueue.size() <= 1) {
            try {
                FileInputStream fis = new FileInputStream(new File("out.bin"));
                while (fis.available() > 0) {
                    fis.read();
                    byte[] packet = new byte[255];
                    for (int i = 0; i < 255; i++) {
                        packet[i] = (byte)fis.read();
                    }
                    imageQueue.add(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageQueue.poll();
    }

}
