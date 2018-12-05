package com.abilix.dialogdemo;

/**
 * Created by quhw on 2017/6/27.
 */

public class UpdateInfo {

    public int code ;//是否有更新 1.是 0.否
    public int force_update ;//是否强制更新 1.是 0.否
    public String apk_url ;//完整的apk下载地址
    public String update_detail ;//更新信息

    public String getUpdate_detail() {
        return update_detail;
    }

    public void setUpdate_detail(String update_detail) {
        this.update_detail = update_detail;
    }

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getForce_update() {
        return force_update;
    }

    public void setForce_update(int force_update) {
        this.force_update = force_update;
    }
}
