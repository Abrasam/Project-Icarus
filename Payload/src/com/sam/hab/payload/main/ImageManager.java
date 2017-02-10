package com.sam.hab.payload.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public class ImageManager {

    private Queue<byte[]> imageQueue = new LinkedList<byte[]>();

    private String latestImage = "";
    private boolean fullDownload = false;

    /**
     * This class creates a thread which loops continuously taking a picture every 30 seconds or so (the inaccuracy is because the time taken to execute the commands is variable, particularly convert).
     * It also provides the means by which to get SSDV encoded image data in 256 byte packets.
     * @param callsign The payload callsign, this is used when encoding SSDV images.
     */
    public ImageManager(String callsign) {
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
                        rt.exec("raspistill -o images/" + name).waitFor();
                        rt.exec("convert images/" + name + " -resize 768x576! tmp.jpg").waitFor();
                        rt.exec("./ssdv -e -c " + callsign + " -i " + String.valueOf(count) + " tmp.jpg out.bin").waitFor();
                        latestImage = name;
                        count++;
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

    /**
     * Returns the next image packet that is ready to be sent by the payload.
     * @return A 256 byte array which equates to one packet.
     */
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
        byte[] packet = imageQueue.poll();
        return packet;
    }

}