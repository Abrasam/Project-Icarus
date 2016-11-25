package com.sam.hab.txrx;

import com.sam.hab.lora.Constants.*;
import com.sam.hab.main.IcarusMain;

import java.io.IOException;

import static com.sam.hab.lora.Constants.PacketType.*;

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
        switch (this.type) {
            case CMD:

                break;
        }
    }
}