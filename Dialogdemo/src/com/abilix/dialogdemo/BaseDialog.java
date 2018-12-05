package com.abilix.dialogdemo;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;


/**
 * 加载对话框
 * 现在有两种形式，默认是loading框，还有一个是确认对话框
 * 需要设置type
 * Created by lz on 2016/6/12.
 */
public class BaseDialog extends Dialog {

    private Context mContext;

    public BaseDialog(Context context) {
        this(context, R.style.update_lib_BaseDialog);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context mContext) {
        this.mContext = mContext;
    } 

    /**
     * 设置dialog布局
     */
    public View setLayoutRes(int layoutResID) {
        View view = View.inflate(mContext, layoutResID, null);
        setContentView(view);
        return view;
    }

    /**
     * 设置动画效果
     *
     * @param resId
     * @return
     */
    public void setAnimations(int resId) {
        Window window = getWindow();
        //设置显示动画
        window.setWindowAnimations(resId);
    }

    /**
     * 设置点击的时候是否可以取消
     *
     * @param isCancel
     */
    public void setIsCancel(boolean isCancel) {
        setCancelable(isCancel);
        setCanceledOnTouchOutside(isCancel);
    }



    /**
     * activity 销毁前调用
     */
    public void destroy() {
        if (isShowing()) {
            dismiss();
        }
        cancel();
    }

}
