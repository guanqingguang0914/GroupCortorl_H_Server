package com.partnerx.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 实现对表的创建、更新、变更列名操作
 */
public class DBRobotHelper extends SQLiteOpenHelper {
    public static final String TB_NAME = "robotmessage";

    public DBRobotHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 创建新表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + "(" + SQLBeanRobot.DID + " String primary key,"
                + SQLBeanRobot.GID + " String," + SQLBeanRobot.TIME + " String," + SQLBeanRobot.BINFILE + " String"
                + ")");
    }

    /**
     * 当检测与前一次创建数据库版本不一样时，先删除表再创建新表
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
        onCreate(db);
    }

    /**
     * 变更列名
     *
     * @param db
     * @param oldColumn
     * @param newColumn
     * @param typeColumn
     */
    public void updateColumn(SQLiteDatabase db, String oldColumn, String newColumn, String typeColumn, String TB_NAME) {
        try {
            db.execSQL("ALTER TABLE " + TB_NAME + " CHANGE " + oldColumn + " " + newColumn + " " + typeColumn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
