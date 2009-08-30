package com.jhlee.vbudget.overview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class RRPastelBoxView extends TextView {

	private Paint	mPaint;
	private Rect	mRect = new Rect();
	
	public RRPastelBoxView(Context context) {
		this(context, null);
	}

	public RRPastelBoxView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRPastelBoxView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setPadding(2, 2, 2, 2);
		this.setTextColor(Color.rgb(0x82, 0x82, 0x82));
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(0.8f);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mRect.set(2, 2, w-2, h-2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(mRect, mPaint);
	}

}
