package com.sam.hab.util.txrx;

import com.sam.hab.util.csum.CRC16CCITT;

import java.util.Arrays;

public class TwoWayPacketGenerator {

    public static String[] generateShellPackets(String callsign, String key, String[] response) {
        for (int i = 0; i < response.length; i++) {
            String pckt = ">>" + callsign + ",%s,1" + response[i];
            pckt += ("*" + CRC16CCITT.calcCsum((pckt+key).getBytes()));
            response[i] = pckt;
        }
        return response;
    }

    public static String generateStatPacket(String callsign, String key, String name, String stat) {
        String pckt = ">>" + callsign + ",%s,2," + name + "/" + stat;
        pckt += ("*" + CRC16CCITT.calcCsum((pckt+key).getBytes()));
        return pckt;
    }

    public static String generateCommand(String callsign, String key, String cmd) {
        String pckt = ">>" + callsign + ",%s,0," + cmd;
        pckt += ("*" + CRC16CCITT.calcCsum((pckt+key).getBytes()));
        return pckt;
    }
}
