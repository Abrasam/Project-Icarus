package com.sam.hab.util.txrx;

import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.csum.CRC16CCITT;
import sun.misc.CRC16;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PacketParser {

    /**
     * Simple algorithm to parse a two way packet, will first check the checksum is valid and that the correct key has been used then it will then return a new ReceivedPacket object with this packet's data.
     * @param raw The string of the packet received from the radio.
     * @param key The encryption key, set in config.
     * @return The ReceivedPacket object for this packet. Or null if the packet failed the checksum/key test.
     */
    public static ReceivedPacket parseTwoWay(String raw, String key) {
        String cSum = raw.split("\\*")[1].replace("\n", "");
        String packet = raw.split("\\*")[0].replace(">", "");;
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

    /**
     * Parses telemetry by splitting by comma, this works for the standard UKHAS format setup as $$CALLSIGN,ID,HH:MM:SS,LAT,LON,SATS*CSUM\n only.
     * @param raw The string of the telemetry taken from the radio.
     * @return The ReceivedTelemetry object for this telemetry string, this will then be sent to the server and the display.
     */
    public static ReceivedTelemetry parseTelemetry(String raw) {
        String cSum = raw.split("\\*")[1].replace("\n", "");
        String packet = raw.split("\\*")[0].replace("$", "");
        if (!CRC16CCITT.calcCsum((packet).getBytes()).equals(cSum)) {
            return null;
        }
        String packetList[] = packet.split(",");
        return new ReceivedTelemetry(raw, Float.valueOf(packetList[3]), Float.valueOf(packetList[4]), Float.valueOf(packetList[5]), Long.valueOf(packetList[1]));
    }

    /**
     * This parses SSDV data, it first stores the bytes received in the appropriate image file, the image number is the 7th byte in the array of each packet.
     * The ssdv program by fsphil is then run to decode the SSDV into a jpg image.
     * @param in The bytes of one SSDV packet.
     * @return The image number (i.e. 7th item in the input array) and the packet ID, also derived from the packet data.
     */
    public static int[] parseSSDV(byte[] in) {
        byte[] bytes = new byte[in.length+1];
        bytes[0] = 0x55;
        System.arraycopy(in, 0, bytes, 1, in.length);
        int imageNo = (0xFF & bytes[6]);
        int packetNo = (0xFF & bytes[7]) * 256 + (0xFF & bytes[8]);
        //TODO: Log here that I've received a packet.
        FileOutputStream fos = null;
        File file = new File("images/image_" + String.valueOf(imageNo) + ".bin");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Runtime rt = Runtime.getRuntime();
        try {
            file.createNewFile();
            fos = new FileOutputStream(file, true);
            fos.write(bytes);
            fos.close();
            Process pr = rt.exec("./ssdv -d images/image_" + String.valueOf(imageNo) + ".bin images/current.jpg");
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
