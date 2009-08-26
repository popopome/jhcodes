package com.jhlee.budgetsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/*
 * The class is for drawing pin.
 */
public class RRMonthBudgetPinView extends View {

	private static final int	LINE_WIDTH = 10;
	private static final int	LINE_COLOR = Color.WHITE;
	private static final int	BUDGET_PIN_HEIGHT = 30;
	private static final int	PIN_MARK_HEIGHT = 20;
	private static final int	PIN_MARK_WIDTH = 20;
	
	private Paint mPaint;
	private Path mPath = new Path();
	
	public RRMonthBudgetPinView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		initialize();
	}

	private void initialize() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(LINE_WIDTH);
		mPaint.setColor(LINE_COLOR);
	}

	public RRMonthBudgetPinView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRMonthBudgetPinView(Context context) {
		this(context, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(mPath, mPaint);
	}

	/*
	 * Measure view width and height
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), BUDGET_PIN_HEIGHT);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int cx = w/2;
		int medium = h - PIN_MARK_HEIGHT;
		
		mPath.reset();
		mPath.moveTo(0, 0);
		mPath.lineTo(0, medium);
		mPath.lineTo(cx - PIN_MARK_WIDTH/2, medium);
		mPath.lineTo(cx, h);
		mPath.lineTo(cx + PIN_MARK_WIDTH/2, medium);
		mPath.lineTo(w, medium);
		mPath.lineTo(w, 0);
	}

	
}