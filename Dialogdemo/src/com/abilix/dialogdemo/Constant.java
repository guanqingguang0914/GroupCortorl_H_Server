package com.abilix.dialogdemo;

public interface Constant {

	// 外网api地址
	public static String URL_OUT1 = "http://admin.abilixstore.com:81";
//	public static String URL_OUT1 = "http://10.100.2.32";
	// 外网下载apk地址
	public static String URL_OUT_FILE = "http://file.abilixstore.com:81";
//	public static String URL_OUT_FILE = "http://10.100.2.32:8088";
	
	public static String APK = "/api/appstore/apps/updateandnews";
	public static String STORE = "/api/appstore/updateandnews";
	
	/**app store packagename*/
	public static String AS_PN_PAD = "com.app.appstoreclient";
	public static String AS_PN_PHONE = "com.abilix.appsphone";
	
	public static String DOWNLOAD_STORE = "/appstore/apk";
	public static String DOWNLOAD_APK = "/appstore/apk";
	
	/**获取更新动态地址*/
//	public static String GET_CONTENT = "/APPSTORE/appinfo/100";
	public static String UPDATENEWS_APPS = "/api/appstore/get/updatenews?package_name=";
	public static String UPDATENEWS_OTHERS = "/api/appstore/apps/get/updatenews?package_name=";
	
	public static String STM = "/api/stm";
	public static String STM_DOWN = "/stmfile/";
	
}
