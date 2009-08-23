package com.jhlee.chartsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RRChartBarView extends View {
	private static final int PADDING_HORZ = 10;
	private static final int PADDING_VERT = 10;

	private Paint mPaint;
	private String mTitle;
	private long mMaxValue;
	private long mValue;

	private Rect mBarRect = new Rect();

	public RRChartBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RRChartBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRChartBarView(Context context) {
		this(context, null);
	}

	private void initialize() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}

	public void setData(String title, long maxValue, long value) {
		mTitle = title;
		mMaxValue = maxValue;
		mValue = value;

		this.requestLayout();
	}

	/*
	 * Draw
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.WHITE);
		canvas.drawRect(mBarRect, mPaint);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.RED);
		canvas.drawRect(mBarRect, mPaint);
		
		mPaint.setColor(Color.BLUE);
		int vh = getHeight();
		int vw = getWidth();
		canvas.drawLine(0, vh, vw, vh, mPaint); 
	}

	/*
	 * Compute how rectangle is drawing
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (mMaxValue == 0) {
			super.onSizeChanged(w, h, oldw, oldh);
			return;
		}

		int stickMaxHeight = h - PADDING_VERT;
		int stickHeight = (int) (stickMaxHeight * mValue / mMaxValue);
		mBarRect.set(PADDING_HORZ, h - stickHeight, w - PADDING_HORZ, h);

	}
}
