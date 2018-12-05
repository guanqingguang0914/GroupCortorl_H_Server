package com.partnerx.json;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
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

import com.partnerx.roboth_server.DataBuffer;
import com.partnerx.roboth_server.FileUtils;
import com.partnerx.roboth_server.R;
import com.partnerx.roboth_server.ServerRunnable;
import com.partnerx.roboth_server.UdpBroadcast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanqg on 2018/1/29.
 */

public class SetActionJsonActivity extends Activity implements View.OnClickListener {
    private Button json_back,action_add,action_delall,action_creatjson,set_role,action_play,file_send,action_sendjson,action_updata;
    private EditText action_role,action_time;
    private ListView action_listview;
    private Spinner action_style,action_name;
    private List<String> totalList;
    private List<String> binFileList = new ArrayList<>();
    private List<String> mp3FileList = new ArrayList<>();
    private List<String> jsonFileList = new ArrayList<>();
    private List<String> buTaiList;
    private String jsonFile = Environment
            .getExternalStorageDirectory()
            + File.separator+ "GroupControl" + File.separator;
    private String ACTIONFILE = Environment.getExternalStorageDirectory() +
            File.separator + "GroupControl";
    private List<String> actionStyle;//类型4种
    private String[] style = new String[]{"bin和MP3","bin播放","mp3播放","步态执行"};//类型4种
    private String[] name_butai = new String[]{"向前","踏步","向后","左转","右转","停止","跑步","跑步停止"};//步态对应name
    private int mCountDownPeroid = 0;
    private ArrayAdapter<String> nameAdapter;
    private List<Action> actionList = new ArrayList<>();//listView 对应的集合
    private MonitorListAdapter monitorlistAdapter;
    private int POSTION = 0;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0://bin和MP3
                case 1://bin播放
                    if(totalList!= null){
                        totalList.clear();
                    }
                    totalList.addAll(binFileList);
                    break;
                case 2://mp3播放
                    if(totalList!= null){
                        totalList.clear();
                    }
                    totalList.addAll(mp3FileList);
                    break;
                case 3://步态执行
                    if(totalList!= null){
                        totalList.clear();
                    }
                    totalList.addAll(buTaiList);
                    break;
            }
            nameAdapter.notifyDataSetChanged();
            action_name.setSelection(0,true);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setjson);
        totalList = new ArrayList<>();
        new Thread(new ServerRunnable(getLocalIpAddress())).start();// 打开服务器
        initData();
        initView();
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        DataBuffer.serverIP = ipAddress;
        // 返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    private void initData() {//获取数据
        if(binFileList!= null){
            binFileList.clear();
        }
        if(mp3FileList!= null){
            mp3FileList.clear();
        }
        if(buTaiList != null){
            buTaiList.clear();
        }
        if (actionList != null){
            actionList.clear();
        }
        FileUtils.getFileList_HasEx1(new File(ACTIONFILE),binFileList,1);
        FileUtils.getFileList_HasEx1(new File(ACTIONFILE),mp3FileList,2);
        buTaiList = arrToList(name_butai);
        if(totalList != null){
            totalList.addAll(binFileList);
        }
    }
    private void resetForm(){
        action_role.setText("");
        action_time.setText("");
        action_style.setSelection(0,true);
        action_name.setSelection(0,true);
    }
    private void initView() {
        json_back = (Button) findViewById(R.id.json_back);
        action_add = (Button) findViewById(R.id.action_add);
        action_updata = (Button) findViewById(R.id.action_updata);
        action_delall = (Button) findViewById(R.id.action_delall);
        action_creatjson = (Button) findViewById(R.id.action_creatjson);
        action_sendjson = (Button) findViewById(R.id.action_sendjson);
        action_play = (Button) findViewById(R.id.action_play);
        set_role = (Button) findViewById(R.id.set_role);
        file_send = (Button) findViewById(R.id.file_send);

        action_role = (EditText) findViewById(R.id.action_role);
        action_time = (EditText) findViewById(R.id.action_time);

        action_style = (Spinner) findViewById(R.id.action_style);
        action_name = (Spinner) findViewById(R.id.action_name);

        action_listview = (ListView) findViewById(R.id.action_listview);
        monitorlistAdapter = new MonitorListAdapter();
        action_listview.setAdapter(monitorlistAdapter);
        action_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSelectedValues(position);
            }
        });

        nameAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, totalList);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        action_name.setAdapter(nameAdapter);
        if(actionStyle!= null){
            actionStyle.clear();
        }
        actionStyle = arrToList(style);//类型sponner
        action_style.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountDownPeroid = position;
                mHandler.sendEmptyMessage(mCountDownPeroid);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCountDownPeroid = 0;
                mHandler.sendEmptyMessage(mCountDownPeroid);
            }
        });

        json_back.setOnClickListener(this);
        action_add.setOnClickListener(this);
        action_updata.setOnClickListener(this);
        action_delall.setOnClickListener(this);
        action_creatjson.setOnClickListener(this);
        action_sendjson.setOnClickListener(this);
        action_play.setOnClickListener(this);
        set_role.setOnClickListener(this);
        file_send.setOnClickListener(this);
    }
    public List<String> arrToList(String[] str) {
        List<String> llist = new ArrayList<String>();
        for (String str0 : str) {
            llist.add(str0);
        }
        return llist;
    }

    private void setSelectedValues(int position){//選中某一項
        POSTION = position;
        action_role.setText(actionList.get(position).getRole()+"");
        action_time.setText(actionList.get(position).getTime() + "");
        //暂时不加两个spinner
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.json_back:
//                finish();
                openJsonFile();
                break;
            case R.id.action_add:
                add();
                break;
            case R.id.action_delall:
                deletAll();
                break;
            case R.id.action_creatjson:
                creatJson();
                break;
            case R.id.action_sendjson://选择并发送json文件
                downLoadJson();
                break;
            case R.id.action_play://执行选择的jison
                playJsonSelect();
                break;
            case R.id.set_role:
                setRole();
                break;
            case R.id.action_updata:
                updata();
                break;
            case R.id.file_send:

                break;
        }
    }

    private void openJsonFile() {
        if(jsonFileList != null){
            jsonFileList.clear();
        }
        FileUtils.getFileList_HasEx1(new File(jsonFile),jsonFileList,3);
        if(jsonFileList == null){
            return;
        }
        final String[] jsonFile = (String[]) jsonFileList.toArray(new String[jsonFileList.size()]);
        new AlertDialog.Builder(SetActionJsonActivity.this).
                setTitle(R.string.file_choice)// 超出长度可以自动换行
                .setItems(jsonFile, new DialogInterface.OnClickListener() { // content
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String  binName = DataBuffer.BINFILE + File.separator + jsonFile[which];
                        //TODO
                        try {
                            readjsonToText(binName);
                            Thread.sleep(200);
                            actionList.clear();
                            actionList.addAll(list2);
                            monitorlistAdapter.notifyDataSetChanged();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 关闭alertDialog

            }
        }).show();
    }
    private List<Action> list2;
    private void readjsonToText(String binName) {
        try {
            FileInputStream fis =  new FileInputStream(binName);//
            JsonReader reader = new JsonReader(new InputStreamReader(fis,"utf-8"));
            list2 = new ArrayList<>();
            list2.clear();
            reader.beginArray();
            while(reader.hasNext()){
                int role = 0;
                int id = 0;
                String name = "";
                long time = 0;
                reader.beginObject();
                while(reader.hasNext()){
                    String style = reader.nextName();
                    if(style.equals("role")){
                        role = reader.nextInt();
                    }else if(style.equals("id")){
                        id = reader.nextInt();
                    }else if(style.equals("name")){
                        name = reader.nextString();
                    }else if(style.equals("time")){
                        time = reader.nextLong();
                    }else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                list2.add(new Action(role,id,name,time));
            }
            reader.endArray();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRole() {//同设置分组
        DataBuffer.comID = (byte) 12;
        new Thread(new UdpBroadcast(mHandler)).start();
    }

    private void playJsonSelect() {
        if(jsonFileList != null){
            jsonFileList.clear();
        }
        FileUtils.getFileList_HasEx1(new File(jsonFile),jsonFileList,3);
        if(jsonFileList == null){
            return;
        }
        final String[] jsonFile = (String[]) jsonFileList.toArray(new String[jsonFileList.size()]);

        new AlertDialog.Builder(SetActionJsonActivity.this).
                setTitle(R.string.file_choice)// 超出长度可以自动换行
                .setItems(jsonFile, new DialogInterface.OnClickListener() { // content
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataBuffer.filePath = DataBuffer.BINFILE + File.separator + jsonFile[which];
                        DataBuffer.binName = jsonFile[which];
                        DataBuffer.comID = (byte) 3;
                        new Thread(new UdpBroadcast(mHandler)).start();// 执行文件下载
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 关闭alertDialog

            }
        }).show();
    }

    private void downLoadJson() {
        if(jsonFileList != null){
            jsonFileList.clear();
        }
        FileUtils.getFileList_HasEx1(new File(jsonFile),jsonFileList,3);
        if(jsonFileList == null){
            return;
        }
        final String[] jsonFile = (String[]) jsonFileList.toArray(new String[jsonFileList.size()]);

        new AlertDialog.Builder(SetActionJsonActivity.this).
                setTitle(R.string.file_choice)// 超出长度可以自动换行
                .setItems(jsonFile, new DialogInterface.OnClickListener() { // content
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataBuffer.filePath = DataBuffer.BINFILE + File.separator + jsonFile[which];
                        DataBuffer.sendbinName = jsonFile[which];
                        DataBuffer.comID = (byte) 31;
                        new Thread(new UdpBroadcast(mHandler)).start();// 执行文件下载
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 关闭alertDialog

            }
        }).show();
    }

    private AlertDialog dialog;
    private void creatJson() {//生成json
        AlertDialog.Builder builder = new AlertDialog.Builder(SetActionJsonActivity.this);
        View view = View.inflate(SetActionJsonActivity.this,R.layout.createjson_layout,null);
        builder.setView(view);
        dialog = builder.create();
//        dialog.setCancelable(false);
        final EditText create_name = (EditText) view.findViewById(R.id.create_name);
        Button create_button = (Button) view.findViewById(R.id.create_button);
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String createName = create_name.getText().toString().trim();
                if(actionList == null){
                    Toast.makeText(SetActionJsonActivity.this,"请先编辑后再生成JSON文件",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(createName)){
                    create_name.requestFocus();
                    return;
                }

                createJSONFILE(createName);
                dialog.dismiss();
                dialog.cancel();
                dialog = null;
            }
        });
        dialog.show();
    }

    private void createJSONFILE(String jsonName){
        try{
            FileOutputStream fos = new  FileOutputStream(jsonFile + jsonName + ".json");
            JsonWriter jsonWriter= new JsonWriter(new OutputStreamWriter(fos,"utf-8"));
            jsonWriter.setIndent(" ");
            jsonWriter.beginArray();
            for(Action action:actionList){
                jsonWriter.beginObject();
                jsonWriter.name("role").value(action.role);
                jsonWriter.name("id").value(action.id);
                jsonWriter.name("name").value(action.name);
                jsonWriter.name("time").value(action.time);
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void deletAll() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SetActionJsonActivity.this);
        dialog.setTitle(R.string.notice);// 窗口名
        dialog.setMessage(R.string.sure_delete);
        dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                   actionList.clear();
                    monitorlistAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    private void updata() {//修改單條
        String role = action_role.getText().toString().trim();
        int style  = action_style.getSelectedItemPosition();
        String startTime = action_time.getText().toString().trim();
        String filename = totalList.get(action_name.getSelectedItemPosition());
        updata(POSTION,role,style,startTime,filename);
    }

    private void updata(final int postion, final String role, final int style, final String startTime, final String filename) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SetActionJsonActivity.this);
        dialog.setTitle(R.string.notice);// 窗口名
        dialog.setMessage(R.string.grade_data);
        dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    actionList.get(postion).setRole(Integer.valueOf(role));
                    actionList.get(postion).setId(style);
                    actionList.get(postion).setName(filename);
                    actionList.get(postion).setTime(Long.valueOf(startTime));
                    monitorlistAdapter.notifyDataSetChanged();
                    resetForm();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    private void add() {
        String role = action_role.getText().toString().trim();
        int style  = action_style.getSelectedItemPosition();
        String startTime = action_time.getText().toString().trim();
        String filename = totalList.get(action_name.getSelectedItemPosition());
        Action action = new Action(Integer.valueOf(role),style,filename,Long.valueOf(startTime));
        actionList.add(action);
        monitorlistAdapter.notifyDataSetChanged();
    }


    private class MonitorListAdapter extends BaseAdapter{
        public MonitorListAdapter(){}
        @Override
        public int getCount() {
            return actionList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            ViewHolder holder = null;
            if(view == null){
                holder = new ViewHolder();
                view  = getLayoutInflater().inflate(R.layout.monitorlistview,null);
                holder.roleID = (TextView) view.findViewById(R.id.gid);
                holder.styleID = (TextView) view.findViewById(R.id.style);
                holder.timeID = (TextView) view.findViewById(R.id.time);
                holder.nameID = (TextView) view.findViewById(R.id.binfile);
                holder.delet = (Button) view.findViewById(R.id.delet);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            holder.roleID.setText(""+actionList.get(position).getRole());
            holder.styleID.setText(""+actionStyle.get(actionList.get(position).getId()));
            holder.timeID.setText(""+actionList.get(position).getTime());
            holder.nameID.setText(""+actionList.get(position).getName());
            holder.delet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SetActionJsonActivity.this);
                    dialog.setTitle(R.string.notice);// 窗口名
                    dialog.setMessage(R.string.delete_data);
                    dialog.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                actionList.remove(position);
                                notifyDataSetChanged();
                            } catch (Exception e) {
                                 e.printStackTrace();
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
    public static class ViewHolder{
        private TextView roleID;
        private TextView styleID;
        private TextView timeID;
        private TextView nameID;
        private Button delet;
    }
}
