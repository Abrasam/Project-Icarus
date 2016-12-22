import com.sam.hab.payload.main.Config;
import com.sam.hab.payload.serial.GPSLoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        System.out.println(new Config().getFreq());
        System.out.println(new Config().getBandwidth());
        System.out.println(new Config().getCallsign());
        System.out.println(new Config().getCodingRate());
    }

}
