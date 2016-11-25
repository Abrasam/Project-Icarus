package com.sam.hab.lora;

import java.io.IOException;
import com.sam.hab.lora.Constants.*;

public class LoRaTransmitter extends LoRa {

    private String[] transmit;
    private int transmitPtr;

    /**
     * Constructor for the LoRa interface class. Parameters are the modem settings for the radio.
     *
     * @param frequency
     * @param bandwidth
     * @param spreadingFactor
     * @param codingRate
     * @param explicitHeader
     */
    public LoRaTransmitter(double frequency, Constants.Bandwidth bandwidth, short spreadingFactor, Constants.CodingRate codingRate, boolean explicitHeader) throws IOException {
        super(frequency, bandwidth, spreadingFactor, codingRate, explicitHeader);
    }

    public void send(String[] transmitList) throws IOException {
        super.setDIOMapping(DIOMode.TXDONE);
        super.writePayload(transmitList[0].getBytes());
        this.transmitPtr = 1;
        this.transmit = transmitList;
        super.setMode(Mode.TX);
    }

    @Override
    public void onTxDone() {
        transmitPtr++;
        if (transmitPtr > transmit.length - 1) {

            try {
                super.setMode(Mode.SLEEP);
            } catch (IOException e) {
                //Error here?
            }
        } else {
            try {
                super.writePayload(transmit[transmitPtr].getBytes());
                super.setMode(Mode.TX);
            } catch (IOException e) {
                //Error here?
           }

        }
    }

    @Override
    public void onRxDone() {
        return;
    }

}
