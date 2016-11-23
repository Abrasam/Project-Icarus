import com.sam.hab.txrx.PacketParser;
import com.sam.hab.txrx.ReceivedTelemetry;
import com.sam.hab.util.CRC16CCITT;

import java.io.IOException;

public class test {

    public static void main(String[] args) throws IOException, InterruptedException {
//        LoRa lora = new LoRa(869.850, Bandwidth.BW250, (short) 7, CodingRate.CR4_5, true) {
//            @Override
//            public void onTxDone() {
//                System.out.println("TXDONE!");
//                try {
//                    super.clearIRQFlags();
//                    super.setMode(Mode.STDBY);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onRxDone() {
//                System.out.println("Test.");
//            }
//        };
//        lora.clearIRQFlags();
//        lora.setDIOMapping(DIOMode.TXDONE);
//        lora.setMode(Mode.STDBY);
//        lora.writePayload("$$Kittens.".getBytes());
//        lora.setMode(Mode.TX);
//        try {
//            while (true) {
//                System.out.println(lora.getMode());
//                Thread.sleep(1000);
//            }
//        } catch (Exception e) {
//        }
    }
}
