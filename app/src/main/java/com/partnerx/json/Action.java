package com.partnerx.json;

/**
 * Created by guanqg on 2018/1/26.
 */

public class Action {
    public int role;//角色
    public int id;//播放类型：
    // 0：bin;         ；名称
    // 1:bin(无mp3)    ；名称
    // 2.mp3           ；名称
    // 3.步态          ；前进后退左转右转停止
    public  String name;//播放名称
    public long time;//开始时间

    public Action() {
    }

    public Action(int role, int id, String name, Long time){
        this.role = role;
        this.id = id;
        this.name = name;
        this.time = time;
    }
    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
