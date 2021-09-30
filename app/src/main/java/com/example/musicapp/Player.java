package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class Player extends AppCompatActivity {
    Button btnplay,btnnext,btnprev,btnff,btnfr;
    TextView txtsname,txtstart,txtstop;
    SeekBar seekmusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    protected void onDestroy() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnprev=findViewById(R.id.btprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsname=findViewById(R.id.txtsn);
        txtstart = findViewById(R.id.textsstart);
        txtstop = findViewById(R.id.textsstop);
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.bar);
        imageView = findViewById(R.id.imageview);

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle= i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri =Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateseekbar = new Thread(){
            @Override
            public void run(){
                int totalDuration = mediaPlayer.getDuration();
                int currentposition=0;

                while (currentposition<totalDuration){
                    try {
                        sleep(500);
                        currentposition=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.teal_200), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.teal_700),PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        String endTime=createTime(mediaPlayer.getDuration());
        txtstop.setText(endTime);

        final Handler handler=new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime= createTime(mediaPlayer.getCurrentPosition());
                txtstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    btnplay.setBackgroundResource(R.drawable.playico);
                    mediaPlayer.pause();  //Pause song
                }
                else {
                    btnplay.setBackgroundResource(R.drawable.pauseico);
                    mediaPlayer.start(); // start track
                }
            }
        });
        int audiosessionId = mediaPlayer.getAudioSessionId();
        if(audiosessionId!=-1){
            visualizer.setAudioSessionId(audiosessionId);
        }

        //next BTN
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position =((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname =mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pauseico);
                startAnimation(imageView);

            }
        });
        //prev BTN
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pauseico);
                startAnimation(imageView);

            }
        });



    }
    public void startAnimation(View view){
        ObjectAnimator animator=ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time ="";
        int min=duration/1000/60;
        int sec = duration/1000%60;
        time+=min+":";
        if (sec<10){
            time+="0";
        }
        time+=sec;
        return time;

    }
}