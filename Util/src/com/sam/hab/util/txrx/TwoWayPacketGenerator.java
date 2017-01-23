package com.sam.hab.util.txrx;

import com.sam.hab.util.csum.CRC16CCITT;

import java.util.Arrays;

public class TwoWayPacketGenerator {

    public static String[] generateShellPackets(String callsign, String[] response) {
        for (int i = 0; i < response.length; i++) {
            String pckt = ">>" + callsign + ",%s,1," + response[i];
            response[i] = pckt;
        }
        return response;
    }

    public static String generateShellCmdPacket(String callsign, String cmd) {
        return ">>" + callsign + ",%s,1," + cmd;
    }

    public static String generateStatPacket(String callsign, String name, String stat) {
        return ">>" + callsign + ",%s,2," + name + "/" + stat;
    }

    public static String generateCommand(String callsign, String cmd) {
        return ">>" + callsign + ",%s,0," + cmd;
    }
}
