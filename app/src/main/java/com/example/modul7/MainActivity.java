package com.example.modul7;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ImageView imgSlot1;
    private ImageView imgSlot2;
    private ImageView imgSlot3;
    private Button btnGet;
    ArrayList<String> arrayUrl = new ArrayList<>();
    ExecutorService execGetImage;
    boolean isPlaying = false;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable[] runnables = new Runnable[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGet = findViewById(R.id.btn_get);
        imgSlot1 = findViewById(R.id.img_slot1);
        imgSlot2 = findViewById(R.id.img_slot2);
        imgSlot3 = findViewById(R.id.img_slot3);

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    startImages();
                } else {
                    stopImages();
                }
            }
        });
    }

    private void startImages() {
        isPlaying = true;
        btnGet.setText("Stop");

        execGetImage = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            runnables[i] = new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && isPlaying) {
                        try {
                            final String txt = loadStringFromNetwork("https://661fe99e16358961cd95e3e5.mockapi.io/api/v1/items");
                            try {
                                JSONArray jsonArray = new JSONArray(txt);
                                arrayUrl.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    arrayUrl.add(jsonObject.getString("url"));
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Random rand = new Random();
                                        int randomIndex = rand.nextInt(arrayUrl.size());
                                        switch (index) {
                                            case 0:
                                                Glide.with(MainActivity.this).load(arrayUrl.get(randomIndex)).into(imgSlot1);
                                                break;
                                            case 1:
                                                Glide.with(MainActivity.this).load(arrayUrl.get(randomIndex)).into(imgSlot2);
                                                break;
                                            case 2:
                                                Glide.with(MainActivity.this).load(arrayUrl.get(randomIndex)).into(imgSlot3);
                                                break;
                                        }
                                    }
                                });

                                Thread.sleep(15);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            execGetImage.execute(runnables[i]);
        }
    }

    private void stopImages() {
        isPlaying = false;
        btnGet.setText("Ambil gambar");
        arrayUrl.clear();
        if (execGetImage != null) {
            for (int i = 0; i < 3; i++) {
                if (runnables[i] != null) {
                    Thread.currentThread().interrupt();
                }
            }
            execGetImage.shutdown();
        }
    }


    private String loadStringFromNetwork(String s) throws IOException {
        final URL myUrl = new URL(s);
        final InputStream in = myUrl.openStream();
        final StringBuilder out = new StringBuilder();
        final byte[] buffer = new byte[1024];
        try {
            for (int ctr; (ctr = in.read(buffer)) != -1; ) {
                out.append(new String(buffer, 0, ctr));
            }
        } catch (IOException e) {
            throw new RuntimeException("Gagal mendapatkan text", e);
        }
        final String yourFileAsAString = out.toString();
        return yourFileAsAString;
    }
}
