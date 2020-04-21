package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permission;
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
    ArrayList<Integer> playlist = new ArrayList<>(); //will be used to host playlist
    Thread playThrd, playSeekThrd, stopThrd; //play threads to be used explicitly
    Spinner spinner; //to show the songs
    Switch urlRawSwitch; //to choose RAW Storage or URL play show
    TextView urlText;
    Button urlStramBtn;
    MediaPlayer mediaPlayer2;
    Playlist pls;
    public int CurrentSong = R.raw.song;
    public int MY_PERM_REQ=1;
    boolean titleOrArtist=true;
    Toolbar tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) //if not yet got SD/EXT permissions..
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE))
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERM_REQ);
        }

        pls=new Playlist(); //my class for playlists....
        getExtMusic();

        mediaPlayer = MediaPlayer.create(getApplicationContext(), CurrentSong);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        handler = new Handler();
        AboutMe(null);
        spinner = findViewById(R.id.spinner);
        urlRawSwitch = findViewById(R.id.switch1);
        urlText = findViewById(R.id.urlText);
        urlStramBtn = findViewById(R.id.streamBtn);

        ///////////////////////SWITCH///////////////////////////////////////
        urlRawSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urlRawSwitch.isChecked()) {
                    urlText.setVisibility(View.VISIBLE);
                    urlStramBtn.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.INVISIBLE);
                    mediaPlayer2=new MediaPlayer();
                    Toast.makeText(getApplicationContext(), "URL Mode , Input MP3 DirectLink", Toast.LENGTH_SHORT).show();

                } else {
                    urlText.setVisibility(View.INVISIBLE);
                    urlStramBtn.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.VISIBLE);
                    mediaPlayer2.release();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), CurrentSong);
                    Toast.makeText(getApplicationContext(), "Normal RAW Storage Mode", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ///////////////////////////////////////////////SPINNER////////////////////////////////////////
        spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item,pls.getCleanSpinnerList()));


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectSongSpinner = parent.getItemAtPosition(position).toString();
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(pls.searchForFullPath(selectSongSpinner)));
                pls.setPlayInd(pls.searchForFullPath(selectSongSpinner));
                handler.postDelayed(stopThrd, 1000);
                handler.postDelayed(playThrd, 1000);
                handler.postDelayed(playSeekThrd, 1000);
                TextView SNAME=findViewById(R.id.songName);
                SNAME.setText((pls.getTitleWithIndex(pls.playInd)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

 /////////////////////////SEEK BAR ///////////////////////////////////////
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && !urlRawSwitch.isChecked()) mediaPlayer.seekTo(progress);
                else if(fromUser && urlRawSwitch.isChecked()) mediaPlayer2.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayingEvents();
        fillPlaylistFromRes(); //HERE THE RESTOCK HAPPENS!

        tab=findViewById(R.id.tab);
        tab.setOnClickListener(new TabLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Bla",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressLint("ResourceType")
    public int giveNameGetID(String songName) {
        return getResources().getIdentifier(songName, "raw", this.getPackageName());
    }


    public ArrayList<String> fillPlaylistFromRes() {
        Field[] inRawArr = R.raw.class.getFields();
        ArrayList<String> namesForSpinner = new ArrayList<String>();
        for (int i = 0; i < inRawArr.length; i++) {
            namesForSpinner.add(inRawArr[i].getName());
            playlist.add(getResources().getIdentifier(inRawArr[i].getName(), "raw", this.getPackageName()));
            Log.d("Filling the Field[]", "fillPlaylistFromRes: " + inRawArr[i].getName() + " " + playlist.get(i));

        }
        return namesForSpinner;
    }

    public CharSequence nicelyFormat(int timeMs) {
        String TAG = "Checking";
        CharSequence cs = "";
        Log.d(TAG, "nicelyFormat: " + timeMs / 1000);
        int timeInSec = timeMs / 1000;
        int Min = timeInSec / 60;
        int Sec = timeInSec - Min * 60;

        return "" + String.valueOf(Min) + ":" + (Sec < 10 ? "0" + String.valueOf(Sec) : String.valueOf(Sec));
    }

    public void PlayingEvents() {
        String TAG = "Debug";
        Log.d(TAG, "PlayingEvents: HERE ");
        if (!urlRawSwitch.isChecked()) { //////////////////////if normal raw player.. then..


        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    timeText.setText(nicelyFormat(mediaPlayer.getCurrentPosition())); //set time val
                    PlayingEvents();
                }

            };

        } else {
            runnable = new Runnable() {
                @Override
                public void run() {

                    PlayingEvents();
                }

            };
        }
        handler.postDelayed(runnable, 1000);
    }
        else //////////////////////////////URL Player
        {
            if (mediaPlayer2.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        timeText=findViewById(R.id.songName2);
                        timeText.setText(nicelyFormat(mediaPlayer2.getCurrentPosition())); //set time val
                        PlayingEvents();
                    }

                };

            } else {
                runnable = new Runnable() {
                    @Override
                    public void run() {

                        PlayingEvents();
                    }

                };
            }
            handler.postDelayed(runnable, 1000);
        }
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

            if (mediaPlayer.getCurrentPosition() < 0) //if song is stopped the position is -3459385 something..
                mediaPlayer.reset();
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(pls.getCurrentFullPath()));
            }
            catch (Exception e)
            {
                Toast.makeText(this,"Song was found , but couldn't be played!",Toast.LENGTH_SHORT).show();
                pls.moveToFirst();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(pls.getCurrentFullPath()));
            }




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
                    songName.setText(pls.getTitleWithIndex(pls.playInd));

                }
                else {
                    mediaPlayer.pause();
                    mediaIconSet(2);
                    eqAnim.setVisibility(View.INVISIBLE);
                    }
               if(mediaPlayer.isPlaying()) {
                   playBtn.setText("Pause");
                   playBtn.setBackground(getResources().getDrawable(R.drawable.pause128,null));
               }
               else {
                   playBtn.setText("Play");
                   playBtn.setBackground(getResources().getDrawable(R.drawable.play128,null));
               }
            }
        };
        playThrd.run();

        playSeekThrd=new Thread(){

            public void run()
            {
                seekBar.setProgress(mediaPlayer.getCurrentPosition()); //set seek val
                handler.postDelayed(this,1000);
            }

        };
        playSeekThrd.start();
    }

    public void Stop(View view) {
        mediaPlayer.reset();
        playBtn.setText("Play");
        playBtn.setBackground(getResources().getDrawable(R.drawable.play128,null)); //new LOGO changes as well.
        stopThrd=new Thread(){public void run(){

            timeText.setText("Stopped");
            mediaIconSet(0);
            findViewById(R.id.eqAnim).setVisibility(View.INVISIBLE); //nested thread of 1sec interval to show stopped and kill gif.
        }};
        handler.postDelayed(stopThrd,1000);
    }

    public void Next(View view) {

        mediaPlayer.reset();
        playBtn.setText("Play");
        mediaPlayer=MediaPlayer.create(getApplicationContext(),Uri.parse(pls.moveToAndGetNext())); //internal call to my lovely playlist class <3
        handler.postDelayed(stopThrd,1000);
        handler.postDelayed(playThrd,1000);
        handler.postDelayed(playSeekThrd,1000);
    }

    public void AboutMe(View view) {
        Toast.makeText(getApplicationContext(),"AudioPlayer was developed by Daniel Ohayon for Matan Nir",Toast.LENGTH_LONG).show();
    }

    public void Prev(View view) {
        mediaPlayer.reset();
        playBtn.setText("Play");
        mediaPlayer=MediaPlayer.create(getApplicationContext(),Uri.parse(pls.moveToAndGetPrev())); //internal call to my lovely playlist class <3
        handler.postDelayed(stopThrd,1000);
        handler.postDelayed(playThrd,1000);
        handler.postDelayed(playSeekThrd,1000);
    }

    public void prepareURLStream(String url)
    {

        try {
            mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer2.setDataSource(url);
            Toast.makeText(getApplicationContext(),"Buffering Media..",Toast.LENGTH_SHORT).show();
            mediaPlayer2.prepare();
        } catch (IOException e) {
            Log.e("STREAM-ERR", "run: ", e);
            Toast.makeText(getApplicationContext(),"Failed to Load Stream From URL, Check The Link",Toast.LENGTH_LONG).show();
        }
    }
    public void StreamUrl(View view) {
        final Button streamBtn=findViewById(R.id.streamBtn);
            new Thread(){
                public void run(){
                    final CharSequence streamURI=urlText.getText();
                    prepareURLStream(streamURI.toString());
                    songName=findViewById(R.id.songName);

                    PlayingEvents();


                    seekBar.setMax(mediaPlayer2.getDuration());

                    final GifImageView eqAnim=(GifImageView)findViewById(R.id.eqAnim);
                    playThrd=new Thread(){
                        @SuppressLint("ResourceType")
                        public void run(){

                            if(streamBtn.getText().equals("Stream")) {
                                mediaPlayer2.start();
                                mediaIconSet(1);
                                eqAnim.setVisibility(View.VISIBLE);

                            }
                            else {
                                mediaPlayer2.pause();
                                mediaIconSet(2);
                                eqAnim.setVisibility(View.INVISIBLE);
                            }
                            if(mediaPlayer2.isPlaying()) {
                                streamBtn.setText("Pause");
                            }
                            else
                                streamBtn.setText("Stream");
                        }
                    };
                    playThrd.run();

                    playSeekThrd=new Thread(){

                        public void run()
                        {

                            Log.d("Seek Debug", "run: ");
                            seekBar.setProgress(mediaPlayer2.getCurrentPosition()); //set seek val
                            handler.postDelayed(this,1000);
                        }

                    };
                    playSeekThrd.start();
                }
        }.run();

    }

    @SuppressLint("Recycle")
    public void getExtMusic()
    {

        int songTitle=0,songArtist=0,actualName=0;
        ContentResolver contentResolver = getContentResolver();
        Uri CurrentSongUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songsCursor= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d("TAG", "getExtMusicURINAME: "+String.valueOf(CurrentSongUri));
            songsCursor = contentResolver.query(CurrentSongUri,null,null,null);
        }
        if(songsCursor.getCount() >0 && songsCursor.moveToFirst())
        {
            songTitle=songsCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            songArtist=songsCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            actualName=songsCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        }

        while(!songsCursor.isAfterLast())
        {
            Log.d("TAG", "getExtMusic: "+ CurrentSongUri+songsCursor.getString(songTitle)+songsCursor.getString(actualName));
            pls.addSong(songsCursor.getString(actualName),songsCursor.getString(songTitle),songsCursor.getString(songArtist));
           songsCursor.moveToNext();


        }
    }

    public void SwapTitleArtist(View view) {
        if(songName.getText().length()>1 && titleOrArtist) {
            songName.setText(pls.getArtistWithIndex(pls.playInd));
            titleOrArtist = !titleOrArtist;
        }
        else if(songName.getText().length()>1)
        {
            songName.setText(pls.getTitleWithIndex(pls.playInd));
            titleOrArtist = !titleOrArtist;
        }


    }
}
