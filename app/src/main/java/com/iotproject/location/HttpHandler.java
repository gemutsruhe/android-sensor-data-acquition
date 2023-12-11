package com.iotproject.location;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class HttpHandler {

    /*public String makeHttpRequest(String urlString) {
        String result = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 연결 설정
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            // 연결 시작
            conn.connect();

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 응답을 읽어옴
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // 결과를 문자열로 변환
                result = stringBuilder.toString();

                // 연결 해제
                in.close();
            }

            // 연결 해제
            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void sendDataToServer(String data) {
        try {
            // 데이터를 인코딩하여 URL에 추가
            String encodedData = URLEncoder.encode(data, "UTF-8");
            String serverUrl = "http://192.168.0.100:3000/api/data?data=" + encodedData;

            // AsyncTask 실행
            new SendDataToServerTask().execute(serverUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SendDataToServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            return makeHttpRequest(url);
        }

        @Override
        protected void onPostExecute(String result) {
            // 서버 응답 처리
            Log.d("HTTP_RESULT", result);
        }
    }*/
}
