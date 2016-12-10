package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;
import com.sam.hab.util.CRC16CCITT;

import java.io.*;

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

    public static void parseSSDV(String raw) {
        raw = "U" + raw;
        byte[] bytes = raw.getBytes();
        int imageNo = bytes[6];
        int packetNo = bytes[7] * 256 + bytes[8];
        //TODO: Log here that I've received a packet.
        FileOutputStream fos = null;
        File file = new File("image_" + String.valueOf(imageNo) + ".bin");
        Runtime rt = Runtime.getRuntime();
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(bytes);
            Process pr = rt.exec("ssdv -d image_" + String.valueOf(imageNo) + ".bin current.jpeg");
        } catch (FileNotFoundException e) {
            //Error here?
        } catch (IOException e) {
            //Error here?
        }
    }

}
