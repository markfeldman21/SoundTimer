package com.markfeldman.countdownapp;


import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn;
        Button stopBtn;

        amount = (TextView)findViewById(R.id.countDownAmount);
        startBtn = (Button)findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);
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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startButton: {
                setUserInput();
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
            counter.cancel();
            startTime = null;
            millis = 0L;
            amount.setText(R.string.amount_of_time);
            mp = MediaPlayer.create(MainActivity.this, R.raw.car_horn);
            mp.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        input.setText("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mp!=null && mp.isPlaying()){
            mp.stop();
        }
        if (counter!=null) {
            counter.cancel();
        }
        startTime = null;
        millis = null;
    }
}
