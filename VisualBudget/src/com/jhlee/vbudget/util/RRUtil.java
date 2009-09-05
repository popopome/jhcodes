package com.jhlee.vbudget.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RRUtil {
	private static final String TAG = "RRUtil";
	public static String[] mMonthStr;
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
	
	public static String getCurrentYearMonthString() {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy");
		return formatter.format(new Date());
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
	
	
	public static String formatMoney(long fixed, boolean useDollarSign) {
		long sign = (fixed < 0) ? -1 : 1;
		long absFixed = Math.abs(fixed);
		return formatMoney(sign*(absFixed/100), absFixed%100, useDollarSign);
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
	
	public static FileOutputStream openFileOutputStream(Activity activity,
			String filePath) {
		FileOutputStream stm = null;
		try {
			/* Create new file */
			/* outputFile.createNewFile();*/
			stm = new FileOutputStream(filePath);
		} catch(Exception e) {
			try {
				stm = activity.openFileOutput(filePath, Context.MODE_WORLD_READABLE);
			} 
			catch(Exception e2) {
				Log.e(TAG, "Unable to open file");
				return null;
			}
		}
		
		return stm;
	}
	
	public static FileInputStream openFileInputStream(Activity activity,
			String filePath) {
		FileInputStream stm = null;
		try {
			/* Create new file */
			/* outputFile.createNewFile();*/
			stm = new FileInputStream(filePath);
		} catch(Exception e) {
			try {
				stm = activity.openFileInput(filePath);
			} 
			catch(Exception e2) {
				Log.e(TAG, "Unable to open file");
				return null;
			}
		}
		
		return stm;
	}
	
	public static boolean deleteFile(String filePath) {
		try {
			File f = new File(filePath);
			if(f.exists()) {
				if(false == f.delete()) {
					Log.e(TAG, "Unable to delete file:filePath=" + filePath);
					return false;
				}
			}
			
		} catch(Exception e) {
			Log.e(TAG, "Unable to delete file:filePath=" + filePath);
			return false;
		}
		return true;
	}
}
