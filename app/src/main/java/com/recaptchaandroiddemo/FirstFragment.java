package com.recaptchaandroiddemo;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.recaptchaandroiddemo.databinding.FragmentFirstBinding;

class Reply{
    private String data;
    private String result;

    public void setData(String data) {
        this.data = data;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getData() {
        return data;
    }

    public String getResult() {
        return result;
    }
}

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private TextView tx1;
    private final String ipAddr = "172.17.0.1";
    private final String port = "8080";
    private final String endpoint = "api";
    private final String proto = "http"; // or https

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        tx1 = binding.textviewFirst;
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {

            private void sendRequest() {
                Reply reply = new Reply();
                OutputStream out;
                try {
                    URL url = new URL(proto+"://"+ipAddr+":"+port+"/"+endpoint);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("X-API-Key","TEST");
                    out = new BufferedOutputStream(conn.getOutputStream());

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write("{\"type\":\"android\"}");
                    writer.flush();
                    writer.close();
                    out.close();
                    conn.connect();
                    try {
                        InputStream is = conn.getInputStream();
                        byte[] b1 = new byte[1024];
                        StringBuilder buffer = new StringBuilder();

                        while ( is.read(b1) != -1)
                            buffer.append(new String(b1));

                        conn.disconnect();
                        try {
                            JSONObject jsonObject = new JSONObject(buffer.toString());
                            reply.setData(jsonObject.getString("data"));
                            reply.setResult(jsonObject.getString("result"));
                        }
                        catch(Exception e){
                            reply.setData("error");
                            reply.setResult("App Error: Couldn't create JSON object");
                        }
                    }
                    catch(Exception e){
                        reply.setData("error");
                        reply.setResult("App Error: Couldn't read buffer");
                    }
                }
                catch(Exception e){
                    reply.setData("error");
                    reply.setResult("App Error: Couldn't connect to URL");
                }
                String replyContent = reply.getResult()+"\n"+reply.getData();
                tx1.setText(replyContent);
            }
            @Override
            public void onClick(View view) {
                try{
                    sendRequest();
                }
                catch(Exception e){
                    tx1.setText("error\nApp Error: Can't send request to server");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}