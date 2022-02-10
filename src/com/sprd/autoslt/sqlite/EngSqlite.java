
package com.sprd.autoslt.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.util.StationItem;
import com.sprd.autoslt.util.TestColumns;
import com.sprd.autoslt.util.TestItem;

public class EngSqlite {
    private static final String TAG = "EngSqlite";
    private Context mContext;
    private SQLiteDatabase mSqLiteDatabase = null;

    private static EngSqlite mEngSqlite;
    private ArrayList<StationItem> mStationlist;
    private Map<String, String> map ;

    private static final boolean INIT_TESTCASE_BY_PC = true;

    public static synchronized EngSqlite getInstance(Context context) {
        if (mEngSqlite == null) {
            mEngSqlite = new EngSqlite(context);
        }
        return mEngSqlite;
    }

    private EngSqlite(Context context) {
        mContext = context;
        try {
             ValidationToolsDatabaseHelper databaseHelper = new ValidationToolsDatabaseHelper(mContext);
             mSqLiteDatabase = databaseHelper.getWritableDatabase();
             map = new HashMap<String, String>();
            } catch (Exception e) {
              // TODO: handle exception
                  e.printStackTrace();
            }
    }


    public void setStationList(ArrayList<StationItem> list) {
        mStationlist = list;
    }

    public Map<String, String> getMap() {
        ArrayList<StationItem> list = queryStationData();
        if (list != null && list.size() >0) {
            for (int i = 0; i < list.size(); i++) {
                map.put(list.get(i).getStationName(), list.get(i).getStationResult());
            }
        }
        return map;
    }

    public int getVaule(){
        int result = 0;
        int  pass_count =0;
        int  nt_count =0;
        int  fail_count = 0;
        int  go_count =0;
        for (String value : map.values()) {
            String[] val = value.split("\\^");
            Log.d("pei.li", "val[0] = " +val[0]);
            if (val[0].equalsIgnoreCase("pass")) {
                pass_count++;
            }else if (val[0].equalsIgnoreCase("nt")) {
                nt_count++;
            }else if (val[0].equalsIgnoreCase("fail")) {
                fail_count ++;
            }else if (val[0].equalsIgnoreCase("go")) {
                go_count ++;
            }
        }
        Log.d("pei.li", "map.size() = " +map.size()+"; pass_count = "+pass_count);
        Log.d("pei.li", "map.size() = " +map.size()+"; nt_count = "+nt_count);
        Log.d("pei.li", "map.size() = " +map.size()+"; fail_count = "+fail_count);
        Log.d("pei.li", "map.size() = " +map.size()+"; go_count = "+go_count);
        if (pass_count == map.size()&& pass_count == TestColumns.STATION_COLUMN.length) {
            result =  1;
        }else if (nt_count == map.size()&& nt_count == TestColumns.STATION_COLUMN.length) {
            result = 3;
        }else if (fail_count != 0 ){
            result =2;
        }else {
            result =4;
        }
        return result;
    }

    public int getResult(){
        int result = 0;
        int  pass_count =0;
        int  nt_count =0;
        int  fail_count = 0;
        int  go_count =0;
        for (int i = 0; i < mStationlist.size(); i++) {
            if (mStationlist.get(i).getStationResult().equalsIgnoreCase("pass")) {
                pass_count++;
            }else if (mStationlist.get(i).getStationResult().equalsIgnoreCase("nt")) {
                nt_count++;
            }else if (mStationlist.get(i).getStationResult().equalsIgnoreCase("fail")) {
                fail_count++;
            }else if (mStationlist.get(i).getStationResult().equalsIgnoreCase("go")){
                go_count ++;
            }
        }
        Log.d("pei.li", "mStationlist.size() = " +mStationlist.size()+"; pass_count = "+pass_count);
        Log.d("pei.li", "mStationlist.size() = " +mStationlist.size()+"; nt_count = "+nt_count);
        Log.d("pei.li", "mStationlist.size() = " +mStationlist.size()+"; fail_count = "+fail_count);
        Log.d("pei.li", "mStationlist.size() = " +mStationlist.size()+"; go_count = "+go_count);
        if (pass_count == mStationlist.size()&& pass_count == TestColumns.STATION_COLUMN.length  ) {
            result =  1;
        }else if (nt_count == mStationlist.size()&& nt_count == TestColumns.STATION_COLUMN.length) {
            result = 3;
        }else if (fail_count != 0) {
            result =2;
        }else {
            result =4;
        }
        return result;
    }
    public long insertValue(String station,String name, String value, String note) {
        long bln = 0;
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_STRING2INT_STATION, station);
        values.put(SLTConstant.ENG_STRING2INT_NAME, name);
        values.put(SLTConstant.ENG_STRING2INT_VALUE, value);
        values.put(SLTConstant.ENG_STRING2INT_NOTE, note);
        bln = mSqLiteDatabase.insert(SLTConstant.ENG_STRING2INT_TABLE, null, values);
        Log.d("huasong", "insert:" + name + ":" + value);
        return bln;
    }

    public void updateData(String name, String value, String note) {
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_STRING2INT_NAME, name);
        values.put(SLTConstant.ENG_STRING2INT_VALUE, value);
        values.put(SLTConstant.ENG_STRING2INT_NOTE, note);
        if(queryData(name)) {
            mSqLiteDatabase.update(SLTConstant.ENG_STRING2INT_TABLE, values,
                    SLTConstant.ENG_STRING2INT_NAME + "= \'" + name + "\'", null);
        } else {
            mSqLiteDatabase.insert(SLTConstant.ENG_STRING2INT_TABLE, null, values);
        }
    }

    public void updateData(String name, String value, String note,boolean insertable) {
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_STRING2INT_NAME, name);
        values.put(SLTConstant.ENG_STRING2INT_VALUE, value);
        values.put(SLTConstant.ENG_STRING2INT_NOTE, note);
        if(queryData(name)) {
            mSqLiteDatabase.update(SLTConstant.ENG_STRING2INT_TABLE, values,
                    SLTConstant.ENG_STRING2INT_NAME + "= \'" + name + "\'", null);
        } else if(insertable){
            mSqLiteDatabase.insert(SLTConstant.ENG_STRING2INT_TABLE, null, values);
        }
    }

    public void updateData(String station,String name, String value, String note) {
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_STRING2INT_STATION, station);
        values.put(SLTConstant.ENG_STRING2INT_NAME, name);
        values.put(SLTConstant.ENG_STRING2INT_VALUE, value);
        values.put(SLTConstant.ENG_STRING2INT_NOTE, note);
        if(queryData(name)) {
            mSqLiteDatabase.update(SLTConstant.ENG_STRING2INT_TABLE, values,
                    SLTConstant.ENG_STRING2INT_NAME + "= \'" + name + "\'", null);
        } else {
            mSqLiteDatabase.insert(SLTConstant.ENG_STRING2INT_TABLE, null, values);
        }
    }

    public void updateHistoryData(String project,String id,  String result) {
    Log.d("pei.li","updateHistoryData  id =" +id +"; project = "+project +"; result = "+project);
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_HISTORY_TABLE_CASE, project);
        values.put(SLTConstant.ENG_HISTORY_TABLE_ID, id);
        values.put(SLTConstant.ENG_HISTORY_TABLE_RESULT, result);
        if(queryHistoryData(project)) {
            mSqLiteDatabase.update(SLTConstant.ENG_HISTORY_TABLE, values,
                    SLTConstant.ENG_HISTORY_TABLE_CASE + "= \'" + project + "\'", null);
        } else {
            Log.d("pei.li","mSqLiteDatabase.insert");
//            mSqLiteDatabase.delete(SLTConstant.ENG_HISTORY_TABLE, SLTConstant.ENG_HISTORY_TABLE_ID + "!=?", new String[]{id});
            mSqLiteDatabase.insert(SLTConstant.ENG_HISTORY_TABLE, null, values);
        }
    }

    public ArrayList<TestItem> queryHistoryData() {
/*        try {
            Cursor cursor = mSqLiteDatabase.query(SLTConstant.ENG_HISTORY_TABLE,
                    new String[] {
                        SLTConstant.ENG_HISTORY_TABLE_CASE,
                        SLTConstant.ENG_HISTORY_TABLE_ID,
                        SLTConstant.ENG_HISTORY_TABLE_RESULT
                    }, null, null, null, null, null);
            if (cursor != null) {
                StringBuffer sb = new StringBuffer();
                while (cursor.moveToNext()) {
                    sb.append(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_ID)));
                    sb.append("^");
                    sb.append(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_CASE)));
                    sb.append("^");
                    sb.append(PhaseCheckParse.getSerialNumber());
                    sb.append("^");
                    sb.append(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_RESULT)));
                }
                cursor.close();
                return sb.toString();
            }
        } catch (Exception e) {
        }
        return "null";*/               

        ArrayList<TestItem> mStationHistoryList = new ArrayList<TestItem>();
        try {
            Cursor cursor = mSqLiteDatabase.query(SLTConstant.ENG_HISTORY_TABLE,
                    new String[] {
                        SLTConstant.ENG_HISTORY_TABLE_CASE,
                        SLTConstant.ENG_HISTORY_TABLE_ID,
                        SLTConstant.ENG_HISTORY_TABLE_RESULT
                    }, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {                   
                    TestItem sItem = new TestItem();
                    sItem.setTestCase(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_CASE)));
                    sItem.setTestID(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_ID)));
                    sItem.setTestResult(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_HISTORY_TABLE_RESULT)));
                    mStationHistoryList.add(sItem);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return mStationHistoryList;
        }
        return mStationHistoryList;
    }

    public ArrayList<TestItem> queryData() {
        ArrayList<TestItem> mTestItemList = new ArrayList<TestItem>();
        try {
            Cursor cursor = mSqLiteDatabase.query(SLTConstant.ENG_STRING2INT_TABLE,
                    new String[] {
                        SLTConstant.ENG_STRING2INT_ID,
                        SLTConstant.ENG_STRING2INT_STATION,
                        SLTConstant.ENG_STRING2INT_NAME,
                        SLTConstant.ENG_STRING2INT_VALUE,
                        SLTConstant.ENG_STRING2INT_NOTE
                    }, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    TestItem item = new TestItem();
                    item.setTestID(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_ID)));
                    item.setTestStation(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_STATION)));
                    item.setTestCase(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_NAME)));
                    item.setTestResult(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_VALUE)));
                    item.setmTestNote(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_NOTE)));
                    mTestItemList.add(item);
                }
                cursor.close();
            }
        } catch (Exception e) {
            return mTestItemList;
        }
        return mTestItemList;
    }

    public boolean queryData(String name) {
        try {
            Cursor c = mSqLiteDatabase.query(SLTConstant.ENG_STRING2INT_TABLE,
                    new String[] {
                    SLTConstant.ENG_STRING2INT_NAME, SLTConstant.ENG_STRING2INT_VALUE,
                    SLTConstant.ENG_STRING2INT_NOTE
                    },
                    SLTConstant.ENG_STRING2INT_NAME + "= \'" + name + "\'",
                    null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean queryStation(String station) {
        try {
            Cursor c = mSqLiteDatabase.query(SLTConstant.ENG_STRING2INT_TABLE,
                    new String[] {
                     SLTConstant.ENG_STRING2INT_STATION,
                    SLTConstant.ENG_STRING2INT_NAME, SLTConstant.ENG_STRING2INT_VALUE,
                    SLTConstant.ENG_STRING2INT_NOTE
                    },
                    SLTConstant.ENG_STRING2INT_STATION + "= \'" + station + "\'",
                    null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean deleteByStation(String station) {
        int ret = mSqLiteDatabase.delete(SLTConstant.ENG_STRING2INT_TABLE, SLTConstant.ENG_STRING2INT_STATION + "=?", new String[]{station});
        Log.d(TAG, "deleteByStation ret=" + ret);
        return ret > 0;
    }

    public boolean queryDataPass(String name) {
        if (mSqLiteDatabase == null) {
            Log.e(TAG, "queryDataPass mSqLiteDatabase == null");
            return false;
        }

        Cursor cursor = mSqLiteDatabase.query(SLTConstant.ENG_STRING2INT_TABLE,
                new String[] {
                    "value"
                }, "name=" + "\'" + name + "\'", null, null,
                null, null);
        Log.d(TAG, "queryDataPass name=" + name);
        Log.d(TAG, "queryDataPass cursor.count=" + cursor.getCount());
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                //2 == PASS,1 === FAIL, 0 == NOTTEST
                return cursor.getInt(cursor.getColumnIndex(SLTConstant.ENG_STRING2INT_VALUE)) == 2;
            } else {
                Log.d(TAG, "cursor.count <= 0");
                return false;
            }
        } catch (Exception ex) {
            Log.d(TAG, "exception");
            return false;
        } finally {
            Log.d(TAG, "fianlly");
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean queryHistoryData(String name) {
        try {
            Cursor c = mSqLiteDatabase.query(SLTConstant.ENG_HISTORY_TABLE,
                    new String[] { SLTConstant.ENG_HISTORY_TABLE_CASE,
                            SLTConstant.ENG_HISTORY_TABLE_ID,
                            SLTConstant.ENG_HISTORY_TABLE_RESULT },
                    SLTConstant.ENG_HISTORY_TABLE_CASE + "= \'" + name + "\'",
                    null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public int queryNotTestCount() {
        int bln = 0;
        if (mSqLiteDatabase == null)
            return bln;
        Cursor cur = mSqLiteDatabase.query(SLTConstant.DB_NAME, new String[] {
                "name", "value"
        },
                "value=?", new String[] {
                    "2"
                },
                null, null, null);
        if (cur != null) {
            bln = cur.getCount();
            cur.close();
        }
        return bln;
    }

    public int queryFailCount() {
        int bln = 0;
        if (mSqLiteDatabase == null)
            return bln;
        Cursor cur = mSqLiteDatabase.query(SLTConstant.DB_NAME, new String[] {
                "name", "value"
        },
                "value!=?", new String[] {
                    "1"
                },
                null, null, null);
        if (cur != null) {
            bln = cur.getCount();
            cur.close();
        }
        return bln;
    }
    
    public ArrayList<StationItem> queryStationData() {
        ArrayList<StationItem> mStationItemList = new ArrayList<StationItem>();

        try {
            Cursor cursor = mSqLiteDatabase.query(SLTConstant.ENG_STATION_TABLE,
                    new String[] {
                        SLTConstant.ENG_STATION_TABLE_NAME,
                        SLTConstant.ENG_STATION_TABLE_RESULT
                    }, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {                   
                    StationItem sItem = new StationItem();
                    sItem.setStationName(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STATION_TABLE_NAME)));
                    sItem.setStationResult(cursor.getString(cursor.getColumnIndex(SLTConstant.ENG_STATION_TABLE_RESULT)));
                    mStationItemList.add(sItem);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return mStationItemList;
        }
        return mStationItemList;
    }
    
    public void updateStationData(String name, String value) {
        Log.d("pei.li","updateStationData : name =" +name+"; value = "+value);
        ContentValues values = new ContentValues();
        values.put(SLTConstant.ENG_STATION_TABLE_NAME, name);
        values.put(SLTConstant.ENG_STATION_TABLE_RESULT, value);
        if(queryStationData(name)) {
            mSqLiteDatabase.update(SLTConstant.ENG_STATION_TABLE, values,
                    SLTConstant.ENG_STATION_TABLE_NAME + "= \'" + name + "\'", null);
        } else {
            mSqLiteDatabase.insert(SLTConstant.ENG_STATION_TABLE, null, values);
        }
    }    
    
    public boolean queryStationData(String name) {
        try {
            Cursor c = mSqLiteDatabase.query(SLTConstant.ENG_STATION_TABLE,
                    new String[] {
                    SLTConstant.ENG_STATION_TABLE_NAME, SLTConstant.ENG_STATION_TABLE_RESULT
                    },
                    SLTConstant.ENG_STATION_TABLE_NAME + "= \'" + name + "\'",
                    null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
    
    public void updateBootFlag(String flag){
        Log.d("pei.li","updateBootFlag : flag =" +flag);
        ContentValues values = new ContentValues();
        values.put(SLTConstant.BOOT_FLAG_RESULT, flag);
        Log.d("pei.li","queryBootFlag(flag) =" + queryBootFlag(flag));
        if (queryBootFlag(flag)) {
            mSqLiteDatabase.update(SLTConstant.BOOT_FLAG, values, SLTConstant.BOOT_FLAG_RESULT+ "= \'" + flag + "\'", null);
        }else {
            mSqLiteDatabase.delete(SLTConstant.BOOT_FLAG, SLTConstant.BOOT_FLAG_RESULT + "!=?", new String[]{flag});
            mSqLiteDatabase.insert(SLTConstant.BOOT_FLAG, null, values);
        }
        
    }
    
    public boolean queryBootFlag(String fg){
        try {
            Cursor c = mSqLiteDatabase.query(SLTConstant.BOOT_FLAG,
                    new String[] {
                    SLTConstant.BOOT_FLAG_RESULT
                }, SLTConstant.BOOT_FLAG_RESULT+ "= \'" + fg + "\'", null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
    
    public String queryBootFlag(){
        String result = null;
        try {
            Cursor cursor = mSqLiteDatabase.query(SLTConstant.BOOT_FLAG,
                    new String[] {
                        SLTConstant.BOOT_FLAG_RESULT,
                    }, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result = cursor.getString(cursor.getColumnIndex(SLTConstant.BOOT_FLAG_RESULT));
                }
                cursor.close();
                return result;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private static class ValidationToolsDatabaseHelper extends SQLiteOpenHelper {
        public ValidationToolsDatabaseHelper(Context context) {
            super(context, SLTConstant.DB_NAME, null, SLTConstant.DB_VERSION);
            Log.d(TAG, "ValidationToolsDatabaseHelper ---- ValidationToolsDatabaseHelper");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
//            db.execSQL("DROP TABLE IF EXISTS " + SLTConstant.DB_NAME + ";");
            db.execSQL("CREATE TABLE " + SLTConstant.ENG_STRING2INT_TABLE + " (" + BaseColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + SLTConstant.ENG_GROUPID_VALUE
                    + " INTEGER NOT NULL DEFAULT 0,"
                    + SLTConstant.ENG_STRING2INT_STATION + " TEXT,"
                    + SLTConstant.ENG_STRING2INT_NAME + " TEXT,"
                    + SLTConstant.ENG_STRING2INT_VALUE
                    + " INTEGER NOT NULL DEFAULT 0,"+ SLTConstant.ENG_STRING2INT_NOTE
                    + " TEXT NOT NULL DEFAULT NULL"+");");

            db.execSQL("CREATE TABLE " + SLTConstant.ENG_HISTORY_TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SLTConstant.ENG_HISTORY_TABLE_CASE + " TEXT,"
                    + SLTConstant.ENG_HISTORY_TABLE_ID + " TEXT,"
                    + SLTConstant.ENG_HISTORY_TABLE_RESULT
                    + " TEXT NOT NULL DEFAULT NULL" + ");");
            
            if(!INIT_TESTCASE_BY_PC){
                for(int i = 0; i < TestColumns.TEST_COLUMN.length; i++) {
                    db.execSQL("INSERT INTO " + SLTConstant.ENG_STRING2INT_TABLE + " VALUES ("
                            + " NULL, 0, "
                            + " '', "
                            + "'" + TestColumns.TEST_COLUMN[i] + "', 'NT',"
                            + "''"+ ");");
                }
            }
            
            for (int i = 0; i < TestColumns.STATION_COLUMN.length; i++) {
                db.execSQL("INSERT INTO " + SLTConstant.ENG_HISTORY_TABLE + " VALUES ("
                        + " NULL,"
                        + "'" + TestColumns.STATION_COLUMN[i] + "', 'NULL" + "', 'NT'"
                        + ");");
            }
            
            db.execSQL("CREATE TABLE " + SLTConstant.ENG_STATION_TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 //                   + SLTConstant.ENG_STATION_TABLE_ID + " TEXT,"
                    + SLTConstant.ENG_STATION_TABLE_NAME + " TEXT,"
                    + SLTConstant.ENG_STATION_TABLE_RESULT
                    + " TEXT NOT NULL DEFAULT NULL" + ");");
            
            for (int i = 0; i < TestColumns.STATION_COLUMN.length; i++) {
                db.execSQL("INSERT INTO " + SLTConstant.ENG_STATION_TABLE + " VALUES ("
                        + " NULL,"
                        + "'" + TestColumns.STATION_COLUMN[i] + "', 'NT'"
                        + ");");
            }
            Log.d(TAG, "onCreate ---- create BOOT_FLAG");
            db.execSQL("CREATE TABLE " + SLTConstant.BOOT_FLAG + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SLTConstant.BOOT_FLAG_RESULT 
                    + " INTEGER NOT NULL DEFAULT 0" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + SLTConstant.DB_NAME + ";");
                onCreate(db);
            }
        }
    }
}
