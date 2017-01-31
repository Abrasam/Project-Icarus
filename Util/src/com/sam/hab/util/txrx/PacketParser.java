package com.sam.hab.util.txrx;

import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.csum.CRC16CCITT;
import sun.misc.CRC16;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PacketParser {

    public static ReceivedPacket parseTwoWay(String raw, String key) {
        String cSum = raw.split("\\*")[1].replace("\n", "");
        String packet = raw.split("\\*")[0].replace(">", "");;
        //packet = packet.replace(">", "");
        if (!CRC16CCITT.calcCsum((packet.replace(">", "") + key).getBytes()).equals(cSum)) {
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
        String packet = raw.split("\\*")[0].replace("$", "");
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
        int imageNo = (0xFF & bytes[6]);
        int packetNo = (0xFF & bytes[7]) * 256 + (0xFF & bytes[8]);
        //TODO: Log here that I've received a packet.
        FileOutputStream fos = null;
        File file = new File("image_" + String.valueOf(imageNo) + ".bin");
        Runtime rt = Runtime.getRuntime();
        try {
            file.createNewFile();
            fos = new FileOutputStream(file, true);
            fos.write(bytes);
            fos.close();
            Process pr = rt.exec("./ssdv -d image_" + String.valueOf(imageNo) + ".bin current.jpg");
            pr.waitFor();
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
