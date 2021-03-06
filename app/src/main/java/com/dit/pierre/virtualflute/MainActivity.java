package com.dit.pierre.virtualflute;

import android.content.Intent;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TButton holes[] = new TButton[6];
    private int sum;
    private SoundPool spool;
    private int streamId;
    private int notes[] = new int[9];
    private int fingerings[] = new int[64];
    private ProgressBar mProgressBar;
    private CountDownTimer mCountDownTimer;
    private CountDownTimer preCount;
    private int progress = 0;
    private int countdown = 3;
    private TextView pc;
    private ArrayList<Integer> queryPitch;
    private ArrayList<Integer> queryTimestamp;
    private boolean recording;
    private boolean monitor;

    private View.OnTouchListener tlisten = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            TButton tb = (TButton) view;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!tb.pressed) {
                        tb.pressed = true;
                        sum += tb.getFingerValue();
                        ((TextView) findViewById(R.id.textView)).setText("[" + sum + "]");
                        checksum();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    tb.pressed = false;
                    sum -= tb.getFingerValue();
                    ((TextView) findViewById(R.id.textView)).setText("[" + sum + "]");
                    checksum();
                    return true;
            }

            return true;
        }
    };

    protected void checksum() {
        if (fingerings[sum] == 0 || !monitor)
            return;
        spool.stop(streamId);
        streamId = spool.play(notes[fingerings[sum] - 1], // correcting for offset
                1, 1,
                1, -1, 1);
        if (recording) {
            queryPitch.add(fingerings[sum] - 1);
            queryTimestamp.add((int) System.currentTimeMillis());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hide the title bar
        getSupportActionBar().hide();

        holes[0] = findViewById(R.id.button1);
        holes[1] = findViewById(R.id.button2);
        holes[2] = findViewById(R.id.button3);
        holes[3] = findViewById(R.id.button4);
        holes[4] = findViewById(R.id.button5);
        holes[5] = findViewById(R.id.button6);
        for (int i=0; i<6; i++) {
            holes[i].setOnTouchListener(tlisten);
            int val = 1;
            for (int j=0; j<i; j++)
                val *= 2;
            holes[i].setFingerValue(val);
        }
        sum = 0;

        spool = (new SoundPool.Builder()).build();
        notes[0] = spool.load(this, R.raw.flute_d4, 1);
        notes[1] = spool.load(this, R.raw.flute_e4, 1);
        notes[2] = spool.load(this, R.raw.flute_fs4, 1);
        notes[3] = spool.load(this, R.raw.flute_g4, 1);
        notes[4] = spool.load(this, R.raw.flute_a4, 1);
        notes[5] = spool.load(this, R.raw.flute_b4, 1);
        notes[6] = spool.load(this, R.raw.flute_c5, 1);
        notes[7] = spool.load(this, R.raw.flute_cs5, 1);
        notes[8] = spool.load(this, R.raw.flute_d5, 1);
        streamId = 0;

        // offset so that 0 == nothing
        fingerings[63] = 1; // 0 + 1
        fingerings[31] = 2; // 1 + 1
        fingerings[15] = 3; // 2 + 1
        fingerings[1] =  6; // 5 + 1
        fingerings[3] =  5; // 4 + 1
        fingerings[7] =  4; // 3 + 1
        fingerings[6] =  7; // 6 + 1
        fingerings[0] =  8; // 7 + 1
        fingerings[62] = 9; // 8 + 1

        recording = false;
        monitor = true;

        // Mute button
        View b = findViewById(R.id.stopSound);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spool.stop(streamId);
            }
        });

        // progress bar
        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setProgress(progress);
        mCountDownTimer=new CountDownTimer(12000,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progress++;
                mProgressBar.setProgress(progress*100/(12000/100));
            }

            @Override
            public void onFinish() {
                progress++;
                mProgressBar.setProgress(100);
                recording = false;
                spool.stop(streamId);
                monitor = false;
                sendQuery();
            }
        };

        pc = findViewById(R.id.countdown);
        pc.setText("Start");
        preCount = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                pc.setText("[" + countdown + "]");
                countdown--;
            }

            @Override
            public void onFinish() {
                pc.setText("[" + countdown + "]");
                mCountDownTimer.start();
                recording = true;
                checksum();
            }
        };

        pc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preCount.start();
            }
        });
        queryPitch = new ArrayList<>();
        queryTimestamp = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pc.setText("Start");
        countdown = 3;
        progress = 0;
        sum = 0;
        mProgressBar.setProgress(0);
        queryPitch.clear();
        queryTimestamp.clear();
        recording = false;
        monitor = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        spool.stop(streamId);
        monitor = false;
    }

    public void sendQuery() {
        Intent intent = new Intent(this, ProcessQuery.class);
        intent.putExtra("tuneQueryPitch", queryPitch);
        intent.putExtra("tuneQueryTimestamp", queryTimestamp);
        startActivity(intent);
    }
}
