package com.abilix.dialogdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

public class ShimmerText extends TextView{
	
	private LinearGradient mLinearGradient;
	private Matrix mMatrix;
	private Paint mPaint;
	private int mViewWidth = 0;
	private float mScale = 0.1f;
	
	private boolean mAnimating = true;

	public ShimmerText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		if(mViewWidth == 0){
			mViewWidth = getMeasuredWidth();
			if(mViewWidth > 0){
				mPaint = getPaint();
				mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, 
						new int[]{0x33ffffff, 0xffffffff, 0x33ffffff},
						new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP);
				mPaint.setShader(mLinearGradient);
				mMatrix = new Matrix();
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(mAnimating && mMatrix != null){
			mScale += 0.1f;
			if(mScale > 1.2f){
				mScale = 0.1f;
			}
			mMatrix.setScale(mScale, mScale, mViewWidth/2, 0);
			mLinearGradient.setLocalMatrix(mMatrix);
			postInvalidateDelayed(100);
		}
	}
	
}
