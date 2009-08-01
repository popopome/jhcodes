package com.jhlee.calendarsample;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import junit.framework.Assert;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CalendarStreamView extends View {

	public static final String TAG = "CalendarStreamView";
	public static final long ONEDAY_IN_MILLISECONDS = 60 * 60 * 24 * 1000;
	public static final long ONEWEEK_IN_MILLISECONDS = ONEDAY_IN_MILLISECONDS * 7;
	
	private static final int COLOR_SUNDAY = Color.rgb(255, 0, 0);
	private static final int COLOR_SATURDAY = Color.rgb(0, 0, 255);
	private static final int COLOR_WEEKDAY = Color.BLACK;
	private static final int COLOR_EVEN_MONTH = Color.rgb(192, 192, 192);
	private static final int COLOR_ODD_MONTH = Color.rgb(172,172,172);
	private static final int COLOR_FOCUS_BG = Color.BLACK;
	private static final int COLOR_FOCUS_FG = Color.WHITE;
	
	private static final int PADDING_DAY_FROM_RIGHT = 3;
	private static final int PADDING_DAY_FROM_TOP = 3;
	
	/* Base date */
	private Calendar mBaseDate = new GregorianCalendar(new SimpleTimeZone(0,
			"GMT"));
	/* Base date in milliseconds */
	private long mBaseDateInMillis = 0;
	private Calendar mDate = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
	private int mWeekHeight = 30;
	private int mDayWidth = 0;
	private int mCurOffset = 0;
	private final String mNumString[] = new String[] { "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
			"29", "30", "31" };
	private Paint mPaint;
	private int mTextHeight = 0;
	private int mDayTextSize = 13;
	private int mYearMonthTextSize = 60;

	private boolean mMouseDownFlag = false;
	private Point mLastPoint = new Point();
	private Point mDownPoint = new Point();
	private Rect	mTmpRectForDrawing = new Rect();
	
	private long	mFocusDateInMillis;
	private long	mHitDateInMillis;
	private Calendar mHitTestDate = Calendar.getInstance();
	private boolean	mTouchMovedOverDate = false;
	
	private NinePatch mFocusDateBg;

	/** CTOR */
	public CalendarStreamView(Context ctx) {
		super(ctx);
		initialize();
	}

	public CalendarStreamView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		initialize();
	}

	public CalendarStreamView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		initialize();
	}

	/** Initialize */
	private void initialize() {
		/*
		 * Initialize paint object
		 */
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(mDayTextSize);
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Paint.Align.RIGHT);

		/* Compute text height */
		Rect bounds = new Rect();
		mPaint.getTextBounds("01234567890", 0, 1, bounds);
		mTextHeight = bounds.height();

		/* Initialize background nine patch for focus date */
		Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.calendar_view_todaybg);
		mFocusDateBg = new NinePatch(bmp, bmp.getNinePatchChunk(), null);
		
		
		/*
		 * Base date is 1989-12-31 0, 0, 0 It was Sunday and offset 0 will be
		 * mapped onto this day. Java Calendar use 0-based index for month
		 */
		mBaseDate.set(1989, 11, 31, 0, 0, 0);
		mBaseDate.set(Calendar.MILLISECOND, 0);
		mBaseDateInMillis = mBaseDate.getTimeInMillis();

		moveToToday();
	}

	/**
	 * Compute DATE from Y-Offset
	 * 
	 * @param yOffset
	 * @return
	 */
	public Calendar dateFromYOffset(int yOffset) {
		int nthWeeks = yOffset / mWeekHeight;
		long millsSinceBaseDate = nthWeeks * ONEWEEK_IN_MILLISECONDS;
		mDate.clear();
		mDate.set(Calendar.MILLISECOND, 0);
		mDate.setTimeInMillis(mBaseDate.getTimeInMillis() + millsSinceBaseDate);

		return mDate;
	}

	/**
	 * Compute Y-Offset from DATE
	 * 
	 * @param date
	 * @return
	 */
	public int yOffsetFromDate(Calendar cal) {
		long mills = cal.getTimeInMillis();
		int nthWeeks = (int) ((mills - mBaseDateInMillis) / ONEWEEK_IN_MILLISECONDS);
		return nthWeeks * mWeekHeight;
	}

	/**
	 * Move offset to month which includes given date.
	 * 
	 * @param cal
	 */
	public void moveToDate(Calendar cal) {
		int offset = yOffsetFromDate(cal);

		/* Is visible? */
		if ((offset >= mCurOffset) && (offset < mCurOffset + this.getHeight())) {
			/*
			 * The date is already visible. Hence we do not need to change
			 * offset
			 */
			return;
		}

		mCurOffset = offset;
		this.invalidate();
	}

	/** Move to today */
	public void moveToToday() {
		Calendar today = Calendar.getInstance();
		moveToDate(today);
	}

	/**
	 * Draw calendar
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		Assert.assertTrue(mWeekHeight != 0);
		Assert.assertTrue(mDayWidth != 0);

		/* Align offset */
		int offset = mCurOffset / mWeekHeight * mWeekHeight;
		int endOffset = mCurOffset + this.getHeight();

		/* Initial computation before entering drawing loop. */
		Calendar cal = this.dateFromYOffset(offset);
		int nextWeekStartDay = cal.get(Calendar.DAY_OF_MONTH);
		int nextWeekMonth = cal.get(Calendar.MONTH);
		int nextWeekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
		int nextYear = cal.get(Calendar.YEAR);
		
		int curWeekMonth = 0;
		int curWeekStartDay = 0;
		int curWeekOfMonth = 0;
		int curYear = 0;

		
		
		/* Draw each week */
		int bgColor = 0;
		long screenY = offset - mCurOffset;
		for (; offset < endOffset; offset += mWeekHeight, screenY += mWeekHeight) {
			curWeekStartDay = nextWeekStartDay;
			curWeekMonth = nextWeekMonth;
			curWeekOfMonth = nextWeekOfMonth;
			curYear = nextYear;
			
			/* To find current focused date */
			long curDateMillis = cal.getTimeInMillis();

			/* Compute next week information */
			cal = this.dateFromYOffset(offset + mWeekHeight);
			nextWeekStartDay = cal.get(Calendar.DAY_OF_MONTH);
			nextWeekMonth = cal.get(Calendar.MONTH);
			nextWeekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			nextYear = cal.get(Calendar.YEAR);

			int curMonthColor = COLOR_ODD_MONTH;
			int nextMonthColor = COLOR_EVEN_MONTH;
			/* Decide background color */
			if ((curWeekMonth & 0x01) == 0x00) {
				curMonthColor = COLOR_EVEN_MONTH;
				nextMonthColor = COLOR_ODD_MONTH;
			}
			
			/* If all days of current week are in same month,
			 * ...
			 */
			if ((curWeekMonth == nextWeekMonth)
					|| ((curWeekMonth != nextWeekMonth) && (nextWeekStartDay == 1))) {
				
				/* Draw background */
				drawDayBackground(canvas, 0, (int)screenY, curMonthColor, 0, 7);
				
				/* Draw year/month information if current week is third one */
				if(3 == curWeekOfMonth) {
					mPaint.setTextSize(mYearMonthTextSize);
					mPaint.setColor(nextMonthColor);
					
					String ymStr = Integer.toString(curYear);
					ymStr += "." + mNumString[curWeekMonth];
					canvas.drawText(ymStr, this.getWidth(), screenY+mWeekHeight, mPaint);
					mPaint.setTextSize(mDayTextSize);
				}
				/* Draw day text */
				drawDayText(canvas, 0, (int)screenY, 0, 7, curWeekStartDay, false);
			} else {
				
				/*
				 * We know current week has days from different month. And
				 * nextWeekStartDay SHOULD be less than 7 because next week is
				 * first week of that month.
				 */
				int cnt = 7 - (nextWeekStartDay - 1);


				/* Draw background */
				int xPos = drawDayBackground(canvas, 0, (int)screenY, curMonthColor, 0, cnt);
				drawDayBackground(canvas, xPos, (int)screenY, nextMonthColor, cnt-1, 7);
				
				/* Draw day text */
				xPos = drawDayText(canvas, 0, (int)screenY, 0, cnt, curWeekStartDay, false);
				drawDayText(canvas, xPos, (int)screenY, cnt, 7, 1, false);
			}
		}
		
		/* Draw focus area */
		mDate.setTimeInMillis(mFocusDateInMillis);
		int focusDayOfWeek =mDate.get(Calendar.DAY_OF_WEEK)-1; 
		int y = yOffsetFromDate(mDate) - mCurOffset;
		int x = (focusDayOfWeek) * mDayWidth;
		mPaint.setColor(COLOR_FOCUS_BG);
		canvas.drawRect(x, y, x+mDayWidth, y+mWeekHeight, mPaint);
		mPaint.setColor(COLOR_FOCUS_FG);
		drawDayText(canvas, x, y, focusDayOfWeek, focusDayOfWeek+1, mDate.get(Calendar.DAY_OF_MONTH), true);
	}

	/*
	 * Draw day text
	 */
	private int drawDayText(Canvas canvas, 
			int x,
			int y,
			int startDayOfWeek,
			int endDayOfWeek,
			int startDay,
			boolean focused) {
		/* Draw day text */
		mPaint.setColor(COLOR_SUNDAY);
		int right = x + mDayWidth;
		for (int dayIndex = startDayOfWeek; dayIndex < endDayOfWeek; ++dayIndex, ++startDay) {
			if(focused) {
				mPaint.setColor(COLOR_FOCUS_FG);
			}
			else if(0 == dayIndex) {
				mPaint.setColor(COLOR_SUNDAY);
			} else if(6 == dayIndex) {
				mPaint.setColor(COLOR_SATURDAY);
				right = this.getWidth();
			} else {
				mPaint.setColor(COLOR_WEEKDAY);
			}
			
			canvas.drawText(mNumString[startDay - 1],
					right - PADDING_DAY_FROM_RIGHT, y
							+ mTextHeight + PADDING_DAY_FROM_TOP, mPaint);
			x = right;
			right += mDayWidth;
		}
		return x;
	}

	/**
	 * Draw day background
	 * @param canvas
	 * @param bgColor
	 * @param curDateMillis
	 */
	private int drawDayBackground(Canvas canvas,
			int x,
			int y,
			int bgColor,
			int startDayOfWeek,
			int endDayOfWeek) {
		mPaint.setColor(bgColor);
		int right = x+mDayWidth;
		for (int dayIndex = startDayOfWeek; dayIndex < endDayOfWeek; ++dayIndex) {
			if (dayIndex == 6) {
				right = this.getWidth();
			}
			canvas.drawRect(x, y, right, y+mWeekHeight, mPaint);
			x = right;
			right += mDayWidth;
		}
		
		return x;
	}

	private int drawDays(Canvas canvas, int curWeekStartDay, int bgColor,
			long screenY, long curDateMillis, int cnt) {
		int xPos;
		int dayIndex;
		/* Initialize rectangle for drawing */
		mTmpRectForDrawing.set(0, (int)screenY, mDayWidth, (int)screenY + mWeekHeight);
		xPos = 0;
		for (dayIndex = 0; dayIndex < cnt; ++dayIndex) {
			/* Draw background */
			mPaint.setColor(bgColor);
			canvas.drawRect(mTmpRectForDrawing, mPaint);
			
			/* Is focused date? */
			if(curDateMillis == mHitDateInMillis) {
				mFocusDateBg.draw(canvas, mTmpRectForDrawing);
			}
			
			/* Draw day text */
			if(0 == dayIndex)
				mPaint.setColor(COLOR_SUNDAY);
			else
				mPaint.setColor(COLOR_WEEKDAY);

			canvas.drawText(mNumString[curWeekStartDay + dayIndex - 1],
					xPos + mDayWidth - PADDING_DAY_FROM_RIGHT, screenY
							+ mTextHeight + PADDING_DAY_FROM_TOP, mPaint);
			xPos += mDayWidth;
			curDateMillis += ONEDAY_IN_MILLISECONDS;
		}
		return xPos;
	}

	/**
	 * Size is changed
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mDayWidth = w / 7;
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
			mTouchMovedOverDate = false;
			mLastPoint.x = x;
			mLastPoint.y = y;
			mDownPoint.x = x;
			mDownPoint.y = y;
			
			mHitDateInMillis = hitTest(x, y);
			mFocusDateInMillis = mHitDateInMillis;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDownFlag) {

				int yChanged = mLastPoint.y - y;
				if (yChanged != 0) {
					mCurOffset += yChanged;
					invalidate();
				}
				int xChanged = mLastPoint.x - x;
				
				/* If mouse movement is sufficient to consider user moves date, */
				if(xChanged > mDayWidth ||
				   yChanged > mWeekHeight) {
					mTouchMovedOverDate = true;
				}
				
				mHitDateInMillis = hitTest(x, y);
				mLastPoint.x = x;
				mLastPoint.y = y;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mMouseDownFlag) {
				/* User clicks on date */
				mHitDateInMillis = hitTest(x, y);
				if(this.mHitDateInMillis == this.mFocusDateInMillis) {
					
				}
			}
			mMouseDownFlag = false;
			mTouchMovedOverDate = false;
			break;
		}
		return true;
	}

	/**
	 * Hit test to find date
	 * @param x
	 * @param y
	 * @return
	 */
	private long hitTest(int x, int y) {
		Calendar cal = this.dateFromYOffset(y + mCurOffset);
		return cal.getTimeInMillis() + (x/mDayWidth) * ONEDAY_IN_MILLISECONDS; 
	}
}
