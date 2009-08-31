package com.jhlee.vbudget.collect;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class RRImageStorageManager {

	private static final String STORAGE_FOLDER_NAME = "budgeting";
	private static final String TAG = "RrimageStorageManager";
	private static final String PHOTO_FOLDER_NAME = "photos";
	
	/* Image storage path */
	private File mImageStoragePath;
	private File mPhotoFolderPath;
	private Context mCtx;

	/**
	 * Check whether storage card is available or not
	 * 
	 * @return
	 */
	public static boolean isStorageCardAvailable() {
		String storageState = Environment.getExternalStorageState();
		return storageState.contains("mounted");
	}

	/*
	 * Open storage
	 */
	public boolean open(Context ctx) {
		/* Keep context */
		mCtx = ctx;

		boolean useDefaultStorage = true;
		if(isStorageCardAvailable()) {
			if(initializeExternalStorage()) {
				useDefaultStorage = false;
			}
		}
		
		if(useDefaultStorage) {
			Log.e(TAG, "Storage is not available");
			
			File f = ctx.getFileStreamPath("temp");
			mImageStoragePath = f.getParentFile();
			
			prepareDefaultSavingDirectoryIfNotExists();
		}
		
		return true;
	}

	/*
	 * Prepare default photo saving directory
	 */
	private boolean prepareDefaultSavingDirectoryIfNotExists() {
		mPhotoFolderPath = mCtx.getFileStreamPath(PHOTO_FOLDER_NAME);
		if (true == mPhotoFolderPath.exists()) {
			return true;
		}

		Log
				.v(TAG,
						"Receipt saving folder does not exist. Let's make directory");
		if (false == mPhotoFolderPath.mkdir()) {
			Log.e(TAG, "Unable to create receipts folder");
			return false;
		}

		Log.v(TAG, "Succeed to make receipt saving directory:"
				+ mPhotoFolderPath.getAbsolutePath());
		return true;
	}
	
	private boolean initializeExternalStorage() {
		File sd = Environment.getExternalStorageDirectory();
		String sdPath = sd.getAbsolutePath();
		String r2StoragePath = sdPath + "/" + STORAGE_FOLDER_NAME;

		mImageStoragePath = new File(r2StoragePath);
		if (mImageStoragePath.exists() == false) {
			/* Let's create r2 storage path */
			if (false == mImageStoragePath.mkdirs()) {
				Log.e(TAG, "Unable to create storage path");
				return false;
			}
		}
		
		String photoStoragePath = mImageStoragePath.getAbsolutePath();
		String fullPath = photoStoragePath + "/" + PHOTO_FOLDER_NAME;
		mPhotoFolderPath = new File(fullPath);
		if(mPhotoFolderPath.exists() == false) {
			/* Let's create photo folder path */
			if(false == mPhotoFolderPath.mkdir()) {
				Log.e(TAG, "Unable to create photo path");
				return false;
			}
		}
		
		return true;
	}

	/*
	 * Return bas epath
	 */
	public String getBasePath() {
		return mImageStoragePath.getAbsolutePath();
	}
	
	public File getPhotoFolder() {
		return mPhotoFolderPath;
	}
}
