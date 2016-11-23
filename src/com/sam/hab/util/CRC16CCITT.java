package com.sam.hab.util;

public class CRC16CCITT {

    //Simple algorithm to generate a 16 bit Cyclic Redundancy Check checksum.
    //Based on an example from Stack Overflow.

    public static String calcCsum(byte[] val) {
        int crc = 0xFFFF;
        int poly = 0x1021; //Implies 0001 0000 0010 0001 or 0 5 12 in denary.

        for (byte b : val) {
            for (int i = 0; i < 8; i++) {
                boolean bt = ((b >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<=1;
                if (c15 ^ bt) crc ^= poly;
            }
        }

        crc&=0xFFFF;
        return Integer.toHexString(crc).toUpperCase();
    }
}
