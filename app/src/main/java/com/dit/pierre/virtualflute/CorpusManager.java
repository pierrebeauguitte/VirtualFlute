package com.dit.pierre.virtualflute;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CorpusManager extends SQLiteOpenHelper {

    // mostly adapted from https://stackoverflow.com/a/5949629

    private File DB_PATH; // = "/data/data/com.example.customtouch/databases/";
    private static String DB_NAME = "corpus.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    public ArrayList<Tune> corpus;

    public CorpusManager(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        this.DB_PATH = this.myContext.getDatabasePath(DB_NAME);
        System.out.println(this.DB_PATH);
        this.corpus = new ArrayList<>();
    }

    // Change for Android P (from https://stackoverflow.com/a/51953955)
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();

        if(!dbExist){
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        } else {
            System.out.println("Database exists already.");

        }
    }

    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(this.DB_PATH.getAbsolutePath(),
                    null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            //database does't exist yet.
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDataBase() throws IOException{
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(DB_PATH.getAbsolutePath());
        
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        System.out.println("All written...");

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLiteException{
        myDataBase = SQLiteDatabase.openDatabase(DB_PATH.getAbsolutePath(),
                null, SQLiteDatabase.OPEN_READONLY);
     }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public int getCorpusSize() {
        Cursor cur = myDataBase.rawQuery("SELECT COUNT(*) FROM Tunes", null);
        cur.moveToFirst();
        int count = cur.getInt(0);
        cur.close();
        return count;
    }

    public boolean cacheTunes(int offset) {
        if (!this.corpus.isEmpty())
            this.corpus.clear();
        Cursor cur = myDataBase.rawQuery("SELECT _id, setting, name, key FROM Tunes LIMIT 500 OFFSET " + offset,
                null);
        while (cur.moveToNext()) {
            corpus.add(new Tune(
                    cur.getInt(0),
                    cur.getInt(1),
                    cur.getString(2),
                    cur.getString(3)));
        }
        cur.close();
        return (corpus.size() >  0);
    }

    static protected Comparator<Tune> cmpTune = new Comparator<Tune>() {
        @Override
        public int compare(Tune t1, Tune t2) {
            float diff = t2.score - t1.score;
            // reverse order in compare...
            if (diff < 0)
                return -1;
            if (diff > 0)
                return 1;
            return 0;
        }
    };

    public List<Tune> searchPattern(String query) {
        for (Tune t: corpus)
            t.getSubED(query);
        Collections.sort(corpus, cmpTune);
        return corpus.subList(0, 10);
    }

}

class Tune {
    private int _id;
    private int setting;
    private String name;
    private String key;
    public float score;

    private static final int MAX_KEY_LENGTH = 2000;
    private static final int MAX_QUERY_LENGTH = 300;
    private static int[][] d = new int[MAX_QUERY_LENGTH + 1][MAX_KEY_LENGTH + 1];

    public Tune(int _id, int setting, String name, String key) {
        this._id = _id;
        this.setting = setting;
        this.name = name;
        this.key = key;
        if (this.key.length() > 2000)
            this.key = this.key.substring(0, 2000);
    }

    public String toString() {
        return String.format(Locale.ENGLISH,
                "[%d_%d] %s - %.2f",
                this._id, this.setting, this.name, this.score * 100);
    }

    public void getSubED(String pattern) {
        int pLength = pattern.length();
        int tLength = this.key.length();
        int difference;

        if (pLength == 0 || tLength == 0) {
            this.score = 0;
            return;
        }

        // Initialise the first row
        for (int i = 0; i < tLength + 1; i++)
            d[0][i] = 0;

        // Now make the first col = 0,1,2,3,4,5,6
        for (int i = 0; i < pLength + 1; i++)
            d[i][0] = i;

        for (int i = 1; i <= pLength; i++) {
            for (int j = 1; j <= tLength; j++) {
                int v = d[i - 1][j - 1];
                difference = (this.key.charAt(j - 1) == pattern.charAt(i-1)) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v + difference);
            }
        }

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < tLength + 1; i++) {
            int c = d[pLength][i];
            if (c < min)
                min = c;
        }

        this.score = 1.0f - min / (float)pLength;
    }

}
