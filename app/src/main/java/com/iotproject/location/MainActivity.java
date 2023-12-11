package com.iotproject.location;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button findTableButton;
    Handler handler = new Handler();
    Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        findTableButton = findViewById(R.id.findTableButton);
        findTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SensorReceiver sensorReceiver = new SensorReceiver(context);
                    }
                }).start();
            }
        });

        /*String serverUrl = "  ";
        Observable<String> observable = Observable.create(emitter -> {
            try {
                String result = (new HttpHandler()).makeHttpRequest(serverUrl);
                emitter
            }
        });*/
    }

    /*public boolean getUserInfo(final Context context) {

        try {
            OkHttpClient client = new OkHttpClient();

            String url = SERVER_CONFIGURATION.ADDRESS + ":" +
                    SERVER_CONFIGURATION.PORT + "/v1/userinfo";

            Request request = new Request.Builder()
                    .addHeader("Authorization", "TEST AUTH")
                    .url(url)
                    .build();
            Response response = client.newCall(request)
                    .execute();

            String result = response.body().string();

            Gson gson = new Gson();
            UserInfo info = gson.fromJson(result, UserInfo.class);

            Log.i("id: " + info.id);
            Log.i("name: " + info.name);

            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }*/
/*
    public void printClientLog(final String data) {
        Log.d("MainActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.append(data + "\n");
            }
        });
    }

    public void printServerLog(final String data) {
        Log.d("MainActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                textView2.append(data + "\n");
            }
        });
    }

    public void send(String data) {
        try{
            int portNum = 5001;
            Socket sock = new Socket("localhost", portNum);
            printClientLog("소켓 연결함");

            ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
            outStream.writeObject(data);
            outStream.flush();
            printClientLog("데이터 전송함");

            ObjectInputStream insTream = new ObjectInputStream(sock.getInputStream());
            printClientLog("서버로부터 받음 : " + insTream.readObject());
            sock.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startServer() {
        try {
            int portNumber = 5001;

            ServerSocket server = new ServerSocket(portNumber);
            printServerLog("서버 시작함: " + portNumber);

            while (true) { // 클라이언트 접속 대기
                Socket sock = server.accept(); //접속 요청 오면 accept 메서드를 통해 소켓 객체 반환
                InetAddress clientHost = sock.getLocalAddress(); // 클라이언트 연결 정보 확인 가능
                int clientPort = sock.getPort(); // 클라이언트 포트 번호 확인
                printServerLog("클라이언트 연결됨: " + clientHost + " : " + clientPort);

                ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
                Object obj = instream.readObject(); // 문자열 받아와
                printServerLog("데이터 받음: " + obj); //

                ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
                outstream.writeObject(obj + " from Server."); //from server 라는 문자열 붙여서 클라이언트로 다시 보내
                outstream.flush();
                printServerLog("데이터 보냄.");

                sock.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/

}
