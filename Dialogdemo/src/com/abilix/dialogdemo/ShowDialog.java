package com.abilix.dialogdemo;

import java.util.Map;
import java.util.logging.LogManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Text;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 版本更新类
 * @author hanxl
 *
 */
public class ShowDialog implements Constant{
	private static ShowDialog showdialog = new ShowDialog();
	private Context mContext;
	private String appfilename; // 应用文件名称
	private String appName;//应用名称
	private static final int CONTENT = 1;
	private static final int UPDATE = 3;
	private static final int CHECKUPDATE = 4;
	private static final int CHECKUPDATE_ERROR = 5;
	public String mSavePath = null; 
//	public String versionName = null ;
	public int versionCode = 0 ;
	private String packageName ;
	public int serviceVersion = 0 ;
	public long FileCrc;
//	public String contents = null;//更新动态
	public DownFileThread downFileThread;
	public UpdateBrainDCallback callback;

	public ProgressDialog pd ;

	public static ShowDialog getDialog() {
		return showdialog;
	}

	/**
	 * 应用版本内更新
	 * @param contexts
	 */
	public void loadVersion(Context contexts){
		this.mContext = contexts;
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		try {
			packageName = mContext.getPackageName();
			PackageManager packageManager = mContext.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
//			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
			appName = mContext.getResources().getString(packageInfo.applicationInfo.labelRes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = "";
				url = URL_OUT1;
				String l = mContext.getResources().getConfiguration().locale.getLanguage();
				String s = mContext.getResources().getConfiguration().locale.getDefault().getCountry();
				l = l + "-" + s;
				
				StringBuffer surl = new StringBuffer(url);
				if(packageName != null){
					if(packageName.equals(AS_PN_PAD) || packageName.equals(AS_PN_PHONE)){
						surl = surl.append(STORE);
					}else{
						surl = surl.append(APK);
					}
					String str = "?package_name="+packageName+"&version_code="+versionCode;
					surl = surl.append(str);
					String update_url = surl.toString();
					Log.e("quhw", "检测更新的地址为："+update_url);
					
					HttpPost httpRequest = new HttpPost(update_url);
					httpRequest.addHeader("Accept-Language", l);
					try {
						HttpClient httpClient = new DefaultHttpClient();

						HttpResponse httpResponse = httpClient.execute(httpRequest);

						if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
							String jsonResult = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
							JSONObject paramjson = new JSONObject(jsonResult);
							boolean code = paramjson.optBoolean("code");//code:false 请求成功
							if(!code){//false正确，true出错
								JSONObject obj = paramjson.getJSONObject("data");
								int isUpdate = obj.getInt("code");//1:有更新；2：无更新
								if(isUpdate == 1){
									int force_update = obj.getInt("force_update");
									String apk_url = obj.getString("apk_url");
									String update_detail = obj.getString("update_detail");
									Log.e("quhw", "版本更新的内容："+update_detail);
									Log.e("quhw", "apk下载地址："+apk_url);
									appfilename = apk_url.substring(apk_url.lastIndexOf("/"));
									Log.e("quhw", "appfilename："+appfilename);
									UpdateInfo updateInfo = new UpdateInfo();
									updateInfo.setForce_update(force_update);
									updateInfo.setCode(isUpdate);
									updateInfo.setApk_url(apk_url);
									updateInfo.setUpdate_detail(update_detail);
									Message message = new Message();
									message.what = UPDATE;
									message.obj = updateInfo;
									mHandler.sendMessage(message);
								}

							}else{
								Log.d("quhw", "调用接口出错，code="+code);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d("quhw", "报异常了");
					}
				}else{
					Log.d("quhw", "未检测到包名");
				}
			}
		}).start();
		
		
	}
	/**
	 * 显示更新提示框
	 */
	public void showNoticeDialog(final UpdateInfo updateInfo){
		
		final BaseDialog dialog  = new BaseDialog(mContext);
		View view = View.inflate(mContext, R.layout.update_dialog, null);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.setContentView(view);
		TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
		final TextView tv_update = (TextView)view.findViewById(R.id.tv_update_show);
		final ImageView tv_cancel = (ImageView)view.findViewById(R.id.tv_cancel_show);
		final FrameLayout rl_update_version = (FrameLayout) view.findViewById(R.id.rl_update_version);

		RelativeLayout ll_update_show = (RelativeLayout) view.findViewById(R.id.ll_update_show);
		TextView tv_exit = (TextView) view.findViewById(R.id.tv_exit);
		TextView tv_force_update = (TextView) view.findViewById(R.id.tv_force_update);

		final int force_update = updateInfo.getForce_update();
		if(force_update == 1){//强制
			tv_update.setVisibility(View.GONE);
			tv_cancel.setVisibility(View.GONE);
			ll_update_show.setVisibility(View.VISIBLE);
		}else{
			tv_update.setVisibility(View.VISIBLE);
			tv_cancel.setVisibility(View.VISIBLE);
			ll_update_show.setVisibility(View.GONE);
		}
		String contents = updateInfo.getUpdate_detail();

		dialog.show();

		tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());

		if(contents != null){
			tv_content.setText(contents);
		}

		tv_update.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				rl_update_version.setVisibility(View.GONE);
				showDialogDialog(force_update);
		        downFileThread = new DownFileThread(mContext, appName, appfilename, updateInfo, true, pd);
		        downFileThread.start();
			}
		});
		
		tv_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				
			}
		});

		tv_exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.exit(0);//杀死进程
			}
		});

		tv_force_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				rl_update_version.setVisibility(View.GONE);
				showDialogDialog(force_update);
				downFileThread = new DownFileThread(mContext, appName, appfilename, updateInfo, true, pd);
				downFileThread.start();
			}
		});

	}
	
	/**
	 * 显示固件升级提示框
	 */
//	public void showCheckDialog(final String path){
//		LayoutInflater inflater = (LayoutInflater) mContext
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View viewdialog = inflater.inflate(R.layout.update_dialog_double, null);
//		final BaseDialog dialog = new BaseDialog(mContext);
//		dialog.setContentView(viewdialog);
//		dialog.show();
//		dialog.setCanceledOnTouchOutside(false);
//		WindowManager.LayoutParams params = dialog
//				.getWindow().getAttributes();
//		params.width = mContext
//				.getResources()
//				.getDimensionPixelSize(R.dimen.y1110);
//		params.height = mContext
//				.getResources()
//				.getDimensionPixelSize(R.dimen.y810);
//		dialog.getWindow().setAttributes(params);
//		dialog.getWindow().setBackgroundDrawable(
//				new BitmapDrawable());
//		TextView tv_message = (TextView) viewdialog
//				.findViewById(R.id.tv_update_message);
//		TextView tv_cancel = (TextView) viewdialog
//				.findViewById(R.id.tv_update_cancel);
//		TextView tv_submit = (TextView) viewdialog
//				.findViewById(R.id.tv_update_submit);
//		tv_message.setText(mContext.getResources().getString(R.string.update_text));
//		// 确定
//		tv_submit.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// 发送回调出去
//				dialog.dismiss();
//				callback.onSuccessCallback(path);
//			}
//		});
//
//		tv_cancel.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// 取消下载
//				dialog.dismiss();
//			}
//		});
//	}
	
	/**
	 * 消息回调处理
	 */
    private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE:
				Log.e("quhw", "版本更新");
				UpdateInfo updateInfo = (UpdateInfo) msg.obj;
				showNoticeDialog(updateInfo);
//				if(force_update == 1){
//					showDialogDialog();
//					downFileThread = new DownFileThread(mContext, appName, appfilename, apkUrl, true,0);
//					downFileThread.start();
//				}else{
//					showNoticeDialog(updateInfo);
//				}

				break;
//			case CHECKUPDATE:
//				// 固件版本升级
//				String path = msg.obj.toString();
//				showCheckDialog(path);
//				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 显示“正在下载”进度框
	 */
	public void showDialogDialog(int force_update){
		pd.setMessage(mContext.getResources().getString(R.string.update_lib_down_ing));
    	pd.setCanceledOnTouchOutside(false);
    	pd.show();
    	//延时3秒消失
		if(force_update != 1){

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					pd.dismiss();
				}
			}, 3000);
		}

	}
	
	public void cancelDownNotification(){
		DownFileThread.cancelNotification();
	}
	
	/**
	 * 发下载完成的回调，调用方只需要implements Callbacks 接口，在solve的方法里处理返回值
	 */
//	public void checkupdateshow(String path){
//		Log.e("checkupdate", "版本更新:" + path);
////		showCheckDialog(path);
//		// 更新
//		Message msg = new Message();
//		msg.what = CHECKUPDATE;
//		msg.obj = path;
//		mHandler.sendMessage(msg);
//	}
	
	/**
	 * 下载过程异常
	 */
	public void checkfailshow(String error){
		Log.e("error", "下载异常原因:" + error);
		if(callback != null){
			callback.onFailCallback(error);
		}

	}
}
