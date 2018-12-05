package com.partnerx.roboth_server;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库增删改查操作类
 *
 * @author lz
 * @date 2017-3-14
 */
public class SQLDatabase {

    /**
     * 表字段
     */
    public static String ID = "id";
    public static String GROUP_ID = "group_id";
    public static String GROUP_DEFER = "group_defer";
    public static String GROUP_DANCE = "group_dance";
    public static String ROBORT_IP = "robort_ip";
    public static String ROBORT_NUM = "robort_num";

    // private static String

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public SQLDatabase(Context mContext) {
        dbHelper = new DBHelper(mContext);
        database = dbHelper.getWritableDatabase();
    }

    /**
     * 插入数据
     *
     * @param group_id
     * @param group_defer
     * @param group_dance
     * @param robort_ip
     * @param robort_num
     */
    public void installData(int group_id, int group_defer, String group_dance, String robort_ip, int robort_num) {
        ContentValues values = new ContentValues();
        values.put(GROUP_ID, group_id);
        values.put(GROUP_DEFER, group_defer);
        values.put(GROUP_DANCE, group_dance);
        values.put(ROBORT_IP, robort_ip);
        values.put(ROBORT_NUM, robort_num);
        database.insert(DBHelper.DATABASE_TABLE, null, values);
    }

    public void installOrUpdate() {

    }

}
