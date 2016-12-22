package com.sam.hab.payload.serial;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.sam.hab.payload.main.PayloadMain;

import java.io.IOException;

public class GPSLoop
        implements Runnable
{
    SerialConfig config;
    Serial serial;

    public GPSLoop()
    {
        this.config = new SerialConfig();
        this.serial = SerialFactory.createInstance();
        try
        {
            this.config.device(SerialPort.getDefaultPort()).baud(Baud._9600);
            this.serial.open(this.config);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        String received = "";
        for (;;)
        {
            try
            {
                if (this.serial.available() > 0)
                {
                    char c = (char)(0xFF & this.serial.read(1)[0]);
                    received = received + c;
                    if (c == '\n')
                    {
                        PayloadMain.generateTelemetry(received);
                        this.serial.discardOutput();
                        received = "";
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                Thread.sleep(10L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
