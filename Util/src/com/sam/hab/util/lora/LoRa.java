package com.sam.hab.util.lora;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import com.pi4j.wiringpi.Gpio;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.txrx.CycleManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LoRa {

    private double frequency;
    private Bandwidth bandwidth;
    private short spreadingFactor;
    private CodingRate codingRate;
    private boolean explicitHeader;
    private Mode mode;
    private final GpioController gpio;
    private SpiDevice spi = null;
    private DIOMode dioMapping = DIOMode.RXDONE;

    /**
     * Constructor for the LoRa interface class. Parameters are the modem settings for the radio.
     * This encapsulated class contains all the advanced LoRa register modification functionality, providing me, as the developer, with a simpler interface elsewhere in the program.
     * @param frequency frequency to set the radio to.
     * @param bandwidth bandwidth  to set the radio to.
     * @param spreadingFactor sf to set the radio to.
     * @param codingRate cr to set the radio to.
     * @param explicitHeader whether or not to use explicit headers (please use explicit headers!).
     */
    public LoRa(double frequency, Bandwidth bandwidth, short spreadingFactor, CodingRate codingRate, boolean explicitHeader) throws IOException {
        this.frequency = frequency;
        this.bandwidth = bandwidth;
        this.spreadingFactor = spreadingFactor;
        this.codingRate = codingRate;
        this.explicitHeader = explicitHeader;

        spi = SpiFactory.getInstance(SpiChannel.CS1, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        gpio = GpioFactory.getInstance();

        setMode(Mode.SLEEP);

        setDIOMapping(DIOMode.RXDONE);

        setFrequency(frequency);
        setModemConfig(bandwidth, spreadingFactor, codingRate, explicitHeader);
        setPAConfig((byte)0x08); //This is the default value which equates to ~10mW.
        clearIRQFlags();
    }

    /**
     * Called when packet is received.
     */
    public byte[] handlePacket() {
        try {
            byte[] payload = readPayload();
            resetRXPtr();
            setMode(Mode.RX);
            return payload;
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Used for all writing of registers via the SPI interface.
     * @param reg The register to write to.
     * @param values A byte array of values to write to the regsister.
     * @throws IOException if write operation fails, for example if the radio is disconnected unexpectedly.
     */
    private void writeRegister(Register reg, byte... values) throws IOException {
        System.out.println(reg.toString());
        byte[] send = new byte[values.length + 1];
        send[0] = (byte)(reg.addr | 0x80);
        System.arraycopy(values, 0, send, 1, values.length);
        System.out.println("W:" + Arrays.toString(send));
        spi.write(send);
    }

    /**
     * Read the value held in a register.
     * @param reg The register to read from.
     * @param nbBytes The number of bytes to read.
     * @return The value read from the register (a byte array).
     * @throws IOException
     */
    private byte[] readRegister(Register reg, int nbBytes) throws IOException {
        System.out.println(reg.toString());
        byte[] send = new byte[nbBytes + 1];
        send[0] = (byte)reg.addr;
        byte[] out = spi.write(send);
        System.out.println("R:" + Arrays.toString(out));
        return out;
    }

    /**
     * Set Frequency in MHz.
     * @param frequency Frequency to set the radio to.
     * @throws IOException
     */
    public void setFrequency(double frequency) throws IOException {
        assert this.mode == Mode.SLEEP || this.mode == Mode.STDBY; //Assertion used as these conditions should never not be true.
        int freq = (int)(frequency * (7110656 / 434));
        //This method separates the frequency into the 8 most significant bits, the 8 middle bits and the 8 least significant bits as they are stored in separate registers.
        writeRegister(Register.FRMSB, (byte)((freq >> 16) & 0xFF));
        writeRegister(Register.FRMID, (byte)((freq >> 8) & 0xFF));
        writeRegister(Register.FRLSB, (byte)(freq & 0xFF));
    }

    /**
     * Set the operation mode of the LoRa radio.
     * @param mode Mode to change to.
     * @throws IOException
     */
    public void setMode(Mode mode) throws IOException {
        if (this.mode != mode) {
            this.mode = mode;
        }
        writeRegister(Register.OPMODE, mode.val);
        if (this.mode != Mode.SLEEP) {
            long time = System.currentTimeMillis();
            while (Gpio.digitalRead(26) != 1) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() - time > 500) {
                    return;
                }
            }
            clearIRQFlags();
        }
    }

    /**
     * Returns to the user the operation mode that the radio is currently operating in.
     * @return The mode that the radio is currently operating in.
     * @throws IOException
     */
    public Mode getMode() throws IOException {
        byte reg = readRegister(Register.OPMODE, 1)[1];
        Mode mode = Mode.lookup(reg);
        if (mode != null) {
            this.mode = mode;
        }
        return mode;
    }

    /**
     * Used to configure modem settings, modifies the appropriate registers via the SPI interface. Can be used to edit bandwidth, spreading factor, coding rate and header mode.
     * @param bandwidth Bandwidth to set the radio to.
     * @param spreadingFactor New spreading factor.
     * @param codingRate New CRC error coding rate.
     * @param explicitHeader Whether to use explicit header mode.
     * @throws IOException
     */
    public void setModemConfig(Bandwidth bandwidth, short spreadingFactor, CodingRate codingRate, boolean explicitHeader) throws IOException {
        byte val1 = (byte)((explicitHeader ? 0 : 1) | (codingRate.val << 1) | bandwidth.val << 4);
        byte val2 = (byte)(0b00000000 | (spreadingFactor << 4));
        writeRegister(Register.MODEMCONFIG1, val1);
        writeRegister(Register.MODEMCONFIG2, val2);

    }

    /**
     * Used to write the packet to be sent to the LoRa FIFO.
     * @param str Payload to send. Must be a string encoded with ISO 8859-1.
     */
    public void writePayload(String str) throws IOException {
        byte[] payload = str.getBytes(StandardCharsets.ISO_8859_1);
        setMode(Mode.STDBY);
        if (payload.length < 256) {
            setPayloadLength((short)payload.length);
            byte baseAddr = readRegister(Register.FIFOTXBASEADDR, 1)[1];
            setFifoPointer(baseAddr);
            writeRegister(Register.FIFO, payload);
        }
    }

    /**
     * Set the pointer of the fifo to the given address.
     * @param ptr
     * @throws IOException
     */
    public void setFifoPointer(byte ptr) throws IOException {
        writeRegister(Register.FIFOADDRPOINTER, ptr);
    }

    /**
     * Used to set the packet length of the packet that is about to be written to the LoRa FIFO.
     * Must not exceed 255.
     * @param length
     * @throws IOException
     */
    public void setPayloadLength(short length) throws IOException {
        writeRegister(Register.PAYLOADLENGTH, (byte)length);
    }

    /**
     * Set the power output configuration, see documentation on power output limitations.
     * @param outputPower The output power register value.
     * @throws IOException
     */
    public void setPAConfig(byte outputPower) throws IOException {
        outputPower &= 0b00001111;
        //MSB is a 1 to set the PA_BOOST pin to HIGH. The 4 LSBs are the output power value. The other 3 bits need to be 0 for my implementation.
        byte val = (byte)0b10000000;
        val |= outputPower;
        writeRegister(Register.PACONFIG, val);
    }

    /**
     * Read a received payload from the radio.
     * @return the payload.
     * @throws IOException
     */
    public byte[] readPayload() throws IOException {
        int nbBytes = 0xFF & readRegister(Register.RXNBBYTES, 1)[1];
        byte fifoRxCurrentAddr = readRegister(Register.FIFORXCURRENTADDR, 1)[1];
        setFifoPointer(fifoRxCurrentAddr);
        byte[] payload = Arrays.copyOfRange(readRegister(Register.FIFO, nbBytes), 1, nbBytes+1);
        return payload;
    }

    /**
     * See below.
     * @return Array of booleans for IRQ flags in this order: txdone, rxdone.
     * @throws IOException
     */
    public boolean[] getIRQFlags() throws IOException {
        byte val = readRegister(Register.IRQFLAGS, 1)[1];
        boolean[] flags = new boolean[2];
        flags[0] = ((val >> 7) & 0x01) == 1;
        flags[1] = ((val >> 3) * 0x01) == 1;
        return flags;
    }

    /**
     * Clear all IRQ flags. This also stops the DIO pins from being triggered.
     * @throws IOException
     */
    public void clearIRQFlags() throws IOException {
        writeRegister(Register.IRQFLAGS, (byte)0);
    }

    /**
     * Set the dio mapping, takes a byte[] of length 6.
     * @param mode Mapping.
     * @throws IOException
     */
    public void setDIOMapping(DIOMode mode) throws IOException {
        this.dioMapping = mode;
        if (mode == DIOMode.RXDONE) {
            writeRegister(Register.DIOMAPPING1, (byte)0x00);
        } else if (mode == DIOMode.TXDONE) {
            writeRegister(Register.DIOMAPPING1, (byte)0x40);
        }
        writeRegister(Register.DIOMAPPING2, (byte)0x00);
    }

    /**
     *  Reset the pointer in the FIFO where received packets are stored.
     * @throws IOException
     * @throws InterruptedException
     */
    public void resetRXPtr() throws IOException {
        setMode(Mode.SLEEP);
        byte baseAddr = readRegister(Register.FIFORXBASEADDR, 1)[1];
        setFifoPointer(baseAddr);
        //setMode(Mode.STDBY);
    }

    /**
     * Check the state of the DIO0 pin.
     * @return whether the pin is at HIGH (true) or LOW (false).
     */
    public boolean pollDIO0() {
        return Gpio.digitalRead(27) == 1;
    }

    /**
     * Check the state of the DIO5 pin.
     * @return whether the pin is at HIGH (true) or LOW (false).
     */
    public boolean pollDIO5() {
        return Gpio.digitalRead(26) == 1;
    }

    /**
     * Set the radio to begin transmission of some packets.
     * @param transmitList array of packets to transmit.
     * @throws IOException
     */
    public void send(String[] transmitList) throws IOException {
        setDIOMapping(DIOMode.TXDONE);
        int transmitPtr = 0;
        while (transmitPtr < transmitList.length) {
            if (transmitList[transmitPtr] != null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writePayload(transmitList[transmitPtr]);
                setMode(Mode.TX);
            }
            transmitPtr++;
            long time = System.currentTimeMillis();
            while (Gpio.digitalRead(27) != 1) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() - time > 500) {
                    break;
                }
            }
            clearIRQFlags();
        }
    }

    @Override
    public String toString() {
        return "LoRa Module Setup:\nFrequency: " + frequency + "\nBandwidth: " + bandwidth.toString() + "\nSpreading Factor: " + spreadingFactor + "\nCoding Rate: " + codingRate.toString() + "\nExplicit Header: " + explicitHeader + "\nMode: " + mode.toString();
    }
}