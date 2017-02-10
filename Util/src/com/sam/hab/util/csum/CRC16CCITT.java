package com.sam.hab.util.csum;

public class CRC16CCITT {

    /**
     * Algorithm to generate a 16 bit Cyclic Redundancy Check checksum/hash. This implementation uses polynomial 0x1012 and start value 0xFFFF.
     * @param val the byte array of data that a checksum is to be calculated for.
     * @return the checksum, in capital hexadecimal notation, i.e. EE56 would be a possible checksum.
     */
    public static String calcCsum(byte[] val) {
        int crc = 0xFFFF;
        int poly = 0x1021; //Implies polynomial 0001 0000 0010 0001.

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