import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants;
import com.sam.hab.util.lora.LoRa;
import com.sun.xml.internal.bind.api.impl.NameConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        byte[] bytes = new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        System.out.println(new String(bytes, StandardCharsets.ISO_8859_1));
        System.out.println(Arrays.toString(bytes));
        System.out.println(Arrays.toString(new String(bytes, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1)));
    }

}
