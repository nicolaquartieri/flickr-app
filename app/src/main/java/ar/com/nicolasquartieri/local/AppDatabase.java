package ar.com.nicolasquartieri.local;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Application database which use SQLite.
 * It support incremental upgrades.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class AppDatabase extends SQLiteOpenHelper {
    /** Database Name */
    private static final String DATABASE_NAME = "application.db";
    /** Current database version - App Version: ?? */
    public static final int DATABASE_VERSION = 7;

    /**
     * Default constructor
     * .
     * @param context the context, cannot be null.
     */
    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(CacheStateTable.CREATE);
            db.execSQL(PhotoTable.CREATE);
            db.execSQL(PhotoInfoTable.CREATE);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    @Override
    public void onOpen(final SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public static void cleanDB(ContentResolver contentResolver) {
        contentResolver.delete(PhotoTable.buildUri(), null, null);
        contentResolver.delete(PhotoInfoTable.buildUri(), null, null);
    }
}
