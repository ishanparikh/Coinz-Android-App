package com.example.ishan.coinzapp;


import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

    public class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            try {
                return loadFileFromNetwork(urls[0]);
            } catch (IOException e) {
                return "Unable to load content. Check your network connection";
            }
        }


        private String loadFileFromNetwork(String urlString) throws IOException {
            return readStream(downloadUrl(new URL(urlString)));

        }


        // Given a string representation of a URL, sets up a connection and gets an input stream.
        private InputStream downloadUrl(URL url) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            return conn.getInputStream();
        }

        @NonNull
        private String readStream(InputStream stream)
                throws IOException {
            String reader = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));

//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//            reader.close();
//            return sb.toString();
//            // Read input from stream, build result as a string

            return reader;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new DownloadCompleteRunner().downloadComplete(result);
        }
    } // end class DownloadFileTask
