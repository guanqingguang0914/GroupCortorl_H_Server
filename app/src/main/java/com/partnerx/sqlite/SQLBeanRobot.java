package com.partnerx.sqlite;

public class SQLBeanRobot {
    public static final String DID = "_id";
    public static final String GID = "gid";
    public static final String TIME = "time";
    public static final String BINFILE = "binfile";

    private String did;
    private String gid;
    private String time;
    private String binfile;

    public SQLBeanRobot() {

    }

    public SQLBeanRobot(int gid, String time, String binfile) {
        this.gid = gid + "";
        this.time = time + "";
        this.binfile = binfile;
    }

    public String getGid() {
        return gid;
    }

    public String getTime() {
        return time;
    }

    public String getBinfile() {
        return binfile;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setBinfile(String binfile) {
        this.binfile = binfile;
    }
}
