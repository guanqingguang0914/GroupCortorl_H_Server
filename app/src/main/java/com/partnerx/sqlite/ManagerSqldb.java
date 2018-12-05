package com.partnerx.sqlite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.partnerx.roboth_server.ContextUtil;

public class ManagerSqldb {
    public static String DB_NAME = "robotmessage.db";
    public static int DB_VERSION = 1;
    private static DBRobotHelper dbHelper;//
    private static SQLiteDatabase db;

    private static StringBuffer sql_insert;
    private List<SQLBeanRobot> Door;

    public static Cursor cursor;
    public static boolean busing = false;
    // 单例
    private static ManagerSqldb managerSqldb = null;
    private static AtomicInteger mOpenCounter = new AtomicInteger();

    // 实例化
    public static synchronized ManagerSqldb GetInstance() {
        if (managerSqldb == null) {
            managerSqldb = new ManagerSqldb();
        }
        return managerSqldb;
    }

    public synchronized static void sqlstart(Context context) {// 建立数据库连接
        // 可以构造函数进行初始化
        // Context context = ContextUtil.getInstance();
        if (mOpenCounter.incrementAndGet() == 1) {
            try {
                /* 初始化并创建数据库 */
                dbHelper = new DBRobotHelper(context, DB_NAME, null, DB_VERSION);
				/* 创建表 */
                db = dbHelper.getWritableDatabase(); // 调用SQLiteHelper.OnCreate()
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // 当版本变更时会调用SQLiteHelper.onUpgrade()方法重建表 注：表以前数据将丢失
                ++DB_VERSION;
                dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
            }
        }
    }

    public synchronized static void sqlclose() {// 关闭数据库连接
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            db.close();
        }
    }

    String str[][];
    Object obj[][];

    public void setValue() {//
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                str[i][j] = i + "" + j;
                obj[i][j] = i + "" + j;// 可以放任何对象类型，还可以几种子类型混合放置
            }
        }
    }

    // 查询结果放在list中，然后传出更新显示到listview表中，不需要条件查询，所以就没有放传查询条件参数
    public static synchronized List<SQLBeanRobot> getManager(Context context) {
        // Context context = ContextUtil.getInstance();
        if (busing == false) {
            busing = true;
            List<SQLBeanRobot> Manager = new ArrayList<SQLBeanRobot>();
            try {
                sqlstart(context);
                cursor = db.query(DBRobotHelper.TB_NAME, null, null, null, null, null, SQLBeanRobot.GID + " asc ");// desc降序
                cursor.moveToFirst();
                // 通过判断cursor.moveToFirst()的值为true或false来确定查询结果是否为空。cursor.moveToNext()
                // 是用来做循环的，一般这样来用：while(cursor.moveToNext()){
                while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
                    SQLBeanRobot manager = new SQLBeanRobot();
                    manager.setDid(cursor.getString(0));//
                    manager.setGid(cursor.getString(1));//
                    manager.setTime(cursor.getString(2));
                    manager.setBinfile(cursor.getString(3));
                    Manager.add(manager);
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("", "查询数据库表robotmessage出错" + e);
            } finally {
                sqlclose();// 关闭数据库连接
                cursor = null;
                db = null;
                dbHelper = null;
                busing = false;
            }

            return Manager;
        } else {
            return null;
        }
    }

    public List<SQLBeanRobot> getManager() {
        Context context = ContextUtil.getInstance();// 获取应用的context
        return getManager(context);
    }

    public List<SQLBeanRobot> getManager(int id) {
        Context context = ContextUtil.getInstance();// 获取应用的context
        if (busing == false) {
            busing = true;
            List<SQLBeanRobot> Manager = new ArrayList<SQLBeanRobot>();
            try {
                sqlstart(context);
                cursor = db.query(DBRobotHelper.TB_NAME, null, SQLBeanRobot.GID + " = '" + id + "'", null, null, null,
                        SQLBeanRobot.GID + " asc ");// desc降序
                cursor.moveToFirst();
                // 通过判断cursor.moveToFirst()的值为true或false来确定查询结果是否为空。cursor.moveToNext()
                // 是用来做循环的，一般这样来用：while(cursor.moveToNext()){
                while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
                    SQLBeanRobot manager = new SQLBeanRobot();
                    manager.setDid(cursor.getString(0));//
                    manager.setGid(cursor.getString(1));//
                    manager.setTime(cursor.getString(2));
                    manager.setBinfile(cursor.getString(3));
                    Manager.add(manager);
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("", "查询数据库表robotmessage出错" + e);
            } finally {
                sqlclose();// 关闭数据库连接
                cursor = null;
                db = null;
                dbHelper = null;
                busing = false;
            }

            return Manager;
        } else {
            return null;
        }
    }

    public static synchronized boolean addManagerRecord(Context context, List<SQLBeanRobot> Door) {

        // Context context = ContextUtil.getInstance();
        if (busing == false) {
            busing = true;
            try {
                sqlstart(context);
                String sql = "insert into robotmessage(" + SQLBeanRobot.GID + "," + SQLBeanRobot.TIME + ","
                        + SQLBeanRobot.BINFILE + ") values (?, ?, ?)";
                Log.i("", "" + sql);
                SQLiteStatement statement = db.compileStatement(sql);
                db.beginTransaction(); // 手动设置开始事务
                try { // 批量处理操作
                    for (SQLBeanRobot manager : Door) {
                        // statement.bindString(0, manager.getDId());
                        statement.bindString(1, manager.getGid());
                        statement.bindString(2, manager.getTime());
                        statement.bindString(3, manager.getBinfile());
                        statement.executeInsert();
                    }
                    db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交。
                    // 在setTransactionSuccessful和endTransaction之间不进行任何数据库操作
                } catch (Exception e) {
                    Log.i("", "数据库robotmessage开启事物失败");
                } finally {
                    db.endTransaction(); // 处理完成//结束事务
                }

            } catch (Exception e) {
                Log.i("", "批量增加数据到表robotmessage时出错");
            } finally {
                sqlclose();// 关闭数据库连接
                cursor = null;
                db = null;
                dbHelper = null;
                busing = false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static synchronized boolean deletTable(Context context) {
        // Context context = ContextUtil.getInstance();
        if (busing == false) {
            busing = true;
            try {
                sqlstart(context);
                db.execSQL("delete from " + DBRobotHelper.TB_NAME);
            } catch (Exception e) {
                Log.i("", "删除表robotmessage时出错");
            } finally {
                sqlclose();// 关闭数据库连接
                cursor = null;
                db = null;
                dbHelper = null;
                busing = false;
            }
            return true;
        } else {
            return false;
        }
    }
}
