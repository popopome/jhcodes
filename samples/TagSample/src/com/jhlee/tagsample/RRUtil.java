package com.jhlee.tagsample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RRUtil {
	public static SimpleDateFormat mDataFormatter = new SimpleDateFormat("MM-dd-yyyy");
	public static SimpleDateFormat mGMTDataFormatter = new SimpleDateFormat("yyyy-MM-dd");
	static {
		mGMTDataFormatter.setTimeZone(new SimpleTimeZone(0, "GMT"));
		mDataFormatter.setTimeZone(new SimpleTimeZone(0, "GMT"));
	}
	public static String getTodayDateString() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		String dateString = formatter.format(new Date());
		return dateString;
	}
	
	public static String getCurrentTimeString() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		return formatter.format(new Date());
	}

	public static String formatCalendar(long timeInMillis) {
		return mDataFormatter.format(new Date(timeInMillis));
	}
	public static String formatGMTCalendar(long timeInMillis) {
		return mGMTDataFormatter.format(new Date(timeInMillis));
	}
	
	public static String formatMoney(long l, long m, boolean useDollarSign) {
		StringBuilder sb = new StringBuilder();
		if(useDollarSign)
			sb.append("$");
		sb.append(l);
		sb.append(".");
		if(m < 10)
			sb.append("0");
		sb.append(m);
		return sb.toString();
	}
	
	/*
	 * Create views from layout
	 */
	public static View createViewsFromLayout(Context ctx, int layoutId, ViewGroup parent) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater) ctx.getSystemService(infService);
		return li.inflate(layoutId, parent, true);
	}
}
