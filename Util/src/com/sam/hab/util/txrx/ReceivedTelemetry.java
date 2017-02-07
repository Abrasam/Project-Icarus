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

    /**
     * Simple class, objects of which are generated automatically each time a telemetry sentence is received.
     * @param raw the raw sentence.
     * @param lat the latitude which was included in the sentence.
     * @param lon the longitude which was included in the sentence.
     * @param alt the altitude which was in that sentence.
     * @param id the id of the sentence, should be number of seconds since 1/1/1970 at the time the packet was transmitted.
     */
    public ReceivedTelemetry(String raw, float lat, float lon, float alt, long id) {
        this.raw = raw;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.id = id;
    }
}
