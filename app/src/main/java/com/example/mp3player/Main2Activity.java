package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;

public class Main2Activity extends AppCompatActivity {

    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


    }


    public void goBackPlaylist(View view) {
        setContentView(R.layout.activity_main);
        Log.d("ACT2", "goPlaylist: trying to go to one");
    }

    public void goPlaylist(View view) {
        Log.d("ACT2", "goPlaylist: trying to go to one");
    }
}
