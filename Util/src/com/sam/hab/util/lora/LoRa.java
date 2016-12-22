package com.sam.hab.util.lora;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import com.pi4j.wiringpi.Gpio;
import com.sam.hab.util.lora.Constants.*;
import com.sam.hab.util.txrx.CycleManager;

import java.io.IOException;
import java.util.Arrays;

public class LoRa {

    private double frequency;
    private Bandwidth bandwidth;
    private short spreadingFactor;
    private CodingRate codingRate;
    private boolean explicitHeader;
    private Mode mode;
    private SpiDevice spi = null;
    public final GpioController gpio;
    private final GpioPinDigitalInput dio0;
    private final CycleManager cm;
    private DIOMode dioMapping = DIOMode.RXDONE;
    private int transmitPtr;
    private String[] transmit;

    /**
     * Constructor for the LoRa interface class. Parameters are the modem settings for the radio.
     * This class encapsulates all the advanced LoRa register modification functionality, providing me, as the developer, with a simpler interface elsewhere in the program.
     * @param frequency
     * @param bandwidth
     * @param spreadingFactor
     * @param codingRate
     * @param explicitHeader
     */
    public LoRa(double frequency, Bandwidth bandwidth, short spreadingFactor, CodingRate codingRate, boolean explicitHeader, CycleManager cm) throws IOException {
        this.frequency = frequency;
        this.bandwidth = bandwidth;
        this.spreadingFactor = spreadingFactor;
        this.codingRate = codingRate;
        this.explicitHeader = explicitHeader;

        this.cm = cm;

        spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        gpio = GpioFactory.getInstance();

        dio0 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

        dio0.setShutdownOptions(true);
        dio0.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (event.getEdge() == PinEdge.RISING) {
                    try {
                        clearIRQFlags();
                    } catch (IOException e) {
                        //Error? Need to throw error here.
                    }
                    dio0();
                }
            }
        });

        setMode(Mode.SLEEP);

        setDIOMapping(DIOMode.RXDONE);

        setFrequency(frequency);
        setModemConfig(bandwidth, spreadingFactor, codingRate, explicitHeader);
        setPAConfig((short)5);
        clearIRQFlags();
    }

    /**
     * Called when DIO0 pin goes HIGH.
     */
    private void dio0() {
        if (this.dioMapping == DIOMode.TXDONE) {
            onTxDone();
        } else if (this.dioMapping == DIOMode.RXDONE) {
            onRxDone();
        }
    }

    /**
     * Method called when transmit done.
     */
    public void onTxDone() {
        transmitPtr++;
        if (transmitPtr > transmit.length - 1 || transmit[transmitPtr] == null) {
            try {
                cm.switchMode(Mode.RX);
            } catch (IOException e) {
                //Error here?
            }
            try {
                setMode(Mode.SLEEP);
            } catch (IOException e) {
                //Error here?
            }
        } else {
            try {
                writePayload(transmit[transmitPtr].getBytes());
                setMode(Mode.TX);
            } catch (IOException e) {
                //Error here?
            }

        }
    }

    /**
     * Called when packet is received.
     */
    public void onRxDone() {
        try {
            byte[] payload = readPayload();
            cm.addToRx(payload);
            resetRXPtr();
            setMode(Mode.RX);
        } catch (IOException e) {
            //Error?
        }
    }

    /**
     * Used for all writing of registers via the SPI interface.
     * @param reg The register to write to.
     * @param values A byte array of values to write to the regsister.
     * @throws IOException if write operation fails, for example if the radio is disconnected unexpectedly.
     */
    private void writeRegister(Register reg, byte... values) throws IOException {
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
        assert this.mode == Mode.SLEEP || this.mode == Mode.STDBY;
        int freq = (int)(frequency * (7110656 / 434));
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
            while (Gpio.digitalRead(2) != 1) {
                System.out.println(Gpio.digitalRead(2));
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
        System.out.println(reg);
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
     * @param payload Payload to send.
     */
    public void writePayload(byte[] payload) throws IOException {
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
    public void setPAConfig(short outputPower) throws IOException {
        byte val = (byte)0b10000000;
        val |= outputPower;
        writeRegister(Register.PACONFIG, val);
    }

    /**
     * Read a received payload from the radio.
     * @return
     * @throws IOException
     */
    public byte[] readPayload() throws IOException {
        int nbBytes = 0xFF & readRegister(Register.RXNBBYTES, 1)[1];
        byte fifoRxCurrentAddr = readRegister(Register.FIFORXCURRENTADDR, 1)[1];
        setFifoPointer(fifoRxCurrentAddr);
        byte[] payload = Arrays.copyOfRange(readRegister(Register.FIFO, nbBytes), 1, nbBytes+1);
        System.out.println(payload.length);
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
     * Set the radio to begin transmission of 10 strings.
     * @param transmitList
     * @throws IOException
     */
    public void send(String[] transmitList) throws IOException {
        setDIOMapping(DIOMode.TXDONE);
        writePayload(transmitList[0].getBytes());
        this.transmitPtr = 1;
        this.transmit = transmitList;
        setMode(Mode.TX);
    }
}