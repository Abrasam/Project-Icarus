import com.sam.hab.util.lora.Config;

public class Test {

    public static void main(String[] args) {
        System.out.println(new Config().getFreq());
        System.out.println(new Config().getBandwidth());
        System.out.println(new Config().getCallsign());
        System.out.println(new Config().getCodingRate());
    }

}
