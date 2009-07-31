package com.jhlee.calendarsample;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import junit.framework.Assert;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CalendarStreamView extends View {

	public static final String TAG = "CalendarStreamView";
	public static final long ONEWEEK_IN_MILLISECONDS = 60 * 60 * 24 * 7 * 1000;
	private static final int COLOR_SUNDAY = Color.rgb(255, 0, 0);
	private static final int COLOR_SATURDAY = Color.rgb(0, 0, 255);
	private static final int COLOR_WEEKDAY = Color.BLACK; 
	
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
	private int mEvenMonthColor = Color.rgb(192, 192, 192);
	private int mOddMonthColor = Color.rgb(172,172,172);
	private int mTextHeight = 0;
	private int mDayTextSize = 13;
	private int mYearMonthTextSize = 60;
	
	private int mDayRightPadding = 3;
	private int mDayTopPadding = 3;

	private boolean mMouseDownFlag = false;
	private Point mLastPoint = new Point();
	private Point mDownPoint = new Point();

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

		/*
		 * We SHOULD call to update mDate internal data structure. Java is
		 * really strange for this thing
		 */
		mDate.getTimeInMillis();
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

			/* Compute next week information */
			cal = this.dateFromYOffset(offset + mWeekHeight);
			nextWeekStartDay = cal.get(Calendar.DAY_OF_MONTH);
			nextWeekMonth = cal.get(Calendar.MONTH);
			nextWeekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			nextYear = cal.get(Calendar.YEAR);

			/* If all days of current week are in same month,
			 * ...
			 */
			if ((curWeekMonth == nextWeekMonth)
					|| ((curWeekMonth != nextWeekMonth) && (nextWeekStartDay == 1))) {
				if ((curWeekMonth & 0x01) == 0x01)
					bgColor = mOddMonthColor;
				else
					bgColor = mEvenMonthColor;

				/* Draw bakground */
				int xPos = 0;
				for (int dayIndex = 0; dayIndex < 7; ++dayIndex) {
					mPaint.setColor(bgColor);
					if (dayIndex == 6) {
						canvas.drawRect(xPos, screenY, this.getWidth(), screenY
								+ mWeekHeight, mPaint);
					} else {
						canvas.drawRect(xPos, screenY, xPos + mDayWidth,
								screenY + mWeekHeight, mPaint);
					}
				}
				
				/* Draw year/month information if current week is third one */
				if(3 == curWeekOfMonth) {
					mPaint.setTextSize(mYearMonthTextSize);
					mPaint.setColor(Color.rgb(192, 192, 192));
					
					String ymStr = Integer.toString(curYear);
					ymStr += "." + mNumString[curWeekMonth];
					canvas.drawText(ymStr, this.getWidth(), screenY+mWeekHeight, mPaint);
					mPaint.setTextSize(mDayTextSize);
				}
				
				/* Draw day text */
				mPaint.setColor(COLOR_SUNDAY);
				xPos = 0;
				for (int dayIndex = 0; dayIndex < 7; ++dayIndex) {
					canvas.drawText(mNumString[curWeekStartDay + dayIndex - 1],
							xPos + mDayWidth - mDayRightPadding, screenY
									+ mTextHeight + mDayTopPadding, mPaint);
					xPos += mDayWidth;
					/* Prepare color for next day. 
					 * I checked here if current day is friday,
					 * then use saturday color for next day.
					 */
					if(dayIndex == 5)
						mPaint.setColor(COLOR_SATURDAY);
					else
						mPaint.setColor(COLOR_WEEKDAY);
				}
			} else {
				
				/*
				 * We know current week has days from different month. And
				 * nextWeekStartDay SHOULD be less than 7 because next week is
				 * first week of that month.
				 */
				int xPos = 0;
				int cnt = 7 - (nextWeekStartDay - 1);
				int dayIndex = 0;

				/* Decide background color */
				if ((curWeekMonth & 0x01) == 0x01)
					bgColor = mOddMonthColor;
				else
					bgColor = mEvenMonthColor;

				
				for (dayIndex = 0; dayIndex < cnt; ++dayIndex) {
					/* Draw background */
					mPaint.setColor(bgColor);
					canvas.drawRect(xPos, screenY, xPos + mDayWidth, screenY
							+ mWeekHeight, mPaint);
					/* Draw day text */
					if(0 == dayIndex)
						mPaint.setColor(COLOR_SUNDAY);
					else
						mPaint.setColor(COLOR_WEEKDAY);

					canvas.drawText(mNumString[curWeekStartDay + dayIndex - 1],
							xPos + mDayWidth - mDayRightPadding, screenY
									+ mTextHeight + mDayTopPadding, mPaint);
					xPos += mDayWidth;
				}

				/* This is next month. We set different background color. */
				if ((nextWeekMonth & 0x01) == 0x01)
					bgColor = mOddMonthColor;
				else
					bgColor = mEvenMonthColor;

				cnt = nextWeekStartDay - 1;
				for (dayIndex = 0; dayIndex < cnt; ++dayIndex) {
					mPaint.setColor(bgColor);

					/* Is this saturday? */
					if (dayIndex == cnt - 1) {
						canvas.drawRect(xPos, screenY, this.getWidth(), screenY
								+ mWeekHeight, mPaint);
						mPaint.setColor(COLOR_SATURDAY);
					} else {
						/* Regular week day */
						canvas.drawRect(xPos, screenY, xPos + mDayWidth,
								screenY + mWeekHeight, mPaint);
						mPaint.setColor(COLOR_WEEKDAY);
					}
					canvas.drawText(mNumString[dayIndex], xPos + mDayWidth
							- mDayRightPadding, screenY + mTextHeight
							+ mDayTopPadding, mPaint);
					xPos += mDayWidth;
				}
			}
		}
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
			mLastPoint.x = x;
			mLastPoint.y = y;
			mDownPoint.x = x;
			mDownPoint.y = y;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMouseDownFlag) {

				int changed = mLastPoint.y - y;
				if (changed != 0) {
					mCurOffset += changed;
					invalidate();
				}
				
				mLastPoint.x = x;
				mLastPoint.y = y;
			}
			break;
		case MotionEvent.ACTION_UP:
			mMouseDownFlag = false;
			break;
		}
		return true;
	}

}
