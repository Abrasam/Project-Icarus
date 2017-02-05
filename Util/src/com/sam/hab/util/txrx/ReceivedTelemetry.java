package com.sam.hab.util.txrx;

/**
 * Created by sam on 23/11/16.
 */
public class ReceivedTelemetry {

    public final String raw;
    public final float lat;
    public final float lon;
    public final float alt;
    public final long id;

    public ReceivedTelemetry(String raw, float lat, float lon, float alt, long id) {
        this.raw = raw;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.id = id;
    }
}
