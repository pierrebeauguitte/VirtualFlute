package com.dit.pierre.virtualflute;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessQuery extends AppCompatActivity {

    private static char[] notes = {'D', 'E', 'F', 'G', 'A', 'B', 'C', 'C', 'D'};
    private CorpusManager cm;
    private static int corpusSize;
    private static String query;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_query);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        ArrayList<Integer> pitch = intent.getIntegerArrayListExtra("tuneQueryPitch");
        ArrayList<Integer> ts = intent.getIntegerArrayListExtra("tuneQueryTimestamp");

        float quaver = fuzzyHist(ts);
        System.out.println("Quaver duration : " + quaver);
        query = quantize(pitch, ts, quaver);
        System.out.println("Query pattern: " + query);
        TextView textView = findViewById(R.id.queryView);
        textView.setText(query);

        progressBar = findViewById(R.id.searchProgress);
        progressBar.setMax(100);
        progressBar.setProgress(0);

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

    }

    class MyTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected String doInBackground(Integer... params) {
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
            String rep = "";
            for (Tune t : cumulResults) {
                System.out.println(t);
                rep += t.toString() + "\n";
            }

            return rep;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0] * 100 / corpusSize);
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("Received result " + result);
            TextView resultsView = findViewById(R.id.results);
            resultsView.setText(result);
        }
    }

    public void performSearch(View view) {
        try {
            new MyTask().execute();
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
        String rep = "";
        for (int i=1; i<ts.size(); i++) {
            int interval = ts.get(i) - ts.get(i-1);
            int qInt = Math.round(interval / q);
            for (int j=0; j<qInt; j++)
                rep += notes[pitch.get(i-1)];
        }
        return rep;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cm.close();
    }
}

class Bin {
    public float value;
    public int count;
    public Bin(int value) {
        this.value = value;
        this.count = 1;
    }
}