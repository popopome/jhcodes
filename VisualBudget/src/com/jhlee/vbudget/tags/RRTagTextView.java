package com.jhlee.vbudget.tags;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jhlee.vbudget.R;

public class RRTagTextView extends View {
	private static Bitmap mDeleteBmp = null;

	private static final int PADDING_HORZ = 10;
	private static final int PADDING_VERT = 10;
	private static final int PADDING_TEXT_HORZ = 10;
	private static final int PADDING_TEXT_VERT = 10;
	private static final int COLOR_CHECKED = 0xff73d216;
	private static final int COLOR_NORMAL = 0xffbbbbbb;
	private static final int TAG_TEXT_SIZE = 20;

	private String mText = "";
	private Paint mPaint;
	private Rect mBounds = new Rect();
	private RectF mDrawingTmpRect = new RectF();
	private boolean mChecked = false;
	private boolean mTrackMouse = false;
	private boolean mShowDeleteMark = false;

	private int mMinX;
	private int mMinY;
	private int mMaxX;
	private int mMaxY;
	
	private OnTagClickListener	mTagClickListener;
	

	public RRTagTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public RRTagTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public RRTagTextView(Context context) {
		super(context);
		initialize();
	}

	private void initialize() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(TAG_TEXT_SIZE);

		if (mDeleteBmp == null) {
			mDeleteBmp = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.delete_icon);
		}
	}

	public void setOnTagClickListener(OnTagClickListener tagClickListener) {
		mTagClickListener = tagClickListener;
	}
	public void setTagText(String text) {
		mText = text;
		this.requestLayout();
	}

	public String getTagText() {
		return mText;
	}

	/**
	 * Draw text
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		Paint p = mPaint;
		
		if (mChecked)
			p.setColor(COLOR_CHECKED);
		else
			p.setColor(COLOR_NORMAL);

		p.setShadowLayer(2.0f, 0, 0, Color.BLACK);

		int vw = getWidth();
		int vh = getHeight();
		mDrawingTmpRect.set(PADDING_HORZ, PADDING_VERT, vw - PADDING_HORZ, vh
				- PADDING_VERT);
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setStrokeWidth(3.0f);
		canvas.drawRoundRect(mDrawingTmpRect, 10.0f, 10.0f, p);

		p.setStyle(Paint.Style.FILL);

		p.setColor(Color.WHITE);
		canvas.drawText(mText, vw / 2 - mBounds.width() / 2, vh / 2
				+ mBounds.height() / 3, p);
		p.setShadowLayer(0.0f, 0, 0, Color.BLACK);

		/*
		 * Draw delete mark.
		 */
		if (mShowDeleteMark) {
			canvas.drawBitmap(mDeleteBmp, vw - mDeleteBmp.getWidth() - 2, vh
					/ 2 - mDeleteBmp.getHeight() / 2, p);
		}
	}

	/**
	 * Measure text size
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mPaint.getTextBounds(mText, 0, mText.length(), mBounds);
		/* Use padding value */
		this.setMeasuredDimension(mBounds.width() + PADDING_HORZ * 2
				+ PADDING_TEXT_HORZ * 2, mBounds.height() + PADDING_VERT * 2
				+ PADDING_TEXT_VERT * 2);
	}

	public void toggleCheck() {
		mChecked = !mChecked;
	}

	public void check() {
		mChecked = true;
	}

	public void uncheck() {
		mChecked = false;
	}

	public void showDeleteMark() {
		mShowDeleteMark = true;
	}

	public void hideDeleteMark() {
		mShowDeleteMark = false;
	}

	public boolean isChecked() {
		return mChecked;
	}

	/*
	 * Check view has same tag
	 */
	public boolean hasSameTag(String tag) {
		return 0 == tag.compareTo(mText);
	}

	/*
	 * Do nothing
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mMinX = x;
			mMinY = y;
			mMaxX = x;
			mMaxY = y;
			mTrackMouse = true;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTrackMouse == true) {
				mMinX = Math.min(mMinX, x);
				mMinY = Math.min(mMinY, y);
				mMaxX = Math.max(mMaxX, x);
				mMaxY = Math.max(mMaxY, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTrackMouse == true) {
				int movementX = mMaxX - mMinX;
				int movementY = mMaxY - mMinY;
				if (movementX < 30 && movementY < 30) {
					toggleCheck();
					if(mTagClickListener != null)
						mTagClickListener.onTagClicked(this, mText, isChecked());
				}
				invalidate();
			}

			mTrackMouse = false;
			break;
		default:
			return false;
		}
		/* Consumed */
		return true;
	}
	
	
	public interface OnTagClickListener {
		public void onTagClicked(View view, String tagName, boolean checked);
	}

}
