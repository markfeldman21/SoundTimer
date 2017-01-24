package com.markfeldman.countdownapp;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//Add Voice Recorder
//http://www.techotopia.com/index.php/An_Android_Studio_Recording_and_Playback_Example_using_MediaPlayer_and_MediaRecorder
//https://www.youtube.com/watch?v=aZiRJCWpFhE
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView amount;
    private Counter counter;
    private EditText input;
    private Long startTime = null;
    private final String SAVE_KEY = "time";
    private final String SAVE_BOOLEAN = "bool";
    private Long millis;
    private boolean didUserPause = false;
    private MediaPlayer mp;
    final static int MY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "EXPLANATION", Toast.LENGTH_LONG).show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

                Toast.makeText(this, "EXPLANATION NO NEEDED", Toast.LENGTH_LONG).show();
            }
        }

        Button startBtn;
        Button stopBtn;
        Button resetBtn;

        amount = (TextView)findViewById(R.id.countDownAmount);
        startBtn = (Button)findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);
        resetBtn = (Button) findViewById(R.id.resetButton);
        input = (EditText) findViewById(R.id.input);



        if (savedInstanceState!=null){
            startTime = savedInstanceState.getLong(SAVE_KEY);
            didUserPause = savedInstanceState.getBoolean(SAVE_BOOLEAN);
            millis = startTime;
            if (startTime == 0){
                amount.setText(R.string.amount_of_time);
                startTime = null;
            }else if(didUserPause){
                amount.setText(formateTime(startTime));
            }else{
                counter = new Counter(startTime,1000);
                counter.start();
            }
        }

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startButton: {
                if (counter==null)
                {
                    didUserPause = false;
                    setUserInput();
                }else {
                    Toast.makeText(this, R.string.reset_message, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.stopButton: {
                startTime = millis;
                input.setText("");
                didUserPause = true;
                if (counter!=null){
                    counter.cancel();
                }
                break;
            }
            case R.id.resetButton: {
                resetAll();
                amount.setText(R.string.amount_of_time);
                break;
            }
        }
    }

    public void setUserInput(){
        if (startTime == null){
            String inputValue = input.getText().toString();
            startTime = Long.valueOf(inputValue) * 1000;
            counter = new Counter(startTime,1000);
            counter.start();
            input.setText("");
        }else {
            counter = new Counter(startTime,1000);
            counter.start();
            input.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(millis!=null){
            outState.putLong(SAVE_KEY,millis);
            outState.putBoolean(SAVE_BOOLEAN,didUserPause);
        }
    }

    public String formateTime(long time){
        return String.format(Locale.getDefault(),"%02d:%02d:%02d",TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    public void resetAll(){
        if(mp!=null && mp.isPlaying()){
            mp.stop();
        }
        if (counter!=null) {
            counter.cancel();
            counter = null;
        }
        startTime = null;
        millis = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        input.setText("");
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetAll();
    }

    public class Counter extends CountDownTimer {public Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long l) {
            millis = l;
            amount.setText(formateTime(millis));
        }

        @Override
        public void onFinish() {
            String voiceNotePath = Environment.getExternalStorageDirectory().getPath() + "/Voice Recorder/Door.m4a";
            counter.cancel();
            startTime = null;
            millis = 0L;
            amount.setText(R.string.amount_of_time);
            mp = MediaPlayer.create(MainActivity.this, Uri.parse(voiceNotePath));
            mp.start();
        }
    }

}
