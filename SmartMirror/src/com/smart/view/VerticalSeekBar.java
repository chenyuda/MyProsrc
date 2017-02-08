/*******************************************************************************
 * Copyright (c) 2016 by Ohio Corporation all right reserved.
 * 2016-8-3 
 * 
 *******************************************************************************/ 
package com.smart.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * <pre>
 * 功能说明: 
 * 日期:	2016-8-3
 * 开发者:	cyd
 * 
 * 历史记录
 *    修改内容：
 *    修改人员：
 *    修改日期： 2016-8-3
 * </pre>
 */
public class VerticalSeekBar extends SeekBar {
	public VerticalSeekBar(Context context) {
        super(context);
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }
 
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
 
    protected void onDraw(Canvas c) {
        //将SeekBar转转90度
        c.rotate(-90);
        //将旋转后的视图移动回来
        c.translate(-getHeight(),0);
        Log.i("getHeight()",getHeight()+"");
        super.onDraw(c);
    }
 @SuppressLint("ClickableViewAccessibility")
@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
 
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	Log.e("TGA", "DOWN");
            	break;
            case MotionEvent.ACTION_MOVE:
            	
            case MotionEvent.ACTION_UP:
            	Log.e("TGA", "UP");
            	int i=0;
                //获取滑动的距离
                i=getMax() - (int) (getMax() * event.getY() / getHeight());
                
                //设置进度
                setProgress(i);
                //setThumbOffset(i);
                Log.i("Progress",getProgress()+"");
                //每次拖动SeekBar都会调用
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                Log.i("getWidth()",getWidth()+"");
                Log.i("getHeight()",getHeight()+"");
                break;
 
            case MotionEvent.ACTION_CANCEL:
            	
                break;
        }
        return true;
    }

	/**
	 * 功能说明：
	 * 日期:	2016-8-8
	 * 开发者: cyd
	 *
	 */
	public void setMoveProgress(int progress) {
		// TODO Auto-generated method stub
		setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
	@Override
	public void setThumbOffset(int thumbOffset) {
		// TODO Auto-generated method stub
		super.setThumbOffset(thumbOffset);
	}


	@Override
	public int getThumbOffset() {
		// TODO Auto-generated method stub
		return super.getThumbOffset();
	}

}
