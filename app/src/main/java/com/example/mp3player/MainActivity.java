package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    public MediaPlayer mediaPlayer; //the engine of media itself
    Button playBtn; //the ref to play button
    TextView songName; //the ref to text of song name
    TextView timeText; //the ref to text of time
    SeekBar seekBar; //ref to seek bar in ui
    Runnable runnable; //ref to new thread that are going to asynchronously run.
    Handler handler; //get os refresh rate , window utilization and all real-timeness.
    ArrayList<Integer> playlist=new ArrayList<>(); //will be used to host playlist
    Thread playThrd,playSeekThrd,stopThrd; //play threads to be used explicitly
    Spinner spinner; //to show the songs
    public int CurrentSong=R.raw.song,CurrentSongListIndex=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       mediaPlayer=MediaPlayer.create(getApplicationContext(),CurrentSong);
       seekBar=(SeekBar)findViewById(R.id.seekBar);
        handler=new Handler();
        AboutMe(null);
        spinner=findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,fillPlaylistFromRes()));


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectSongSpinner=parent.getItemAtPosition(position).toString();
                CurrentSong=giveNameGetID(selectSongSpinner);
                mediaPlayer.reset();
                mediaPlayer=MediaPlayer.create(getApplicationContext(),CurrentSong);
                handler.postDelayed(stopThrd,1000);
                handler.postDelayed(playThrd,1000);
                handler.postDelayed(playSeekThrd,1000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


       seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if(fromUser) mediaPlayer.seekTo(progress);
           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

           }
       });
        PlayingEvents();
        fillPlaylistFromRes();
    }

    @SuppressLint("ResourceType")
    public int giveNameGetID(String songName)
    {
        return getResources().getIdentifier(songName,"raw",this.getPackageName());
    }


    public ArrayList<String> fillPlaylistFromRes() {
        Field[] inRawArr=R.raw.class.getFields();
        ArrayList<String> namesForSpinner=new ArrayList<String>();
        for(int i=0;i<inRawArr.length;i++)
        {
            namesForSpinner.add(inRawArr[i].getName());
            playlist.add(getResources().getIdentifier(inRawArr[i].getName(),"raw",this.getPackageName()));
            Log.d("Filling the Field[]", "fillPlaylistFromRes: "+inRawArr[i].getName()+" "+playlist.get(i));

        }
        return namesForSpinner;
    }

    public CharSequence nicelyFormat(int timeMs){
        String TAG="Checking";
        CharSequence cs="";
        Log.d(TAG, "nicelyFormat: "+timeMs/1000);
        int timeInSec=timeMs/1000;
        int Min=timeInSec/60;
        int Sec=timeInSec-Min*60;

        return ""+String.valueOf(Min)+":"+ (Sec<10? "0"+String.valueOf(Sec):String.valueOf(Sec));
    }

    public void PlayingEvents(){
        String TAG="Debug";
        Log.d(TAG, "PlayingEvents: HERE ");
        if(mediaPlayer.isPlaying()){
            runnable= new Runnable() {
                @Override
                public void run() {
                    timeText.setText(nicelyFormat(mediaPlayer.getCurrentPosition())); //set time val
                    PlayingEvents();
                }

            };

        }
        else
        {
            runnable= new Runnable() {
                @Override
                public void run() {

                    PlayingEvents();
                }

            };
        }
        handler.postDelayed(runnable,1000);
    }


    public void mediaIconSet(int sel){ //i decided that 0-stop 1-play 2-pause
        ImageView playIcon=findViewById(R.id.playIcon);
        ImageView stopIcon=findViewById(R.id.stopIcon);
        ImageView pauseIcon=findViewById(R.id.pauseIcon);
        switch (sel)
        {
            case 0:
                playIcon.setVisibility(View.INVISIBLE);
                pauseIcon.setVisibility(View.INVISIBLE);
                stopIcon.setVisibility(View.VISIBLE);
                break;

            case 1:
                playIcon.setVisibility(View.VISIBLE);
                pauseIcon.setVisibility(View.INVISIBLE);
                stopIcon.setVisibility(View.INVISIBLE);
                break;

            case 2:
                playIcon.setVisibility(View.INVISIBLE);
                pauseIcon.setVisibility(View.VISIBLE);
                stopIcon.setVisibility(View.INVISIBLE);
                break;
        }

    }

    public void Play(View view) {
        if(mediaPlayer.getCurrentPosition() < 0) //if song is stopped the position is -3459385 something..
            mediaPlayer=MediaPlayer.create(getApplicationContext(),CurrentSong);


        playBtn=findViewById(R.id.buttonPlay);
        songName=findViewById(R.id.songName);
        timeText=findViewById(R.id.songName2);
        seekBar.setMax(mediaPlayer.getDuration());
        final GifImageView eqAnim=(GifImageView)findViewById(R.id.eqAnim);
        playThrd=new Thread(){
            @SuppressLint("ResourceType")
            public void run(){
                if(playBtn.getText().equals("Play")) {
                    mediaPlayer.start();
                    mediaIconSet(1);
                    eqAnim.setVisibility(View.VISIBLE);
                    songName.setText(getResources().getText(CurrentSong).toString().replace("res/raw/",""));
                }
                else {
                    mediaPlayer.pause();
                    mediaIconSet(2);
                    eqAnim.setVisibility(View.INVISIBLE);
                    }
               if(mediaPlayer.isPlaying()) {
                   playBtn.setText("Pause");
               }
               else
                   playBtn.setText("Play");
            }
        };
        playThrd.run();

        playSeekThrd=new Thread(){

            public void run()
            {

                Log.d("Seek Debug", "run: ");
                seekBar.setProgress(mediaPlayer.getCurrentPosition()); //set seek val
                handler.postDelayed(this,1000);
            }

        };
        playSeekThrd.start();
    }

    public void Stop(View view) {
        mediaPlayer.reset();
        playBtn.setText("Play");
        stopThrd=new Thread(){public void run(){

            timeText.setText("Stopped");
            mediaIconSet(0);
            findViewById(R.id.eqAnim).setVisibility(View.INVISIBLE); //nested thread of 1sec interval to show stopped and kill gif.
        }};
        handler.postDelayed(stopThrd,1000);
    }

    public void Next(View view) throws IllegalAccessException {
        Log.d("TAG", "Next: "+CurrentSong+", "+"Poll:"+playlist.get(CurrentSongListIndex));

            if(CurrentSongListIndex==playlist.size()-1)
            {
                CurrentSong=playlist.get(CurrentSongListIndex);
                CurrentSongListIndex=0;
            }
            else {
                CurrentSong = playlist.get(CurrentSongListIndex++);
            }

        mediaPlayer.reset();
        playBtn.setText("Play");
        mediaPlayer=MediaPlayer.create(getApplicationContext(),CurrentSong);
        handler.postDelayed(stopThrd,1000);
        handler.postDelayed(playThrd,1000);
        handler.postDelayed(playSeekThrd,1000);
    }

    public void AboutMe(View view) {
        Toast.makeText(getApplicationContext(),"AudioPlayer was developed by Daniel Ohayon for Matan Nir",Toast.LENGTH_LONG).show();
    }
}
