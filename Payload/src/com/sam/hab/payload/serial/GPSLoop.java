package com.sam.hab.payload.serial;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.sam.hab.payload.main.PayloadMain;

import java.io.IOException;
import java.sql.Time;
import java.util.Arrays;

public class GPSLoop implements Runnable {
    SerialConfig config;
    Serial serial;

    //This is the array of bytes that needs to be sent to the GPS to put it into airborne mode.
    int[] airborneMode = new int[] {0xFF, 0xB5, 0x62, 0x06, 0x24, 0x24, 0x00, 0xFF, 0xFF, 0x06, 0x03, 0x00, 0x00, 0x00, 0x00, 0x10, 0x27, 0x00, 0x00, 0x05, 0x00, 0xFA, 0x00, 0xFA, 0x00, 0x64, 0x00, 0x2C, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0xDC};

    /**
     * Contains serial port initialisation, the GPS runs at 9600 baud with all settings as default.
     */
    public GPSLoop() {
        this.config = new SerialConfig();
        this.serial = SerialFactory.createInstance();
        try {
            this.config.device(SerialPort.getDefaultPort()).baud(Baud._9600);
            this.serial.open(this.config);
            if (!setAirborneMode(0)) {
                System.exit(-1);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * My GPS needs to be in airborne mode to bypass the COCOM limits that disable a GPS if it reaches a high altitude because it is assumed to be an ICBM.
     * This is a recursive algorithm that sets the GPS into airborne mode, it will continuously try 100 times recursively but if it fails after the 101th try then the program will exit.
     * This prevents me from starting a flight not in airborne mode.
     */
    private boolean setAirborneMode(int attempts) throws IOException, InterruptedException {
        for (int i = 0; i < airborneMode.length; i++) {
            this.serial.write((byte)airborneMode[i]);
        }
        Thread.sleep(1000);
        byte[] read = this.serial.read();
        String s = "";
        for (byte b : read) {
            s += (char)(0xFF & b);
        }
        if (s.contains("µb\u0005\u0001\u0002\u0000\u0006$2[")) {
            return true;
        } else if (attempts > 100) {
            return false;
        } else {
            return setAirborneMode(attempts + 1);
        }
    }

    /**
     * Main loop for the GPS runnable, simply continuously reads from the GPS until a newline character and then attempts to generate telemetry using that GPS data (GPS data is terminated by newline).
     */
    public void run() {
        String received = "";
        while (true) {
            try {
                if (this.serial.available() > 0) {
                    char c = (char)(0xFF & this.serial.read(1)[0]);
                    received = received + c;
                    if (c == '\n') {
                        PayloadMain.generateTelemetry(received);
                        this.serial.discardOutput();
                        received = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
