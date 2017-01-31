package com.sam.hab.ground.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    String server = "http://192.168.250.172:80/";

    public void sendTelemetry(String telem) {
        try {
            URL url = new URL(server + "telemetryUpload");
            sendPut(url, telem);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(String img) {
        try {
            URL url = new URL(server + "imageUpload");
            sendPut(url, img);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void sendPut(URL url, String data) {
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(data.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            out.close();
            connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
