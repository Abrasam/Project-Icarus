package com.sam.hab.ground.web;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    String server = "http://212.250.101.219:8080/";

    /**
     * Sends telemetry to my server for forwarding to habitat tracker.
     * @param telem telemetry to send. Must be either ASCII or ISO 8859-1.
     */
    public void sendTelemetry(String telem) {
        try {
            URL url = new URL(server + "telemetryUpload");
            sendPut(url, telem);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends image packets to my server for forwarding to ssdv.habhub.org.
     * @param img image packet to send. Must be encoded in ISO 8859-1.
     */
    public void sendImage(String img) {
        try {
            URL url = new URL(server + "imageUpload");
            sendPut(url, img);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple method which sends a HTTP PUT request to the url supplied with the data supplied.
     * @param url the url to send to.
     * @param data the data to send.
     */
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

    /**
     * Uploads a 2-way packet to my server for logging in my marvellous database.
     * @param packet packet to log.
     */
    public void sendTwoWay(String packet) {
        try {
            URL url = new URL(server + "packetUpload");
            sendPut(url, packet);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getPayloadData(String callsign) {
        try {
            URL url = new URL(server + callsign);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String data = reader.readLine();
            reader.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}