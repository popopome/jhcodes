package com.jhlee.vbudget.collect;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.util.RRUtil;

public class RRTakeReceiptActivity extends Activity implements RRCameraPreview.OnPictureTakenListener {
	
	private static final String TAG = "RRTakeShot";
	
	private RRImageStorageManager	mImgStg;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		/* Screen orientation to landscape */
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		/* Full screen */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.collect_takeshot);
		
		/* Get preview object */
		final RRCameraPreview preview = (RRCameraPreview)findViewById(R.id.rr_cam_preview);
		preview.setCaptureEventListener(this);
		
		/** Register button click callback function */
		final RRTakeReceiptActivity self = this;
		Button.OnClickListener btnClickListener = new Button.OnClickListener() {
			public void onClick(View arg0) {
				preview.takePicture();
			}
		};
		
		Button btnTakeShot = (Button) this.findViewById(R.id.ButtonTakeShot);
		btnTakeShot.setOnClickListener(btnClickListener);
		btnTakeShot.setOnLongClickListener(new Button.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				preview.startAutoFocus();
				return true;
			}
		});
		
		/* Give default focus to button */
		btnTakeShot.requestFocus();

	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mImgStg != null)
			return;
		
		mImgStg = new RRImageStorageManager();
		/* Check whether storage is available or not */
		if(false == mImgStg.open(this)) {
			Log.e(TAG, "unable to open image storage");
			Toast.makeText(this, "Please insert sd card first", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		
	}

	/**
	 * Picture is taken
	 */
	public void pictureTaken(Bitmap capturedBmp) {
		Log.d(TAG, "Capture image from camera");
		dumpBitmapInfo(capturedBmp);
		
		/* Rotate 90 degree */
		Matrix m = new Matrix();
		m.setRotate(90);
		Bitmap bmp = null;
		try {
			bmp = Bitmap.createBitmap(capturedBmp, 0, 0, capturedBmp.getWidth(),
				capturedBmp.getHeight(), m, false);	
		} catch(OutOfMemoryError oom) {
			/*
			 *  If bitmap rotation is failed,
			 *  we just use captured bitmap.
			 */
			Toast.makeText(this, "Unable to rotate bitmap", Toast.LENGTH_SHORT);
			bmp = capturedBmp;
		}
		
		/* Check storage card availability */
		final String TEMP_FILE_NAME = "temp_capture_image.jpg";
		File outputFile = new File(mImgStg.getBasePath(), TEMP_FILE_NAME);
		
		boolean saveResult = false;
		String absPath = "";
		try {
			/* Create new file */
			/* outputFile.createNewFile();*/
//			FileOutputStream stm = new FileOutputStream(outputFile.getAbsolutePath());
			
//			FileOutputStream stm = this.openFileOutput(TEMP_FILE_NAME, MODE_WORLD_READABLE);
//			FileOutputStream stm = this.openFileOutput(outputFile.getAbsolutePath(), MODE_WORLD_READABLE);
			FileOutputStream stm = RRUtil.openFileOutputStream(this, outputFile.getAbsolutePath());
			if(null == stm) {
				this.finish();
				return;
			}
			saveResult = bmp.compress(CompressFormat.JPEG, 100, stm);			
			stm.flush();
			stm.close();
			
			/* Get absolute path */
			absPath = outputFile.getAbsolutePath();
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Unable to save temporary image: " + e.toString());
			/* Show error message */
			Toast.makeText(this, "Unable to save captured file", Toast.LENGTH_SHORT).show();
			/* Finish activity */
			this.finish();
			return;
		}
		
		if(saveResult == false) {
			Log.e(TAG, "unable to save image to file");
			return;
		}
		
		
		/** Go to RRCaptured activity.
		 *  Pass captured file name.
		 */
		Intent i = new Intent(this, RRCaptureConfirmActivity.class);
		i.putExtra("PARAM_IMAGE_FILE", absPath);
		
		this.startActivity(i);
//		this.finish();
		
	}


	private void dumpBitmapInfo(Bitmap capturedBmp) {
		StringBuilder builder = new StringBuilder();
		builder.append(capturedBmp.getWidth());
		builder.append(capturedBmp.getHeight());
		builder.append(capturedBmp.getRowBytes());
		Log.d(TAG, "Captured image size: " + builder.toString());
	}

}
