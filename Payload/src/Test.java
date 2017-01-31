import com.sam.hab.util.csum.CRC16CCITT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class Test {

    public static void main(String[] args) {
        System.out.println(CRC16CCITT.calcCsum("TEST00,1485866417,12:25:43,5148.70857,-00242.73113,26.0,06".getBytes()));
    }
}
