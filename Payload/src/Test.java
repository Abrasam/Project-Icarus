import com.sam.hab.util.csum.CRC16CCITT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class Test {

    public static void main(String[] args) {
        System.out.println(CRC16CCITT.calcCsum("TEST00,1486216046,13:47:20,5149.56353,-00242.81798,104.5,10".getBytes(StandardCharsets.ISO_8859_1)));
    }
}
