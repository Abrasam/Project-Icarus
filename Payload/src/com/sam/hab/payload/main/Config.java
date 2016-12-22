package com.sam.hab.payload.main;

import com.sam.hab.util.lora.Constants.*;
import com.sun.corba.se.impl.io.TypeMismatchException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Config {

    private String callsign;
    private double freq;
    private Bandwidth bandwidth;
    private int sf;
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
                writer.write("callsign: TEST00\nkey: notakey\nfreq: 869.850\nbw: 250K\nsf: 7\ncoding: 5\nimplicit: false\npower: 5");
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
            bandwidth = Bandwidth.getBandwidth((String) conf.get("bw"));
            sf = (int) conf.get("sf");
            codingRate = CodingRate.valueOf("CR4_" + String.valueOf(conf.get("coding")));
            implicit = (boolean) conf.get("implicit");
            power = (int)conf.get("power");
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

    public Bandwidth getBandwidth() {
        return bandwidth;
    }

    public int getSf() {
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
}
