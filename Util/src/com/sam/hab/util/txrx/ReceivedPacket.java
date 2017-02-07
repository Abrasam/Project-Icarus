package com.sam.hab.util.txrx;

import com.sam.hab.util.lora.Constants.*;

public class ReceivedPacket {

    public final String raw;
    public final String data;
    public final int id;
    public final PacketType type;

    /**
     * Simple class, objects of which are generated automatically each time a 2-way communications packet is received.
     * @param raw the raw string of the packet.
     * @param data the data section of the packet.
     * @param id the id of the packet.
     * @param type the type of the packet.
     */
    public ReceivedPacket(String raw, String data, int id, PacketType type) {
        this.raw = raw;
        this.data = data;
        this.id = id;
        this.type = type;
    }
}