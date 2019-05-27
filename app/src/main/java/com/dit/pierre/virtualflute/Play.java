package com.dit.pierre.virtualflute;

import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class Play extends AppCompatActivity {
    private static char[] notesStr = {'D', 'E', 'F', 'G', 'A', 'B', 'C', 'C', 'D'};
    private TestButton holes[] = new TestButton[6];
    private int sum;
    private SoundPool spool;
    private int notes[] = new int[9];
    private int fingerings[] = new int[64];
    private TextView tv_header;
    private int streamId;
    private boolean monitor;

    protected void checksum() {
        if (fingerings[sum] == 0 || !monitor)
            return;
        spool.stop(streamId);
        streamId = spool.play(notes[fingerings[sum] - 1], // correcting for offset
                1, 1,
                1, -1, 1);
    }

    private View.OnTouchListener tlisten = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            TestButton tb = (TestButton) view;
            if(monitor) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!tb.pressed) {
                            tb.pressed = true;
                            sum += tb.getFingerValue();
//                            if (fingerings[sum] > 0)
//                                tv_header.setText("" + notesStr[fingerings[sum] - 1]);
                            checksum();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        tb.pressed = false;
                        sum -= tb.getFingerValue();
//                        if (fingerings[sum] > 0)
//                            tv_header.setText("" + notesStr[fingerings[sum] - 1]);
                        checksum();
                        return true;
                }
            }
            return true;
        }
    };

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        holes[0] = findViewById(R.id.play_bt1);
        holes[1] = findViewById(R.id.play_bt2);
        holes[2] = findViewById(R.id.play_bt3);
        holes[3] = findViewById(R.id.play_bt4);
        holes[4] = findViewById(R.id.play_bt5);
        holes[5] = findViewById(R.id.play_bt6);

        for (int i = 0; i < 6; i++) {
            holes[i].setOnTouchListener(tlisten);
            int val = 1;
            for (int j = 0; j < i; j++)
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

        monitor = false;

        tv_header = findViewById(R.id.play_header_tv);
        tv_header.setText("Start");
        tv_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    protected void start(){
        if(monitor == true){
            monitor = false;
            tv_header.setText("Start");
            spool.stop(streamId);
            return;
        }
        else {
            monitor = true;
            tv_header.setText("Pause");
            checksum();
        }
    }
}