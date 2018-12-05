package com.partnerx.roboth_server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库辅助类
 *
 * @author lz
 * @date 2017-3-14
 */
public class DBHelper extends SQLiteOpenHelper {

    /**
     * 数据库名称
     */
    private static String DATABASE_NAME = "HRobot";

    /**
     * 数据库版本号
     */
    private static int DATABASE_VERSION = 1;

    /**
     * 机器人分组表名
     */
    public static String DATABASE_TABLE = "robot_group";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        arg0.execSQL("create table " + DATABASE_TABLE + " (" + SQLDatabase.ID
                + " integer PRIMARY KEY AUTOINCREMENT NOT NULL," + SQLDatabase.GROUP_ID + " int,"
                + SQLDatabase.GROUP_DEFER + " int, " + SQLDatabase.GROUP_DANCE + " varchar, " + SQLDatabase.ROBORT_IP
                + " varchar, " + SQLDatabase.ROBORT_NUM + " int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

}
