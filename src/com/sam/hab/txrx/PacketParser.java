package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;
import com.sam.hab.util.CRC16CCITT;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PacketParser {

    public static ReceivedPacket parseTwoWay(String raw, String key) {
        String cSum = raw.split("\\*")[1];
        String packet = raw.split("\\*")[0];
        packet = packet.replace(">", "");
        if (!CRC16CCITT.calcCsum((packet + key).getBytes()).equals(cSum)) {
            return null;
        }
        String[] packetList = packet.split(",");
        PacketType packetType = PacketType.lookup(Integer.valueOf(packetList[2]));
        if (packetType == null) {
            return null;
        }
        return new ReceivedPacket(raw, packetList[3], Integer.valueOf(packetList[1]), packetType);
    }

    public static ReceivedTelemetry parseTelemetry(String raw) {
        String cSum = raw.split("\\*")[1].replace("\n", "");
        String packet = raw.split("\\*")[0];
        packet = packet.replace("$", "");
        if (!CRC16CCITT.calcCsum((packet).getBytes()).equals(cSum)) {
            return null;
        }
        String packetList[] = packet.split(",");
        return new ReceivedTelemetry(raw, Float.valueOf(packetList[3]), Float.valueOf(packetList[4]), Float.valueOf(packetList[5]), Float.valueOf(packetList[1]));
    }

    public static int[] parseSSDV(byte[] in) {
        byte[] bytes = new byte[in.length+1];
        bytes[0] = 0x55;
        System.arraycopy(in, 0, bytes, 1, in.length);
        System.out.println(Arrays.toString(bytes));
        int imageNo = (0xFF & bytes[6]);
        System.out.println(imageNo);
        int packetNo = (0xFF & bytes[7]) * 256 + (0xFF & bytes[8]);
        //TODO: Log here that I've received a packet.
        FileOutputStream fos = null;
        File file = new File("image_" + String.valueOf(imageNo) + ".bin");
        Runtime rt = Runtime.getRuntime();
        try {
            file.createNewFile();
            fos = new FileOutputStream(file, true);
            fos.write(bytes);
            Process pr = rt.exec("./ssdv -d image_" + String.valueOf(imageNo) + ".bin current.jpg");
            pr.waitFor(1000, TimeUnit.MILLISECONDS);
            System.out.println(pr.waitFor());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //Error here?
        } catch (IOException e) {
            e.printStackTrace();
            //Error here?
        } catch (InterruptedException e) {
            //This really shouldn't happen but:
            //Error here?
        }
        return new int[] {imageNo, packetNo};
    }

}
