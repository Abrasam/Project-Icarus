/**
 * Created by Samuel on 12/03/2017.
 */
public class Test {

    public static void main(String[] args) {
        System.out.println(toDeg(5149.5737f));
    }

    private static Double toDeg(float nmea) {
        boolean neg = false;
        if (nmea < 0) {
            neg = true;
            nmea = Math.abs(nmea);
        }
        String in = Float.toString(nmea);
        String[] data = in.split("\\.");
        return (int)(1000000*(Double.valueOf(data[0].substring(0,data[0].length()-2)) + Double.valueOf(data[0].substring(data[0].length()-2) + "." + data[1])/60d))/1000000d * (neg ? -1 : 1);
    }
}
