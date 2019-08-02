package com.zkteco.autk.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.zkteco.autk.models.EnrollModel;
import com.zkteco.autk.models.ZKLiveFaceManager;
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

    public void initFaceLibrary(SQLiteOpenHelper helper) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = query(db, ENROLL_TABLE.NAME, new String[]{ENROLL_TABLE.KEY_FACE_ID, ENROLL_TABLE.KEY_FACE_TEMPLATE},
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String faceId = cursor.getString(cursor.getColumnIndex(ENROLL_TABLE.KEY_FACE_ID));
                    byte[] template = cursor.getBlob(cursor.getColumnIndex(ENROLL_TABLE.KEY_FACE_TEMPLATE));
                    ZKLiveFaceManager.getInstance().dbAdd(faceId, template);
                    Logger.v(TAG, faceId + " has been added.");
                }
                cursor.close();
            }
        } catch (Exception e) {
            Logger.e(TAG, "error occurred while do db Transaction:", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public long insertFaceEnrollInfo(SQLiteOpenHelper helper, EnrollModel.IdentifyInfo info) {
        long rowId = -1;
        ContentValues values = new ContentValues();
        values.put(ENROLL_TABLE.KEY_NAME, info.name);
        values.put(ENROLL_TABLE.KEY_JOB_NUMBER, info.job_number);
        values.put(ENROLL_TABLE.KEY_PHONE_NUMBER, info.phone);
        values.put(ENROLL_TABLE.KEY_FACE_ID, info.faceId);
        values.put(ENROLL_TABLE.KEY_FACE_TEMPLATE, info.face_template);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            rowId = insertCheckForUpdate(db, ENROLL_TABLE.NAME, values,
                    ENROLL_TABLE.KEY_FACE_ID + " = '" + info.faceId + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "error occurred while do db Transaction:", e);
            return rowId;
        } finally {
            db.endTransaction();
            db.close();
        }
        return rowId;
    }

    public long insertFaceCheckInInfo(SQLiteOpenHelper helper, String faceId, long time) {
        long rowId = -1;
        EnrollModel.uploadInfo info = null;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();

            Cursor cursor = query(db, ENROLL_TABLE.NAME, new String[]{ENROLL_TABLE.KEY_ID, ENROLL_TABLE.KEY_NAME},
                    ENROLL_TABLE.KEY_FACE_ID + " = '" + faceId + "'", null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    info = new EnrollModel.uploadInfo();
                    info.name = cursor.getString(cursor.getColumnIndex(ENROLL_TABLE.KEY_NAME));
                    info.job_number = cursor.getString(cursor.getColumnIndex(ENROLL_TABLE.KEY_JOB_NUMBER));
                    info.time = String.valueOf(time);
                    info.type = "face";
                }
                cursor.close();
            }

            if (info == null) {
                return -1;
            }

            ContentValues values = new ContentValues();
            values.put(IDENTIFY_TABLE.KEY_CHECK_IN_TIME, info.time);
            values.put(IDENTIFY_TABLE.KEY_JOB_NUMBER, info.job_number);
            rowId = insertCheckForUpdate(db, IDENTIFY_TABLE.NAME, values,
                    IDENTIFY_TABLE.KEY_JOB_NUMBER + " = '" + info.job_number + "'", null);

            db.setTransactionSuccessful();

            if (rowId != -1) {
                info.upload();
                Logger.v(TAG, info.toString());
            }
        } catch (Exception e) {
            Logger.e(TAG, "error occurred while do db Transaction:", e);
            return rowId;
        } finally {
            db.endTransaction();
            db.close();
        }
        return rowId;
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
        String KEY_JOB_NUMBER = "job_number";
        String KEY_PHONE_NUMBER = "phone_number";
        String KEY_FACE_TEMPLATE = "face_template";
    }

    public interface IDENTIFY_TABLE extends TABLE {
        String NAME = "identify_info";
        String KEY_JOB_NUMBER = "job_number";
        String KEY_CHECK_IN_TIME = "check_in_time";
    }
}
