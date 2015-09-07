package org.tbadg.memory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "scores.db";
    private static final int SCHEMA = 1;

    static public final String TABLE = "scores";
    static public final String SCORE = "score";
    static public final String MATCHES = "matches";
    static public final String GUESSES = "guesses";
    static public final String ELAPSED_TIME = "elapsedTime";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + SCORE + " integer, "
                + MATCHES + " integer, "
                + GUESSES + " integer, "
                + ELAPSED_TIME + " integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("No upgrade possible?");
    }
}
