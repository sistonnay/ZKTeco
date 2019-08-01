package com.zkteco.autk.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;
import com.zkteco.autk.dao.DatabaseUtils.ENROLL_TABLE;
import com.zkteco.autk.dao.DatabaseUtils.IDENTIFY_TABLE;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = Utils.TAG + "#" + DatabaseHelper.class.getSimpleName();
    private static final String DB_NAME = "data_collection.db";

    private static final int DB_VERSION = 1;

    private Context mContext;

    private final String sql_create_enroll_tb = "CREATE TABLE "
            + ENROLL_TABLE.NAME + " ("
            + ENROLL_TABLE.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ENROLL_TABLE.KEY_NAME + " TEXT  NOT NULL,"
            + ENROLL_TABLE.KEY_FACE_ID + " TEXT  NOT NULL,"
            + ENROLL_TABLE.KEY_IDENTITY_ID + " TEXT  NOT NULL,"
            + ENROLL_TABLE.KEY_PHONE_NUMBER + " TEXT NOT NULL)";

    private final String sql_create_identify_tb = "CREATE TABLE "
            + IDENTIFY_TABLE.NAME + " ("
            + IDENTIFY_TABLE.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + IDENTIFY_TABLE.KEY_JOB_NUMBER + " INTEGER DEFAULT -1,"
            + IDENTIFY_TABLE.KEY_CHECK_IN_TIME + " TEXT NOT NULL)";

    public DatabaseHelper(@Nullable Context context) {
        this(context, DB_VERSION);
    }

    public DatabaseHelper(@Nullable Context context, int version) {
        super(context, DB_NAME, null, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.v(TAG, "onCreate: create tables");
        try {
            db.execSQL(sql_create_enroll_tb);
            db.execSQL(sql_create_identify_tb);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.w(TAG, "Upgrading settings database from version " + oldVersion + " to " + newVersion);
        int version = oldVersion;

        if (version != DB_VERSION) {
            Logger.w(TAG, "Destroying old data during upgrade.");
            db.execSQL("DROP TABLE IF EXISTS " + ENROLL_TABLE.NAME);
            db.execSQL("DROP TABLE IF EXISTS " + IDENTIFY_TABLE.NAME);
            onCreate(db);
        }
    }
}
