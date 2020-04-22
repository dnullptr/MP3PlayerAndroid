package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void goBackPlaylist(View view) {
        setContentView(R.layout.activity_main);
        Log.d("TAG", "goPlaylist: trying to go to one");
    }
}
