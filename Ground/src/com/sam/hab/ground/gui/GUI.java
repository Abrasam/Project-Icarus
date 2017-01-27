package com.sam.hab.ground.gui;

import com.sam.hab.util.lora.Config;
import com.sam.hab.util.txrx.CycleManager;
import com.sam.hab.util.txrx.ReceivedPacket;
import com.sam.hab.util.txrx.ReceivedTelemetry;
import com.sam.hab.util.txrx.TwoWayPacketGenerator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Calendar;

public class GUI {
    private JTabbedPane tabbedPane1;
    private JTextArea rxcon;
    private JTextArea txcon;
    private JTextField lat;
    private JTextField lon;
    private JTextField alt;
    private JTextField lastpckt;
    private JTextField recvrs;
    private JTextField velv;
    private JTextField velh;
    private JLabel img;
    private JPanel ssdvPanel;
    private JPanel panelMain;
    private JTextArea consoleOutput;
    private JTextField consoleInput;
    private JButton rebootButton;
    private JTextArea controlResults;
    private JButton fullDownload;
    private JButton noPicsStored;

    public final CycleManager cm;

    public GUI(Config conf) {

        rebootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "RBT"));
            }
        });

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

        fullDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "IMG"));
            }
        });

        noPicsStored.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.addToTx(TwoWayPacketGenerator.generateCommand(conf.getCallsign(), "IMGNO"));
            }
        });

        Calendar cal = Calendar.getInstance();

        cm = new CycleManager(false, conf.getCallsign(), new double[] {conf.getFreq(), conf.getListen()}, conf.getBandwidth(), conf.getSf(), conf.getCodingRate(), !conf.getImplicit(), conf.getKey()) {
            @Override
            public void handleTelemetry(ReceivedTelemetry telem) {
                if (telem == null) {
                    return;
                }
                getAlt().setText(String.valueOf(telem.alt));
                getLat().setText(String.valueOf(telem.lat));
                getLon().setText(String.valueOf(telem.lon));
                getLastpckt().setText(cal.getTime().toString());
                writeRx(telem.raw);
            }

            @Override
            public void onSend(String sent) {
                if (sent != null) {
                    writeTx(sent);
                }
            }

            @Override
            public void handleImage(int iID, int pID) {
                writeRx("Image no. " + iID + " packet no. " + pID + " received.\n");
            }

            @Override
            public void handle2Way(ReceivedPacket packet) {
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
    }
    
    public void init() {
        rxcon.setLineWrap(true);
        txcon.setLineWrap(true);
        consoleOutput.setLineWrap(true);
        ((DefaultCaret)rxcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)txcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)consoleOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    public JTextArea getControlResults() {
        return controlResults;
    }

    public JLabel getImg() {
        return img;
    }

    public JTabbedPane getTabbedPane1() {
        return tabbedPane1;
    }

    public JPanel getPanelMain() {
        return panelMain;
    }

    public JTextArea getRxcon() {
        return rxcon;
    }

    public JTextArea getTxcon() {
        return txcon;
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

    public JTextField getRecvrs() {
        return recvrs;
    }

    public JTextField getVelv() {
        return velv;
    }

    public JTextField getVelh() {
        return velh;
    }

    public JPanel getSsdvPanel() {

        return ssdvPanel;
    }

    public void writeRx(String write) {
        rxcon.append("->: " + write);
    }

    public void writeTx(String write) {
        txcon.append("<-:" + write);
    }

    public JTextArea getConsoleOutput() {
        return consoleOutput;
    }

    public JTextField getConsoleInput() {
        return consoleInput;
    }
}
