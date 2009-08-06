package com.jhlee.textblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Text view with blurred effect.
 * 
 * @author popopome
 * 
 */
public class TextBlurView extends View {

	private Paint mPaint;
	private Bitmap mTextBitmap;
	private boolean mMouseDownFlag = false;
	private boolean mMouseInside = false;
	private boolean mMouseMovedOutside = true;

	/** CTOR */
	public TextBlurView(Context ctx) {
		super(ctx);
	}

	public TextBlurView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initializeInternal(attrs);
	}

	public TextBlurView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initializeInternal(attrs);
	}

	/**
	 * Initialize text
	 * 
	 * @param attrs
	 */
	private void initializeInternal(AttributeSet attrs) {
		/* Initialize paint object */
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		float fontSizeInPixel = dm.scaledDensity * 23.5f;
		/* Let's use custom font */
		/* Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); */
		Typeface font = Typeface.createFromAsset(this.getContext().getAssets(),
				"fonts/Complete in Him.ttf");

		mPaint.setTypeface(font);
		mPaint.setTextSize(fontSizeInPixel);

		String text = attrs.getAttributeValue(null, "text");
		mTextBitmap = createTextBlurBitmap(text, mPaint, Color.WHITE,
				Color.BLACK);
	}

	/**
	 * Create text blur bitmap
	 * 
	 * @param text
	 */
	private Bitmap createTextBlurBitmap(String text, Paint paint,
			int textColor, int bgColor) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		Rect marginBounds = new Rect();
		paint.getTextBounds("A", 0, 1, marginBounds);

		/* Create text bitmap */
		int bmpW = bounds.width() + marginBounds.width() * 2;
		int bmpH = bounds.height() * 2;
		Bitmap bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
		bmp.eraseColor(Color.TRANSPARENT);

		int textDrawingX = bmpW / 2 - bounds.width() / 2;
		int textDrawingY = bmpH / 2 + bounds.height() / 2;

		Canvas canvas = new Canvas(bmp);

		/* Set up blur filter */
		MaskFilter blur = new BlurMaskFilter(8.0f, BlurMaskFilter.Blur.NORMAL);
		paint.setMaskFilter(blur);

		/* Make background text */
		paint.setColor(bgColor);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);

		/* Make foreground text */
		paint.setMaskFilter(null);
		paint.setColor(textColor);
		canvas.drawText(text, textDrawingX, textDrawingY, mPaint);

		return bmp;
	}

	/**
	 * Draw
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int x = 0;
		int y = 0;
		if (mMouseDownFlag == true && mMouseMovedOutside == false) {
			x = 2;
			y = 2;
		}
		canvas.drawBitmap(mTextBitmap, x, y, mPaint);
	}

	/**
	 * Measure view size
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.setMeasuredDimension(this.getMeasuredWidth(), mTextBitmap
				.getHeight());
	}

	/**
	 * Touch event
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int x = (int) e.getX();
		int y = (int) e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mMouseDownFlag = true;
			mMouseInside = true;
			mMouseMovedOutside = false;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			boolean oldMousePos = mMouseMovedOutside;
			if (x < 0 || x > this.getWidth() || y < 0 || y > this.getHeight()) {
				mMouseInside = false;
				mMouseMovedOutside = true;
			} else {
				mMouseMovedOutside = false;
			}
			if(oldMousePos != mMouseMovedOutside) {
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (x < 0 || x > this.getWidth() || y < 0 || y > this.getHeight()) {
				mMouseInside = false;
			}
			if (mMouseDownFlag == true && mMouseInside == true) {
				Toast.makeText(this.getContext(), "clicked", Toast.LENGTH_LONG)
						.show();
			}
			mMouseDownFlag = false;
			mMouseInside = false;
			mMouseMovedOutside = true;
			invalidate();
			break;
		}
		return true;
	}
}
