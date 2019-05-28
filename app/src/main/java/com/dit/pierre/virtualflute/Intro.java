package com.dit.pierre.virtualflute;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Intro extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //hide the title bar
        getSupportActionBar().hide();
        //hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Button btSearch = (Button) findViewById(R.id.bt_search);
        btSearch.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent myIntent = new Intent(view.getContext(), Test.class);
                startActivityForResult(myIntent, 0);
            }
        });

        ObjectAnimator pulseBtSearch = createPulse(btSearch);
        pulseBtSearch.start();

        Button btPlay = (Button) findViewById(R.id.bt_play);
        btPlay.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent myIntent = new Intent(view.getContext(), Play.class);
                startActivityForResult(myIntent,0);
            }
        });

        ObjectAnimator pulseBtPlay = createPulse(btPlay);
        pulseBtPlay.start();
    }

    //adapted from https://stackoverflow.com/questions/27301586/repeat-pulse-animation
    protected ObjectAnimator createPulse(Button button){
        ObjectAnimator pulseButton = ObjectAnimator.ofPropertyValuesHolder(
                button,
                PropertyValuesHolder.ofFloat("scaleX", 1.05f),
                PropertyValuesHolder.ofFloat("scaleY", 1.05f));
        pulseButton.setDuration(900);
        pulseButton.setRepeatCount(ObjectAnimator.INFINITE);
        pulseButton.setRepeatMode(ObjectAnimator.REVERSE);
        return pulseButton;
    }
}
