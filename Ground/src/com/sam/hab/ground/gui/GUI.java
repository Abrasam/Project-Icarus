package com.sam.hab.ground.gui;

import com.sam.hab.ground.web.RequestHandler;
import com.sam.hab.util.lora.Config;
import com.sam.hab.util.lora.Constants;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;
import com.sam.hab.util.txrx.TwoWayPacketGenerator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GUI {
    private JTabbedPane tabbedPane1;
    private JTextArea rxcon;
    private JTextArea txcon;
    private JTextField lat;
    private JTextField lon;
    private JTextField alt;
    private JTextField lastpckt;
    private JTextField velv;
    private JLabel img;
    private JPanel ssdvPanel;
    private JPanel panelMain;
    private JTextArea consoleOutput;
    private JTextField consoleInput;
    private JButton rebootButton;
    private JTextArea controlResults;
    private JButton imageTransmit;
    private JButton noPicsStored;
    private JTextField timeSince;
    private JCheckBox uploadTelemetryCheckBox;
    private JCheckBox uploadImagesCheckBox;
    private JTextField callsign;
    private JButton autoconf;
    private JFormattedTextField key;

    public final CycleManager cm;
    private final File log;

    float lastAlt = 0;
    long lastTime = System.currentTimeMillis();
    long lastID = -1;

    private boolean uploadImage = true;
    private boolean uploadTelem = true;

    public GUI(Config conf) {

        //Prepare log file. This is timestamped so each launch has a new log file.
        log = new File("logs/" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt");
        try {
            if (!log.getParentFile().exists()) {
                log.getParentFile().mkdirs();
            }
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Init request handler.
        RequestHandler requestHandler = new RequestHandler();

        //Prepare the reboot button so it completes the correct action.
        rebootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "RBT"));
            }
        });

        //Prepares the consoleInput so that when enter is pressed it sends the given command.
        consoleInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = consoleInput.getText();
                if (cmd.length() > 0 && cmd.length() < 255 - 14 - conf.getCallsign().length() && !cmd.contains(",")) {
                    cm.addToTx(TwoWayPacketGenerator.generateShellCmdPacket(conf.getCallsign(), cmd));
                    consoleInput.setBackground(Color.GREEN);
                    consoleInput.setText("");
                } else {
                    consoleInput.setBackground(Color.RED);
                }
            }
        });

        autoconf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cs = callsign.getText();
                String ky = key.getText();
                String data = requestHandler.getPayloadData(cs);
                String[] payload = data.split(",");
                if (payload.length == 8) {
                    String callsgn = payload[0];
                    String txFreq = payload[1];
                    int txBw = Integer.valueOf(payload[2]);
                    String sf = payload[3];
                    String coding = payload[4];
                    boolean explicit = payload[5].equals("1");
                    String rxFreq = payload[6];
                    int rxBw = Integer.valueOf(payload[7]);
                    conf.setCallsign(callsgn);
                    conf.setListen(Double.valueOf(txFreq));
                    conf.setRxbandwidth(Constants.Bandwidth.lookup(txBw));
                    conf.setSf(Short.valueOf(sf));
                    conf.setCodingRate(Constants.CodingRate.valueOf("CR4_" + coding));
                    conf.setImplicit(!explicit);
                    conf.setFreq(Double.valueOf(rxFreq));
                    conf.setTxbandwidth(Constants.Bandwidth.lookup(rxBw));
                    conf.setKey(ky);
                    conf.save();
                    JOptionPane.showMessageDialog(panelMain, "Success! Configured for " + callsgn + ". Please restart the program for changes to take effect.");
                } else {
                    JOptionPane.showMessageDialog(panelMain, "Autoconfigure failed, please edit config.yml and restart the program.");
                }
            }
        });

        //Prepares the image transmit toggle button so when clicked it prepares a packet to send which toggles image transmission.
        imageTransmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "IMG"));
            }
        });

        //Prepares this button so when clicked it prepares a packet which will ask the payload to transmit the number of stored images.
        noPicsStored.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "IMGNO"));
            }
        });

        //Init cycle manager.
        cm = new CycleManager(false, conf.getCallsign(), new double[] {conf.getFreq(), conf.getListen()}, new Constants.Bandwidth[] {conf.getTransmitBandwidth(), conf.getReceiveBandwidth()}, conf.getSf(), conf.getCodingRate(), !conf.getImplicit(), conf.getKey()) {
            @Override
            public void handleTelemetry(ReceivedTelemetry telem) {
                if (telem == null) {
                    return;
                }
                getAlt().setText(String.valueOf(telem.alt));
                getLat().setText(String.valueOf(telem.lat));
                getLon().setText(String.valueOf(telem.lon));
                writeRx(telem.raw);
                if (telem.id != lastID) {
                    getLastpckt().setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    updateVelocities(telem.alt);
                    lastID = telem.id;
                    if (uploadTelem) {
                        requestHandler.sendTelemetry(telem.raw);
                    }
                }
            }

            @Override
            public void onSend(String sent) {
                if (sent != null) {
                    writeTx(sent);
                }
            }

            @Override
            public void handleImage(byte[] bytes, int iID, int pID) {
                writeRx("Image no. " + iID + " packet no. " + pID + " received.\n");
                if (uploadImage) {
                    requestHandler.sendImage(new String(bytes, StandardCharsets.ISO_8859_1));
                }
            }

            @Override
            public void handleTwoWay(ReceivedPacket packet) {
                if (packet == null) {
                    return;
                    //TODO: LOGIC HERE FOR NACK PERHAPS!
                }
                writeRx(packet.raw);
                switch (packet.type) {
                    case CMD:
                        if (packet.data.equals("TRA")) {
                            this.txInterrupt();
                        }
                        break;
                    case SHELL:
                        getConsoleOutput().append(packet.data.replace((char)0, '\n'));
                        break;
                    case DIAG:
                        String[] data = packet.data.split("/");
                        getControlResults().append(data[0] + ": " + data[1] + "\n");
                        break;
                    case NACK:
                        String[] ids = packet.data.split("/");
                        String[] transmitted = getTransmitted();
                        for (String id : Arrays.asList(ids)) {
                            try {
                                addToTx(transmitted[Integer.parseInt(String.valueOf(id))]);
                            } catch (NumberFormatException e) {
                                //Error?
                            } catch (ArrayIndexOutOfBoundsException e) {
                                //Error?
                            }
                        }
                        break;
                    case OTHER:
                        //Not really sure what to do here? How about you?
                }
                requestHandler.sendTwoWay(packet.raw);
            }

            @Override
            public String getTelemetry() {
                return null;
            }

            @Override
            public String getImagePacket() {
                return null;
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    long diff = System.currentTimeMillis() - lastTime;
                    diff /= 1000f;
                    timeSince.setText(diff + "s");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //Upload checboxes.
        uploadImagesCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    uploadImage = true;
                } else if (e.getStateChange() == ItemEvent.DESELECTED){
                    uploadImage = false;
                }
            }
        });
        uploadTelemetryCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    uploadTelem = true;
                } else if (e.getStateChange() == ItemEvent.DESELECTED){
                    uploadTelem = false;
                }
            }
        });
    }

    //Simple sets all the GUI elements to be formatted correctly.
    public void init() {
        rxcon.setLineWrap(true);
        txcon.setLineWrap(true);
        consoleOutput.setLineWrap(true);
        controlResults.setLineWrap(true);
        ((DefaultCaret)rxcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)txcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)consoleOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)controlResults.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        consoleOutput.setFont(new Font("monospaced", Font.PLAIN, 12));
        consoleInput.setFont(new Font("monospaced", Font.PLAIN, 12));
        controlResults.setFont(new Font("monospaced", Font.PLAIN, 12));
        rxcon.setFont(new Font("monospaced", Font.PLAIN, 12));
        txcon.setFont(new Font("monospaced", Font.PLAIN, 12));
    }

    public JTextArea getControlResults() {
        return controlResults;
    }

    public JLabel getImg() {
        return img;
    }

    public JPanel getPanelMain() {
        return panelMain;
    }

    public JTextField getLat() {
        return lat;
    }

    public JTextField getLon() {
        return lon;
    }

    public JTextField getAlt() {
        return alt;
    }

    public JTextField getLastpckt() {
        return lastpckt;
    }

    public JPanel getSsdvPanel() {

        return ssdvPanel;
    }

    /**
     * Simple method which writes the received given data to the UI and the log file.
     * @param write string to write.
     */
    public void writeRx(String write) {
        write = write.replace((char)0, '\n');
        rxcon.append("->: " + write);
        try {
            FileWriter writer = new FileWriter(log, true);
            DateFormat format = new SimpleDateFormat("HH:mm:ss");
            writer.write("RX [" + format.format(new Date()) +"]: " + write);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple method which writes the transmitted given data to the UI and the log file.
     * @param write string to write.
     */
    public void writeTx(String write) {
        write = write.replace((char)0, '\n');
        txcon.append("<-:" + write);
        try {
            FileWriter writer = new FileWriter(log, true);
            DateFormat format = new SimpleDateFormat("HH:mm:ss");
            writer.write("TX [" + format.format(new Date()) +"]: " + write);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JTextArea getConsoleOutput() {
        return consoleOutput;
    }

    /**
     * Updates the vertical velocity field. This is an estimate only.
     * @param alt new altitude.
     */
    private void updateVelocities(float alt) {
        float altSpeed = (alt - lastAlt)/((System.currentTimeMillis() - lastTime)/1000f);
        altSpeed = (int)(altSpeed * 10) / 10f;
        velv.setText(String.valueOf(altSpeed) + " m/s");
        lastTime = System.currentTimeMillis();
        lastAlt = alt;
    }
}