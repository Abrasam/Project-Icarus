import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) {
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("echo wibble");
            pr.waitFor(5, TimeUnit.SECONDS);
            BufferedReader is = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = is.readLine();
            while (line != null) {
                System.out.println(line);
                line = is.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
