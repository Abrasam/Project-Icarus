import com.sam.hab.gui.GUI;
import com.sam.hab.lora.Constants.*;
import com.sam.hab.txrx.CycleManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class test {

    public static void main(String[] args) throws IOException, InterruptedException {
        JFrame frame = new JFrame("Prototype 2-Way HAB Comms");
        GUI gui = new GUI();
        gui.init();
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.setContentPane(gui.getPanelMain());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        CycleManager cm = new CycleManager(gui);
        cm.switchMode(Mode.RX);
    }
}
