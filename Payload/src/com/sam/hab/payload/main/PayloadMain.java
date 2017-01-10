package com.sam.hab.payload.main;

import com.sam.hab.payload.serial.GPSLoop;
import com.sam.hab.util.csum.CRC16CCITT;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;
import com.sam.hab.util.txrx.TwoWayPacketGenerator;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PayloadMain {

    private static String currentTelemetry;
    private static String callsign;

    public static void generateTelemetry(String gps) {
        if (gps.startsWith("$GNGGA"))
        {
            String[] data = gps.split(",");
            if (data.length > 9)
            {
                String lat = (data[3].equals("S") ? "-" : "") + data[2];
                String lon = (data[5].equals("W") ? "-" : "") + data[4];
                String time = time = data[1].substring(0, 2) + ":" + data[1].substring(2, 4) + ":" + data[1].substring(4, 6);
                String telemetry = "$$" + callsign +"," + String.valueOf(System.currentTimeMillis()/1000) +"," + time + "," + lat + "," + lon + "," + data[9] + "," + data[7];
                String csum = CRC16CCITT.calcCsum(telemetry.getBytes()).toUpperCase();
                telemetry = telemetry + "*" + csum + "\n";
                currentTelemetry = telemetry;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Config conf = new Config();
        callsign = conf.getCallsign();
        Thread gpsThread = new Thread(new GPSLoop());
        gpsThread.start();
        ImageManager im = new ImageManager();
        while (currentTelemetry == null) {
            Thread.sleep(500);
        }
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
                switch (packet.type) {
                    case CMD:
                        switch(packet.data) {
                            case "RBT":
                                Runtime rt = Runtime.getRuntime();
                                try {
                                    rt.exec("sudo reboot");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "IMGNO":
                                File f = new File("images/");
                                int imageNo = 0;
                                if (f.exists() && f.isDirectory()) {
                                    imageNo = f.list().length;
                                }
                                this.addToTx(TwoWayPacketGenerator.generateStatPacket(conf.getCallsign(), conf.getKey(), "IMGNO", String.valueOf(imageNo)).getBytes());
                                break;
                            case "BATV":
                                //TO BE IMPLEMENTED WITH SOME VOLTMETER.
                                break;
                            case "EXTEMP":
                                //TO BE IMPLEMENTED WITH SOME SENSOR.
                                break;
                            case "NOGPS":
                                this.addToTx(TwoWayPacketGenerator.generateStatPacket(conf.getCallsign(), conf.getKey(), "NOGPS", getTelemetry().split(",")[5]).getBytes());
                                break;
                            case "IMG":
                                //TO BE IMPLEMENTED.
                                break;
                        }
                        break;
                    case SHELL:
                        Runtime rt = Runtime.getRuntime();
                        try {
                            Process pr = rt.exec(packet.data);
                            if (pr.waitFor(5, TimeUnit.SECONDS)) {
                                InputStream stream = pr.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                                String output = "";
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    output += line;
                                }
                                int len = 255 - 14 - conf.getCallsign().length();
                                String[] toSend = new String[(int)Math.ceil(output.length() / (float)len)];
                                if (output.length() > len) {
                                    for (int i = 0; i < toSend.length-1; i++) {
                                        toSend[i] = output.substring(i*len, (i+1)*len);
                                        output = output.substring(len);
                                    }
                                    toSend[toSend.length -1] = output;
                                }
                                String[] packets = TwoWayPacketGenerator.generateShellPackets(conf.getCallsign(), conf.getKey(), toSend);
                                for (String pckt : Arrays.asList(packets)) {
                                    this.addToTx(pckt.getBytes(StandardCharsets.ISO_8859_1));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case NACK:
                        String[] ids = packet.data.split("/");
                        String[] transmitted = getTransmitted();
                        for (String id : Arrays.asList(ids)) {
                            try {
                                addToTx(transmitted[Integer.parseInt(String.valueOf(id))].getBytes(StandardCharsets.ISO_8859_1));
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
                if (packet.id >= 9) {
                    try {
                        switchMode(Constants.Mode.TX);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Error?
                    }
                }
            }

            @Override
            public String getTelemetry() {
                return currentTelemetry;
            }

            @Override
            public String getImagePacket() {
                byte[] pckt = im.getImagePacket();
                return new String(pckt, StandardCharsets.ISO_8859_1);
            }
        };
        while (true) {
            Thread.sleep(1000);
        }
    }
}
