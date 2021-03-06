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
	private static final int PADDING_BASELINE = 30;
	private static final int DEFAULT_TEXT_SIZE = 17;
	private static final float DEFAULT_LINE_STROKE_WIDTH = 1.5f;
	
	private int mBarFillColor = Color.LTGRAY;
	private int mBarEdgeColor = Color.DKGRAY;
	private int mBarTitleColor = Color.WHITE;

	private Paint mPaint;
	private String mTitle;
	private long mTitleTextSize;
	private long mMaxValue;
	private long mValue;
	private String mValueName;
	private long mValueNameTextSize;
	
	private int mBarWidth = 1; 

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
		mPaint.setTextSize((float) DEFAULT_TEXT_SIZE);
		mPaint.setSubpixelText(true);
	}

	public void setData(String title, String valueName, long maxValue, long value) {
		mTitle = title;
		mMaxValue = maxValue;
		mValue = value;
		mValueName = valueName;
	}

	/*
	 * Draw
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		/* Fill bar */
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mBarFillColor);
		canvas.drawRect(mBarRect, mPaint);
		/* Draw edge */
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(DEFAULT_LINE_STROKE_WIDTH);
		mPaint.setColor(mBarEdgeColor);
		canvas.drawRect(mBarRect, mPaint);
		
		/* Draw base line */
		int vh = getHeight();
		int vw = getWidth();
		canvas.drawLine(0, mBarRect.bottom, vw, mBarRect.bottom, mPaint);
		
		/* Draw text at above bar */
		mPaint.setTextSize(mTitleTextSize);
		mPaint.setColor(mBarTitleColor);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setShadowLayer(2.0f, 0, 0, Color.BLACK);
		mPaint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(mTitle, vw/2, mBarRect.top - 5, mPaint);
		
		/* Draw value name */
		mPaint.setTextSize(mValueNameTextSize);
		canvas.drawText(mValueName, vw/2, mBarRect.bottom + 5 + mValueNameTextSize, mPaint);
		
		mPaint.setShadowLayer(0.0f, 0, 0, Color.BLACK);
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

		int stickMaxHeight = h - PADDING_BASELINE - PADDING_VERT;
		int stickHeight = (int) (stickMaxHeight * mValue / mMaxValue);
		mBarRect.set(PADDING_HORZ, h - PADDING_BASELINE - stickHeight, w - PADDING_HORZ, h - PADDING_BASELINE);
	}
	
	/*
	 * Set bar width
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.setMeasuredDimension(mBarWidth, getMeasuredHeight());
	}

	/*
	 * Update default bar width
	 */
	public void setBarWidth(int barWidth) {
		mBarWidth = barWidth;
	}
	public void setBarValueNameTextSize(int textSize) {
		mValueNameTextSize = textSize;
	}
	public void setTitleTextSize(int textSize) {
		mTitleTextSize = textSize;
	}
	public void setBarColor(int barColor, int barEdgeColor) {
		mBarFillColor = barColor;
		mBarEdgeColor = barEdgeColor;
	}
}
