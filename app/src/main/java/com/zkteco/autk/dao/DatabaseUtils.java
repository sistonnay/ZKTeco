package com.zkteco.autk.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

public class DatabaseUtils {
    private static final String TAG = Utils.TAG + "#" + DatabaseUtils.class.getSimpleName();

    private static volatile DatabaseUtils mDBUtils;

    private DatabaseUtils() {
    }

    public static DatabaseUtils getInstance() {
        if (mDBUtils == null) {
            synchronized (DatabaseUtils.class) {
                if (mDBUtils == null) {
                    mDBUtils = new DatabaseUtils();
                }
            }
        }
        return mDBUtils;
    }

    /**
     * @param db
     * @param table
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public Cursor query(SQLiteDatabase db, String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);

        return cursor;
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs) {
        return insertCheckForUpdate(db, table, values, selection, selectionArgs, null);
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        // Query to find existence.
        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = qb.query(db, new String[]{TABLE.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                changedID = cursor.getInt(0);
                changedCount = db.update(table, values, selection, selectionArgs);
                Logger.d(TAG, "insertCheckForUpdate - do update changedID = " + changedID);
            }
            cursor.close();
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
            }
            Logger.d(TAG, "insertCheckForUpdate - do insert changedID = " + changedID);
        }

        if (changedCount > 0) {
            if (notify != null) {
                notify.send(changedID);
            }
        }

        return changedID;
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, String rowID) {
        return updateCheckForNew(db, table, values, selection, selectionArgs, rowID, null);
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, String rowID, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        qb.setTables(table);
        if (rowID != null) {
            qb.appendWhere(TABLE.KEY_ID + "=" + rowID);
        }

        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = qb.query(db, new String[]{TABLE.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                changedCount = db.update(table, values, selection, selectionArgs);
                Logger.d(TAG, "updateCheckForNew - do update changedCount = " + changedCount);
            }
            cursor.close();
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
            }
            Logger.d(TAG, "updateCheckForNew - do insert changedCount = " + changedCount);
        }

        if (changedCount > 0) {
            if (notify != null) {
                notify.send(changedID);
            }
        }

        return changedCount;
    }

    /**
     * @param db
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
        return delete(db, table, selection, selectionArgs, null);
    }

    /**
     * @param db
     * @param table
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        int count = 0;
        Cursor cursor = qb.query(db, new String[]{TABLE.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = db.delete(table, selection, selectionArgs);
                Logger.d(TAG, "delete - count = " + count);
            }
            cursor.close();
        }

        if (count > 0) {
            if (notify != null) {
                notify.send(count);
            }
        }

        return count;
    }


    /**
     * Notify
     */
    public abstract static class Notify {
        public Uri uri;

        Notify(Uri uri) {
            this.uri = uri;
        }

        public abstract void send(long changedID);
    }

    public interface TABLE {
        String KEY_ID = "_id";
    }

    public interface ENROLL_TABLE extends TABLE {
        String NAME = "enroll_info";
        String KEY_NAME = "name";
        String KEY_FACE_ID = "face_id";
        String KEY_IDENTITY_ID = "identity_id";
        String KEY_PHONE_NUMBER = "phone_number";
    }

    public interface IDENTIFY_TABLE extends TABLE {
        String NAME = "identify_info";
        String KEY_IDENTITY_ID = "identity_id";
        String KEY_CHECK_IN_TIME = "check_in_time";
    }
}
