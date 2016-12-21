package com.sam.hab.txrx;

import java.util.Date;

/**
 * Created by sam on 23/11/16.
 */
public class ReceivedTelemetry {

    public final String raw;
    public final float lat;
    public final float lon;
    public final float alt;
    public final float id;

    public ReceivedTelemetry(String raw, float lat, float lon, float alt, float id) {
        this.raw = raw;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.id = id;
    }
}
