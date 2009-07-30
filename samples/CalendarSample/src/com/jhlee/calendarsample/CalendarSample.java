package com.jhlee.calendarsample;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class CalendarSample extends Activity {
	public class CalendarCoordinate {
		public static final long ONEWEEK_IN_MILLISECONDS = 60 * 60 * 24 * 7 * 1000;
		public Calendar mBaseDate = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		public long mBaseDateInMillis = 0;
		public Calendar mDate = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		public int 	mWeekHeight = 30;
		
		/**
		 * CTOR
		 */
		public CalendarCoordinate() {
			/* Base date is 1989-12-31 0, 0, 0
			 * It was Sunday and offset 0 will be mapped onto this day.
			 */
			mBaseDate.set(1989, 12, 31, 0, 0, 0);
			mBaseDate.set(Calendar.MILLISECOND, 0);
			mBaseDateInMillis = mBaseDate.getTimeInMillis();
			mBaseDate.setTimeInMillis(mBaseDateInMillis);
			mBaseDate.setTime(new Date(mBaseDateInMillis));
			Log.v(TAG, "BaseDate:" + mBaseDate.toString());
		}
		
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
			/*mDate.setTimeInMillis(mBaseDate.getTimeInMillis() + millsSinceBaseDate);*/
			mDate.setTimeInMillis(mBaseDate.getTimeInMillis());

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
	}
	
	public static final String TAG = "CALENDARSAMPLE";
	private CalendarCoordinate mCoord = new CalendarCoordinate();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Calendar cal = mCoord.dateFromYOffset(0);
        Log.v(TAG, cal.toString());
        
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
    }
}