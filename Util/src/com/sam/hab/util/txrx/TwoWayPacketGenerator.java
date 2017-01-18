package com.sam.hab.util.txrx;

import com.sam.hab.util.csum.CRC16CCITT;

import java.util.Arrays;

public class TwoWayPacketGenerator {

    public static String[] generateShellPackets(String callsign, String key, String[] response) {
        for (int i = 0; i < response.length; i++) {
            String pckt = ">>" + callsign + ",%s,1" + response[i];
            response[i] = pckt;
        }
        return response;
    }

    public static String generateStatPacket(String callsign, String key, String name, String stat) {
        String pckt = ">>" + callsign + ",%s,2," + name + "/" + stat;
        return pckt;
    }

    public static String generateCommand(String callsign, String key, String cmd) {
        String pckt = ">>" + callsign + ",%s,0," + cmd;
        return pckt;
    }
}
