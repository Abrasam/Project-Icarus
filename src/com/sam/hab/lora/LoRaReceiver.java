package com.sam.hab.lora;

import com.sam.hab.main.IcarusMain;

import java.io.IOException;

/**
 * Created by sam on 25/11/16.
 */
public class LoRaReceiver extends LoRa {

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
    public LoRaReceiver(double frequency, Constants.Bandwidth bandwidth, short spreadingFactor, Constants.CodingRate codingRate, boolean explicitHeader) throws IOException {
        super(frequency, bandwidth, spreadingFactor, codingRate, explicitHeader);
    }

    @Override
    public void onTxDone() {
        return;
    }

    @Override
    public void onRxDone() {
        try {
            byte[] payload = super.readPayload();
            IcarusMain.cycleManager.addToRx(payload);
        } catch (IOException e) {
            //Error?
        }
    }

}
