package com.dit.pierre.virtualflute;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSearch extends AppCompatActivity {
    private static char[] notes = {'D', 'E', 'F', 'G', 'A', 'B', 'C', 'C', 'D'};
    private ProgressBar progressBar;
    private CorpusManager cm;
    private static int corpusSize;
    private static String query;
    private boolean isActive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hide the title bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_test_search);
//        TextView tvNotes = (TextView) findViewById(R.id.tvNotes);

        progressBar = (ProgressBar)findViewById(R.id.searchProgressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        ArrayList<Integer> pitch = intent.getIntegerArrayListExtra("tuneQueryPitch");
        ArrayList<Integer> ts = intent.getIntegerArrayListExtra("tuneQueryTimestamp");

        isActive = true;

        float quaver = fuzzyHist(ts);
        System.out.println("Quaver duration : " + quaver);
        query = quantize(pitch, ts, quaver);
        System.out.println("Query pattern: " + query);
//        tvNotes.setText(query);

        cm = new CorpusManager(this);
        try {
            cm.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            cm.openDataBase();
            corpusSize = cm.getCorpusSize();
        } catch (SQLiteException e) {
            throw new Error("Unable to open database");
        }
        this.performSearch();
    }

    @Override
    public void onBackPressed() {
        isActive = false;
        Intent intent = new Intent(TestSearch.this,
                Intro.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }

    //Get the result form onPostExecute, pass the result details to next activity
    public void returnThreadResult(List<Tune> result){
        int[] _id = new int[result.size()];
        int[] setting = new int[result.size()];
        String[] name = new String[result.size()];
        double[] score = new double[result.size()];
        for(int i = 0; i < result.size(); i++){
            Tune tune = result.get(i);
            _id[i] = tune.get_id();
            setting[i] = tune.getSetting();
            name[i] = tune.getName();
            score[i] = tune.getScore();
            System.out.println(tune.toString());
        }
        Intent intent = new Intent(this, TestResult.class);
        intent.putExtra("_id", _id);
        intent.putExtra("setting", setting);
        intent.putExtra("name", name);
        intent.putExtra("score", score);
        if(isActive)
            startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cm.close();
        isActive = false;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class MyTaskTest extends AsyncTask<Integer, Integer, List<Tune>> {
        private TestSearch testSearch;

        public MyTaskTest(TestSearch testSearch){
            this.testSearch = testSearch;
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected List<Tune> doInBackground(Integer... params) {
            cm.openDataBase();
            int offset = 0;
            List<Tune> cumulResults = new ArrayList<>();

            while (cm.cacheTunes(offset)) {
                System.out.println("Processing from offset " + offset);
                List<Tune> results = cm.searchPattern(query);
                cumulResults.addAll(results);
                Collections.sort(cumulResults, CorpusManager.cmpTune);
                while (cumulResults.size() > 10)
                    cumulResults.remove(10);
                offset += 500;
                publishProgress(offset);
            }

            return cumulResults;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0] * 100 / corpusSize);
        }

        @Override
        protected void onPostExecute(List<Tune> cumulResults) {
            this.testSearch.returnThreadResult(cumulResults);
        }
    }

    public void performSearch() {
        try {
            new MyTaskTest(this).execute();
        } catch (SQLiteException e) {
            throw new Error("Unable to open database");
        }
    }

    private static float fuzzyHist(ArrayList<Integer> ts)  {
        ArrayList<Bin> bins = new ArrayList<>();
        for (int i=1; i<ts.size(); i++) {
            int interval = ts.get(i) - ts.get(i-1);
            boolean found = false;
            for (Bin b: bins) {
                if (interval > (0.67 * b.value) && interval < (1.33 * b.value)) {
                    b.value = (b.value * b.count + interval) / (float) (b.count + 1);
                    b.count += 1;
                    found = true;
                    break;
                }
            }
            if (!found) {
                bins.add(new Bin(interval));
            }
        }

        int maxCount = 0;
        float duration = 0;
        for (Bin b: bins) {
            if (b.count > maxCount) {
                maxCount = b.count;
                duration = b.value;
            }
        }
        return duration;
    }

    private static String quantize(ArrayList<Integer> pitch, ArrayList<Integer> ts, float q) {
        StringBuilder rep = new StringBuilder("");
        for (int i=1; i<ts.size(); i++) {
            int interval = ts.get(i) - ts.get(i-1);
            int qInt = Math.round(interval / q);
            for (int j=0; j<qInt; j++)
                rep.append(notes[pitch.get(i-1)]);
        }
        return rep.toString();
    }
}

class BinTest {
    public float value;
    public int count;

    public BinTest(int value) {
        this.value = value;
        this.count = 1;
    }
}