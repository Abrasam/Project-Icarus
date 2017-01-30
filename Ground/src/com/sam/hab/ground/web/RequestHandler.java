package com.sam.hab.ground.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestHandler {

    URL url;

    public RequestHandler() {
        try {
            url = new URL("http://212.250.101.219:80/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void sendPut(String data) {
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
