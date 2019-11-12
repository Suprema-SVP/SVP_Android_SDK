package com.supremainc.svpdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.supremainc.svpdemo.Data.User;
import com.supremainc.sdk.model.Finger;

import java.util.ArrayList;

import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "SvpUser";

    private static final String TABLE_USER = "User";

    private static final String KEY_ID = "UserId";
    private static final String KEY_NAME = "UserName";

    private static final String KEY_FINGER_ID = "FingerprintID";
    private static final String KEY_FINGER_INDEX = "FingerprintIndex";
    private static final String KEY_FINGER_DATA_0 = "FingerprintTemplate_0";
    private static final String KEY_FINGER_DATA_1 = "FingerprintTemplate_1";

    private static final String KEY_CARD = "CardID";

    private static String TAG = "SvpDatabase";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                KEY_ID + " TEXT NOT NULL," +
                KEY_NAME + " TEXT, " +
                KEY_FINGER_ID + " INTEGER, " +
                KEY_FINGER_INDEX + " INTEGER, " +
                KEY_FINGER_DATA_0 + " BLOB, " +
                KEY_FINGER_DATA_1 + " BLOB, " +
                KEY_CARD + " TEXT" +
                ");";

        Log.i(TAG, CREATE_TABLE_USER);

        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String DROP_TABLE_USER = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(DROP_TABLE_USER);

        Log.i(TAG, DROP_TABLE_USER);

        onCreate(db);
    }

    public void add(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getUserID());
        values.put(KEY_NAME, user.getUserName());

        values.put(KEY_FINGER_ID, user.getFingerID(0));

        values.put(KEY_FINGER_INDEX, user.getFinger(0).index);

        byte[] fingerprints_0 = user.getFinger(0).getTemplate(0);
        byte[] fingerprints_1 = user.getFinger(0).getTemplate(1);
        values.put(KEY_FINGER_DATA_0, fingerprints_0);
        values.put(KEY_FINGER_DATA_1, fingerprints_1);

        values.put(KEY_CARD, user.getCardNumber());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public void update(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getUserID());
        values.put(KEY_NAME, user.getUserName());

        values.put(KEY_FINGER_ID, user.getFingerID(0));

        values.put(KEY_FINGER_INDEX, user.getFinger(0).index);

        byte[] fingerprints_0 = user.getFinger(0).getTemplate(0);
        byte[] fingerprints_1 = user.getFinger(0).getTemplate(1);
        values.put(KEY_FINGER_DATA_0, fingerprints_0);
        values.put(KEY_FINGER_DATA_1, fingerprints_1);

        values.put(KEY_CARD, user.getCardNumber());

        db.update(TABLE_USER, values, KEY_ID + "=" + user.getUserID(), null);
        db.close();
    }

    public void delete(String userID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, KEY_ID + "=" + userID, null);
        db.close();
    }

    public ArrayList<User> getAll() {
        ArrayList<User> userList = new ArrayList<User>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserID(cursor.getString(0));
                user.setUserName(cursor.getString(1));
                Finger finger = new Finger();
                finger.id = cursor.getInt(2);
                finger.index = cursor.getInt(3);
                finger.setTemplate(0, cursor.getBlob(4));
                finger.setTemplate(1, cursor.getBlob(5));
                user.addFinger(finger, null);
                user.setCardNumber(cursor.getString(6));

                userList.add(user);
            } while (cursor.moveToNext());
        }

        return userList;
    }
}
