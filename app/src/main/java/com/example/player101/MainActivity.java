package com.example.player101;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    Button play,prev,next;
    MusicService musicService;
    MusicCompletionReceiver musicCompletionReceiver;
    Intent startMusicServiceIntent;
    boolean isBound = false;
    boolean isInitialized = false;

    public static final String INITIALIZE_STATUS = "intialization status";
    public static final String MUSIC_PLAYING = "music playing";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = (Button) findViewById(R.id.play);
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        play.setOnClickListener(this);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        startMusicServiceIntent= new Intent(this, MusicService.class);

        if(savedInstanceState != null){
            isInitialized = savedInstanceState.getBoolean(INITIALIZE_STATUS);
//            music.setText(savedInstanceState.getString(MUSIC_PLAYING));
        }

        musicCompletionReceiver = new MusicCompletionReceiver(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.play:
                if (isBound) {
                    switch (musicService.getPlayingStatus()){
                        case 0:
                            musicService.startMusic();
                            play.setText("Pause");
                            break;
                        case 1:
                            musicService.pauseMusic();
                            play.setText("Resume");
                            break;
                        case 2:
                            musicService.resumeMusic();
                            play.setText("Pause");
                            break;
                    }
                }
            case R.id.next:
                musicService.next();
                break;

            case R.id.prev:
                musicService.prev();
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(isInitialized && !isBound){
            bindService(startMusicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        }

        registerReceiver(musicCompletionReceiver, new IntentFilter(MusicService.COMPLETE_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isBound){
            unbindService(musicServiceConnection);
            isBound= false;
        }

        unregisterReceiver(musicCompletionReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(INITIALIZE_STATUS, isInitialized);
//        outState.putString(MUSIC_PLAYING, music.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
            musicService = binder.getService();
            isBound = true;

            switch (musicService.getPlayingStatus()) {
                case 0:
                    play.setText("Start");
                    break;
                case 1:
                    play.setText("Pause");
                    break;
                case 2:
                    play.setText("Resume");
                    break;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
            isBound = false;
        }
    };
}