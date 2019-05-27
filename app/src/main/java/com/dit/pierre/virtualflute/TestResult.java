package com.dit.pierre.virtualflute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestResult extends AppCompatActivity {
    private int[] _id;
    private int[] setting;
    private String[] name;
    private double[] score;
    private ListView lvResult;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
        getSupportActionBar().hide();
        lvResult = findViewById(R.id.lvResult);

        Intent intent = getIntent();
        _id = intent.getIntArrayExtra("_id");
        setting = intent.getIntArrayExtra("setting");
        name = intent.getStringArrayExtra("name");
        score = intent.getDoubleArrayExtra("score");
        ArrayList<String> results = new ArrayList<>();

        //Get the "toString() of Tone, put them to array for ListView
        for(int i = 0; i < _id.length; i++){
            String temp = String.format(Locale.ENGLISH,
                    "%s - %.2f",
                    name[i], score[i]* 100);
            results.add(temp);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, results);
        lvResult.setAdapter(adapter);
        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uri = "https://thesession.org/tunes/" + _id[position] + "#setting" + setting[position];
                Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(uriIntent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TestResult.this,
                Intro.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }
}
