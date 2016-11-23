package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;

public class ReceivedPacket {

    public final String raw;
    public final String data;
    public final int id;
    public final PacketType type;

    public ReceivedPacket(String raw, String data, int id, PacketType type) {
        this.raw = raw;
        this.data = data;
        this.id = id;
        this.type = type;
    }

    public void handle() {

    }
}