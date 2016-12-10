package com.sam.hab.gui;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

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

    public void init() {
        rxcon.setLineWrap(true);
        txcon.setLineWrap(true);
        ((DefaultCaret)rxcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ((DefaultCaret)txcon.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
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
        rxcon.append(write);
    }

    public void writeTx(String write) {
        txcon.append(write);
    }
}
