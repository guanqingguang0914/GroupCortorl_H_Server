package com.partnerx.roboth_server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.partnerx.sqlite.DBRobotHelper;
import com.partnerx.sqlite.ManagerSqldb;
import com.partnerx.sqlite.SQLBeanRobot;

public class SetDanceActivity extends Activity {
    private static String DB_NAME = "manager.db";
    private static int DB_VERSION = 1;
    public static int POSTION = 0;
    public ListView monitorlistview;
    public static Cursor cursor;
    public static SQLiteDatabase db;
    private DBRobotHelper dbHelper;
    private MonitorListAdapter monitorlistAdapter;
    public static ManagerSqldb ManagerSqldb;
    public Context contextSql;
    private Toast toast;
    public List<String> binfileList;
    public ArrayAdapter<String> binfileAdapter;
    public static boolean save = true;

    private EditText Agid1;
    private EditText Agid2;
    private EditText Atime;
    private Spinner Abinfile;

    private Button monitoradd;
    private Button monitorselect;
    private Button monitorupdate;
    private Button monitordelete;
    private Button monitorsave;
    private Button sendDance;
    private Button back;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setdance);
        binfileList = arrToList(DataBuffer.showbinStrArr);

        Agid1 = (EditText) findViewById(R.id.monitorID);
        Agid2 = (EditText) findViewById(R.id.monitorIDend);
        Atime = (EditText) findViewById(R.id.sleeptime);
        Abinfile = (Spinner) findViewById(R.id.monitorTypeSpinner);
        monitorlistview = (ListView) findViewById(R.id.monitorlistview);

        contextSql = this.getApplicationContext();
        try {
            DataBuffer.managerList.clear(); // 退出时没有清空的
            DataBuffer.managerList = ManagerSqldb.GetInstance().getManager(contextSql);
        } catch (Exception e) {
            Log.i("ManagerAc ", "managerList.clear()出错");
        }
        monitorlistAdapter = new MonitorListAdapter();
        monitorlistview.setAdapter(monitorlistAdapter);

        // 适配器
        binfileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, binfileList);
        binfileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置样式
        Abinfile.setAdapter(binfileAdapter);// 加载适配器

        sendDance = (Button) findViewById(R.id.senddance);
        sendDance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!save) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this); //
                    // dialog.setIcon(R.drawable.ic_launcher);// 窗口头图标
                    dialog.setTitle(R.string.notice);// 窗口名
                    dialog.setMessage(R.string.save_done);
                    dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                doorsave(contextSql, DataBuffer.managerList);
                                save = true;
                                sendMessage();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("doorsave", "doorsave 没有保存");
                            }
                        }
                    });
                    dialog.setNegativeButton(R.string.no_save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            save = true;
                            sendMessage();
                        }
                    });
                    dialog.show();
                } else {
                    sendMessage();
                }
            }
        });

        monitoradd = (Button) findViewById(R.id.monitoradd);
        monitoradd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int start = 0;
                int end = 0;
                String monStart = Agid1.getText().toString().trim();
                String monEnd = Agid2.getText().toString().trim();
                String monTime = Atime.getText().toString().trim();
                int monitortype = 0;

                //1.0.0.15版本这里暂时关闭，16打开
                if ((monEnd == null || monEnd.equals("")) && (monStart == null || monStart.equals(""))) {
                    toast = Toast.makeText(getApplicationContext(), R.string.startid_end_cannt_null, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                } else if ((monStart == null || monStart.equals("")) && (monEnd != null && !monEnd.equals(""))) {
                    end = Integer.parseInt(monEnd);
                    start = end;
                } else if ((monEnd == null || monEnd.equals("")) && (monStart != null && !monStart.equals(""))) {
                    start = Integer.parseInt(monStart);
                    end = start;
                } else {
                    end = Integer.parseInt(monEnd);
                    start = Integer.parseInt(monStart);
                    if (start > end ) {
                        toast = Toast.makeText(getApplicationContext(), R.string.end_start_id, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                    if (end > 255) {
                        toast = Toast.makeText(getApplicationContext(), R.string.end_team, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                    //start_team_0
                    if (start == 0) {
                        toast = Toast.makeText(getApplicationContext(), R.string.start_team_0, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                }

                String regEx = "^[0]\\d+$";
                monitortype = Abinfile.getSelectedItemPosition();
                if (monTime == null || "".equals(monTime) || monTime.matches(regEx)) {
                    Log.e("monitortype","monTime.matches(regEx) = " + monTime.matches(regEx));
                    toast = Toast.makeText(getApplicationContext(), R.string.delay_time_null, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                int uidf;
                boolean bhave;
                SQLBeanRobot item;
                for (int i = start; i <= end; i++) {
                    uidf = i;
                    //15关闭，16打开
                    bhave = false;
                    for (SQLBeanRobot item0 : DataBuffer.managerList) {
                        if (Integer.parseInt(item0.getGid()) == uidf) {
                            bhave = true;
                            break;
                        }
                    }
                    if (bhave) {
                        toast = Toast.makeText(getApplicationContext(), R.string.notice_address, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                        toast.show();
                        continue;
                    }

                    item = new SQLBeanRobot(uidf, monTime, binfileList.get(monitortype));
                    DataBuffer.managerList.add(item);
                }
                monitorlistAdapter = new MonitorListAdapter();
                monitorlistview.setAdapter(monitorlistAdapter);
                monitorlistAdapter.notifyDataSetChanged();
                save = false;
                resetForm();
            }
        });

        monitorupdate = (Button) findViewById(R.id.monitorupdate);
        monitorupdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String monitorTime0 = "";
                int binfile0 = 0;

                monitorTime0 = Atime.getText().toString().trim();
                binfile0 = Abinfile.getSelectedItemPosition();

                if (monitorTime0 == null | "".equals(monitorTime0)) {
                    toast = Toast.makeText(getApplicationContext(), R.string.delay_time_null, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    update(POSTION, monitorTime0, binfileList.get(binfile0));
                    monitorlistAdapter.notifyDataSetChanged();
                    save = false;
                    resetForm();
                }
            }
        });

        monitordelete = (Button) findViewById(R.id.monitordelete);
        // 绑定事件源和监听器对象
        monitordelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this); //
                // dialog.setIcon(R.drawable.ic_launcher);// 窗口头图标
                dialog.setTitle(R.string.notice);// 窗口名
                dialog.setMessage(R.string.sure_delete);
                dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DataBuffer.managerList.clear();
                            monitorlistview.setAdapter(monitorlistAdapter);
                            monitorlistAdapter.notifyDataSetChanged();
                            save = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        save = false;
                    }
                });
                dialog.show();
            }
        });

        monitorsave = (Button) findViewById(R.id.monitorsave);
        // 绑定事件源和监听器对象
        monitorsave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this); //
                // dialog.setIcon(R.drawable.ic_launcher);// 窗口头图标
                dialog.setTitle(R.string.notice);// 窗口名
                dialog.setMessage(R.string.save_this_done);
                dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            doorsave(contextSql, DataBuffer.managerList);
                            save = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("doorsave", "doorsave 没有保存");
                        }
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        save = false;
                    }
                });
                dialog.show();
            }
        });
        back = (Button) findViewById(R.id.managerback);
        // 绑定事件源和监听器对象
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!save) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this); //
                    // dialog.setIcon(R.drawable.ic_launcher);// 窗口头图标
                    dialog.setTitle(R.string.notice);// 窗口名
                    dialog.setMessage(R.string.save_done);
                    dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                doorsave(contextSql, DataBuffer.managerList);
                                save = true;
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("doorsave", "doorsave 没有保存");
                            }
                        }
                    });
                    dialog.setNegativeButton(R.string.no_save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            save = true;
                            finish();
                        }
                    });
                    dialog.show();
                } else
                    finish();
                // managerList.clear();//不能清空，activity退出时，存储数据库操作还没有完成，可能会没存上
                // busList.clear();
            }
        });

        monitorlistAdapter = new MonitorListAdapter();
        monitorlistview.setAdapter(monitorlistAdapter);
        monitorlistview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int postion, long arg3) {
                setSelectedValues(postion);
            }
        });

    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    /**
     * 发送数据
     */
    private void sendMessage() {
        DataBuffer.comID = (byte) 40;
        new Thread(new UdpBroadcast(mHandler)).start();
    }

    private class MonitorListAdapter extends BaseAdapter {
        public MonitorListAdapter() {
            super();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return DataBuffer.managerList.size();
        }

        @Override
        public Object getItem(int postion) {
            // TODO Auto-generated method stub
            return postion;
        }

        @Override
        public long getItemId(int postion) {
            // TODO Auto-generated method stub
            return postion;
        }

        @Override
        public View getView(final int postion, View view, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == view) {
                holder = new ViewHolder();

                view = getLayoutInflater().inflate(R.layout.monitorlistview, null);
                holder.gid = (TextView) view.findViewById(R.id.gid);
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.binfile = (TextView) view.findViewById(R.id.binfile);
                holder.delet = (Button) view.findViewById(R.id.delet);// listview里面放button，listview将失去焦点，点击没反应但button有反应
                // Log.i("",
                // "          每一行都初始化？ ？       managerdoorlistview初始化完成？？？");
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.gid.setText("" + DataBuffer.managerList.get(postion).getGid());//
            holder.time.setText("" + DataBuffer.managerList.get(postion).getTime());
            holder.binfile.setText("" + DataBuffer.managerList.get(postion).getBinfile());
            /* 删除表数据 */
            holder.delet.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this); //
                    // dialog.setIcon(R.drawable.ic_launcher);// 窗口头图标
                    dialog.setTitle(R.string.notice);// 窗口名
                    dialog.setMessage(R.string.delete_data);
                    dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                DataBuffer.managerList.remove(postion);
                                notifyDataSetChanged();
                                // doorlistview.setAdapter(new
                                // DoorListAdapter());
                                save = false;
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    });
                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }
            });
            return view;
        }
    }

    public static class ViewHolder {
        public TextView gid;
        public TextView time;
        public TextView binfile;
        public Button delet;
    }

    /* 修改表数据并ListView显示更新 */
    public void update(final int postion, final String time0, final String binfile0) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(SetDanceActivity.this);
        // dialog.setIcon(R.drawable.ic_launcher);//窗口头图标
        dialog.setTitle(R.string.notice);// 窗口名
        dialog.setMessage(R.string.grade_data);
        dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    // 选中查询出来的行获取焦点，
                    DataBuffer.managerList.get(postion).setGid("" + DataBuffer.managerList.get(postion).getGid());
                    DataBuffer.managerList.get(postion).setTime("" + time0);
                    DataBuffer.managerList.get(postion).setBinfile("" + binfile0);
                    // notifyDataSetChanged();
                    monitorlistview.setAdapter(new MonitorListAdapter());

                    resetForm();

                } catch (Exception e) {
                    Toast toastl = Toast.makeText(getApplicationContext(), R.string.click_turn, Toast.LENGTH_SHORT); // cursor.getString(1)必须是单条记录否则报错
                    toastl.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    toastl.show();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    /* 设置选中listview的值 */
    public void setSelectedValues(int postion) {
        POSTION = postion;
        // Log.i("", "                   设置选中doorlistview的值");
        Agid1.setText(DataBuffer.managerList.get(POSTION).getGid());
        Agid2.setText(DataBuffer.managerList.get(POSTION).getGid());
        Atime.setText(DataBuffer.managerList.get(POSTION).getTime());
        int iid = 0;
        String binfiles = DataBuffer.managerList.get(POSTION).getBinfile();
        Abinfile.setSelection(iid, true);// 给spinner赋初值
        for (String binf : binfileList) {
            if (binf.equals(binfiles)) {
                Abinfile.setSelection(iid, true);// 给spinner赋初值
                break;
            } else
                iid++;
        }

    }

    /* 重值form */
    public void resetForm() {
        Log.i("", "                  /* 重值form */");
        Agid1.setText("");
        Agid2.setText("");
        Atime.setText("");
        Abinfile.setSelection(0, true);// 给spinner赋初值

    }

    public void doorsave(Context context, List<SQLBeanRobot> robot) {
        if (ManagerSqldb.GetInstance().deletTable(context)) {// 先清空表再重新添加
            if (!ManagerSqldb.GetInstance().addManagerRecord(context, robot)) {
                Log.i("   保存doorsave  ", "         批量添加数据到数据库表失败");
                toast = Toast.makeText(getApplicationContext(), R.string.save_fail, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            Log.i("   保存doorsave  ", "         清空数据库表失败");
            toast = Toast.makeText(getApplicationContext(), R.string.save_fail, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
            toast.show();

        }
    }

    public List<String> arrToList(String[] str) {
        List<String> llist = new ArrayList<String>();
        for (String str0 : str) {
            llist.add(str0);
        }
        return llist;
    }

}
