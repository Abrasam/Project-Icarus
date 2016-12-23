package com.sam.hab.util.lora;

/**
 * A utility class to store all me constants! These are used for the LoRa radio operation, as it uses an SPI interface most data is sent in the form of a few bytes.
 */
public class Constants {

    public enum Register {
        FIFO(0x00),
        OPMODE(0x01),
        FRMSB(0x06),
        FRMID(0x07),
        FRLSB(0x08),
        PACONFIG(0x09),
        FIFOADDRPOINTER(0x0D),
        FIFOTXBASEADDR(0x0E),
        FIFORXBASEADDR(0x0F),
        FIFORXCURRENTADDR(0x10),
        IRQFLAGS(0x12),
        RXNBBYTES(0x13),
        MODEMCONFIG1(0x1D),
        MODEMCONFIG2(0x1E),
        PAYLOADLENGTH(0x22),
        DIOMAPPING1(0x40),
        DIOMAPPING2(0x41);

        public final byte addr;

        private Register(int addr) {
            this.addr = (byte) addr;
        }
    }

    public enum DIOMode {
        TXDONE,
        RXDONE;
    }

    public enum Mode {
        SLEEP(0x80),
        STDBY(0x81),
        TX(0x83),
        RX(0x85);

        public final byte val;

        private Mode(int val) {
            this.val = (byte) val;
        }

        public static Mode lookup(byte val) {
            for (Mode mode : Mode.values()) {
                if (mode.val == val) {
                    return mode;
                }
            }
            return null;
        }

    }

    public enum Bandwidth {
        BW7_8(0),
        BW10_4(1),
        BW15_6(2),
        BW20_8(3),
        BW31_25(4),
        BW41_7(5),
        BW62_5(6),
        BW125(7),
        BW250(8),
        BW500(9);

        public final byte val;

        private Bandwidth(int val) {
            this.val = (byte)val;
        }

        public static Bandwidth getBandwidth(String bw) {
            switch (bw) {
                case "7K8":
                    return BW7_8;
                case "10K4":
                    return BW10_4;
                case "15K6":
                    return BW15_6;
                case "20K8":
                    return BW20_8;
                case "21K25":
                    return BW31_25;
                case "41K7":
                    return BW41_7;
                case "62K5":
                    return BW62_5;
                case "125K":
                    return BW125;
                case "250K":
                    return BW250;
                case "500K":
                    return BW500;
            }
            return null;
        }

        public static String asString(Bandwidth bandwidth) {
            switch (bandwidth) {
                case BW7_8:
                    return "7K8";
                case BW10_4:
                    return "10K4";
                case BW15_6:
                    return "15K6";
                case BW20_8:
                    return "20K8";
                case BW31_25:
                    return "21K25";
                case BW41_7:
                    return "41K7";
                case BW62_5:
                    return "62K5";
                case BW125:
                    return "125K";
                case BW250:
                    return "250K";
                case BW500:
                    return "500K";
            }
            return null;
        }
    }

    public enum CodingRate {
        CR4_5(1),
        CR4_6(2),
        CR4_7(3),
        CR4_8(4);

        public final byte val;

        private CodingRate(int val) {
            this.val = (byte)val;
        }
    }

    public enum PacketType {
        CMD(0),
        SHELL(1),
        DIAG(2),
        NACK(3),
        RESEND(4),
        OTHER(5);

        public final int id;

        private PacketType(int id) {
            this.id = id;
        }

        public static PacketType lookup(int i) {
            for (PacketType type : PacketType.values()) {
                if (type.id == i) {
                    return type;
                }
            }
            return null;
        }
    }
}
