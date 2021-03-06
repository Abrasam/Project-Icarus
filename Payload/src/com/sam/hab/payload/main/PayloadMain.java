package com.sam.hab.payload.main;

import com.sam.hab.payload.serial.GPSLoop;
import com.sam.hab.util.csum.CRC16CCITT;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;
import com.sam.hab.util.txrx.TwoWayPacketGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PayloadMain {

    private static String currentTelemetry;
    private static String callsign;

    private static Config conf;
    private static CycleManager cm;
    private static ImageManager im;

    /**
     * Takes a GPS GGA string and generates a telemetry string using it, then stores this in the static variable currentTelemetry.
     * @param gps the GGA string.
     */
    public static void generateTelemetry(String gps) {
        if (gps.startsWith("$GNGGA"))
        {
            String[] data = gps.split(",");
            if (data.length > 9)
            {
                try {
                    String lat = (data[3].equals("S") ? "-" : "") + data[2];
                    String lon = (data[5].equals("W") ? "-" : "") + data[4];
                    String time = data[1].substring(0, 2) + ":" + data[1].substring(2, 4) + ":" + data[1].substring(4, 6);
                    String telemetry = callsign + "," + String.valueOf(System.currentTimeMillis() / 1000) + "," + time + "," + lat + "," + lon + "," + data[9] + "," + data[7];
                    String csum = CRC16CCITT.calcCsum(telemetry.getBytes(StandardCharsets.ISO_8859_1));
                    telemetry = "$$" + telemetry + "*" + csum + "\n";
                    currentTelemetry = telemetry;
                } catch (StringIndexOutOfBoundsException e) {
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        conf = new Config();
        callsign = conf.getCallsign();
        Thread gps = new Thread(new GPSLoop());
        gps.start();
        im = new ImageManager(conf.getCallsign());
        while (currentTelemetry == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cm = new CycleManager(true, conf.getCallsign(), new double[] {conf.getFreq(), conf.getListen()}, new Constants.Bandwidth[] {conf.getTransmitBandwidth(), conf.getReceiveBandwidth()}, conf.getSf(), conf.getCodingRate(), !conf.getImplicit(), conf.getPower(), conf.getKey()) {
            @Override
            public void handleTelemetry(ReceivedTelemetry telem) {
                return;
            }

            @Override
            public void onSend(String sent) {
                return;
            }

            @Override
            public void handleImage(byte[] bytes, int iID, int pID) {
                return;
            }

            @Override
            public void handleTwoWay(ReceivedPacket packet) {
                switch (packet.type) {
                    case CMD:
                        handleCommand(packet);
                        break;
                    case SHELL:
                        handleShell(packet);
                        break;
                    case OTHER:
                        System.err.println("Packet classed 'OTHER' received:");
                        System.err.println(packet.data);
                }
            }

            @Override
            public String getTelemetry() {
                return currentTelemetry;
            }

            @Override
            public String getImagePacket() {
                byte[] pckt = im.getImagePacket();
                if (pckt != null) {
                    return new String(pckt, StandardCharsets.ISO_8859_1);
                }
                return null;
            }
        };

        //Payloads begin by transmitting.
        cm.mainLoop(Constants.Mode.TX);
    }

    /**
     * Takes a received command packet and determines what action is to be taken, then takes that action.
     * @param packet The packet to analyse.
     */
    public static void handleCommand(ReceivedPacket packet) {
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
                cm.addToTx(TwoWayPacketGenerator.generateStatPacket(conf.getCallsign(), "IMGNO", String.valueOf(imageNo)));
                break;
            case "IMG":
                boolean sendImages = cm.toggleImage();
                cm.addToTx(TwoWayPacketGenerator.generateStatPacket(conf.getCallsign(), "IMG", String.valueOf(sendImages)));
                break;
        }
    }

    /**
     * Takes a shell packet and executes the given command, storing the result and preparing it for sending, adds that result to the transmit queue.
     * @param packet the packet to analyse.
     */
    public static void handleShell(ReceivedPacket packet) {
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec(packet.data);
            if (pr.waitFor(5, TimeUnit.SECONDS)) {
                InputStream stream = pr.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String output = "";
                String line = reader.readLine();
                while (line != null) {
                    output += line + "\n";
                    line = reader.readLine();
                }
                int len = 255 - 14 - conf.getCallsign().length();
                String[] toSend = new String[(int)Math.ceil(output.length() / (float)len)];
                if (output.length() > len) {
                    for (int i = 0; i < toSend.length-1; i++) {
                        toSend[i] = output.substring(0, (len > output.length() ? output.length() -1 : len -1));
                        output = output.substring(len);
                    }
                }
                toSend[toSend.length -1] = output;
                String[] packets = TwoWayPacketGenerator.generateShellPackets(conf.getCallsign(), toSend);
                for (String pckt : Arrays.asList(packets)) {
                    cm.addToTx(pckt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}