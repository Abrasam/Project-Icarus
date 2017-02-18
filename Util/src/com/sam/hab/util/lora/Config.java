package com.sam.hab.util.lora;

import com.sam.hab.util.lora.Constants.*;
import com.sun.corba.se.impl.io.TypeMismatchException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private String callsign;
    private double freq;
    private double listen;
    private Bandwidth txbandwidth;
    private Bandwidth rxbandwidth;
    private short sf;
    private CodingRate codingRate;
    private String key;
    private boolean implicit;
    private int power; //TODO: implement changing power.

    public Config() {
        Yaml yaml = new Yaml();
        File f = new File("config.yml");
        try {
            if (!f.exists()) {
                f.createNewFile();
                FileWriter writer = new FileWriter(f);
                writer.write("callsign: TEST00\nkey: key123456\nfreq: 869.850\nlisten: 869.525\ntxbw: 250K\nrxbw: 62K5\nsf: 7\ncoding: 5\nimplicit: false\npower: 5");
                writer.close();
            }
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String s = "";
            String line = reader.readLine();
            while (line != null) {
                s += line + "\n";
                line = reader.readLine();
            }
            reader.close();
            Map<Object, Object> conf = (Map<Object, Object>)yaml.load(s);
            callsign = (String) conf.get("callsign");
            freq = (double) conf.get("freq");
            listen = (double)conf.get("listen");
            txbandwidth = Bandwidth.getBandwidth((String) conf.get("txbw"));
            rxbandwidth = Bandwidth.getBandwidth((String) conf.get("rxbw"));
            sf = (short) (int) conf.get("sf");
            codingRate = CodingRate.valueOf("CR4_" + String.valueOf(conf.get("coding")));
            implicit = (boolean) conf.get("implicit");
            power = (int)conf.get("power");
            key = (String)conf.get("key");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setListen(double listen) {
        this.listen = listen;
    }

    public void setTxbandwidth(Bandwidth txbandwidth) {
        this.txbandwidth = txbandwidth;
    }

    public void setRxbandwidth(Bandwidth rxbandwidth) {
        this.rxbandwidth = rxbandwidth;
    }

    public void setSf(short sf) {
        this.sf = sf;
    }

    public void setCodingRate(CodingRate codingRate) {
        this.codingRate = codingRate;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void save() {
        Yaml yaml = new Yaml();
        Map<Object, Object> conf = new HashMap<Object, Object>();
        conf.put("callsign", callsign);
        conf.put("freq", freq);
        conf.put("listen", listen);
        conf.put("txbw", Bandwidth.asString(txbandwidth));
        conf.put("rxbw", Bandwidth.asString(rxbandwidth));
        conf.put("sf", sf);
        conf.put("coding", codingRate.toString().replace("CR4_", ""));
        conf.put("implicit", implicit);
        conf.put("power", power);
        conf.put("key", key);
        File f = new File("config.yml");
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
            FileWriter writer = new FileWriter(f);
            yaml.dump(conf,writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCallsign() {
        return callsign;
    }

    public double getFreq() {
        return freq;
    }

    public Bandwidth getReceiveBandwidth() {
        return rxbandwidth;
    }

    public Bandwidth getTransmitBandwidth() {
        return txbandwidth;
    }

    public short getSf() {
        return sf;
    }

    public CodingRate getCodingRate() {
        return codingRate;
    }

    public String getKey() {
        return key;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public int getPower() {
        return power;
    }

    public double getListen() {
        return listen;
    }

    public boolean getImplicit() {
        return implicit;
    }
}