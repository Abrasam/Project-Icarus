package com.sam.hab.payload.main;

import com.sam.hab.payload.serial.GPSLoop;
import com.sam.hab.util.csum.CRC16CCITT;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;

import java.awt.image.BufferedImage;

public class PayloadMain {

    private static String currentTelemetry;

    public static void generateTelemetry(String gps) {
        if (gps.startsWith("$GNGGA"))
        {
            String[] data = gps.split(",");
            if (data.length > 9)
            {
                String lat = (data[3].equals("S") ? "-" : "") + data[2];
                String lon = (data[5].equals("W") ? "-" : "") + data[4];
                String time = time = data[1].substring(0, 2) + ":" + data[1].substring(2, 4) + ":" + data[1].substring(4, 6);
                String telemetry = "$$" + "CALLSIGN" +"," + "%s," + time + "," + lat + "," + lon + "," + data[9] + "," + data[7];
                String csum = CRC16CCITT.calcCsum(telemetry.getBytes()).toUpperCase();
                telemetry = telemetry + "*" + csum + "\n";
                currentTelemetry = telemetry;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Config conf = new Config();
        Thread gpsThread = new Thread(new GPSLoop());
        gpsThread.start();
        ImageManager im = new ImageManager();
        CycleManager cm = new CycleManager(true, conf.getCallsign(), new double[] {conf.getFreq(), conf.getListen()}, conf.getBandwidth(), conf.getSf(), conf.getCodingRate(), !conf.getImplicit(), conf.getKey()) {
            @Override
            public void handleTelemetry(ReceivedTelemetry telem) {
                return;
            }

            @Override
            public void handleImage(BufferedImage pic, int iID, int pID) {
                return;
            }

            @Override
            public void handle2Way(ReceivedPacket packet) {

            }

            @Override
            public String getTelemetry() {
                return currentTelemetry;
            }

            @Override
            public String getImagePacket() {
                return new String(im.getImagePacket());
            }
        };
        while (true) {
            Thread.sleep(1000);
        }
    }
}
