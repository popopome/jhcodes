package com.jhlee.calendarsample;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class CalendarStreamView extends View {

	public static final String	TAG = "CalendarStreamView";
	public static final long ONEWEEK_IN_MILLISECONDS = 60 * 60 * 24 * 7 * 1000;
	
	private Calendar mBaseDate = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
	private long mBaseDateInMillis = 0;
	private Calendar mDate = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
	private int 	mWeekHeight = 30;
	private long mCurYOffset  = 0;
		

					
		
	/**
	 * Compute DATE from Y-Offset
	 * @param yOffset
	 * @return
	 */
	public Calendar dateFromYOffset(int yOffset) {
			long nthWeeks = yOffset / mWeekHeight;
			long millsSinceBaseDate = nthWeeks * ONEWEEK_IN_MILLISECONDS;
			mDate.clear();
			mDate.set(Calendar.MILLISECOND, 0);
			mDate.setTimeInMillis(mBaseDate.getTimeInMillis() + millsSinceBaseDate);

			/* We SHOULD call to update mDate internal data structure. 
			 * Java is really strange for this thing
			 **/
			mDate.getTimeInMillis();
			return mDate;
		}
		
		/**
		 * Compute Y-Offset from DATE
		 * @param date
		 * @return
		 */
		public long yOffsetFromDate(Calendar cal) {
			long mills = cal.getTimeInMillis();
			long nthWeeks = (mills - mBaseDateInMillis)/ONEWEEK_IN_MILLISECONDS;
			return nthWeeks * mWeekHeight;
		}
		
		/**
		 * Move offset to month which includes given date.
		 * @param cal
		 */
		public void moveToMonth(Calendar cal) {
			mCurYOffset = yOffsetFromDate(cal);
		}
	}

	/*-----------------------------------------------------------------------------*/
	/* MEMBER VARIABLES 
	 *-----------------------------------------------------------------------------*/
	private CalendarCoordinate mCoord = new CalendarCoordinate();
	
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
		/* Base date is 1989-12-31 0, 0, 0
		 * It was Sunday and offset 0 will be mapped onto this day.
		 * Java Calendar use 0-based index for month
		 */
		mBaseDate.set(1989, 11, 31, 0, 0, 0);
		mBaseDate.set(Calendar.MILLISECOND, 0);
		mBaseDateInMillis = mBaseDate.getTimeInMillis(); 

		
		mCoord.moveToMonth(Calendar.getInstance());
	}
	
	
}
