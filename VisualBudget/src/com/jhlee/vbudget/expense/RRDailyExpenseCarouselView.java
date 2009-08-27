package com.jhlee.vbudget.expense;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import junit.framework.Assert;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselActiveItemChanged;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselActiveItemClickListener;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselItemCustomDrawListener;
import com.jhlee.vbudget.expense.RRCarouselFlowView.RRCarouselItem;
import com.jhlee.vbudget.util.RRUtil;

public class RRDailyExpenseCarouselView extends FrameLayout implements
		OnCarouselActiveItemClickListener, OnCarouselActiveItemChanged {
	private static final String POSTFIX_REFLECTION_BMP = "@#$";

	private FrameLayout	mFrameView;
	private RRCarouselFlowView mCarouselView;
	private RRDbAdapter mAdapter;
	private Cursor mCursor;
	private HashMap<String, Bitmap> mBmpPool = new HashMap<String, Bitmap>();

	private Matrix mMatrixZoomToFit = new Matrix();
	private RRTagDataProviderFromDb mTagDataProvider;

	/* CTORS */
	public RRDailyExpenseCarouselView(Context context) {
		this(context, null);
	}

	public RRDailyExpenseCarouselView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRDailyExpenseCarouselView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * Initialize views - Create view from layout and - set db related things.
	 */
	public boolean initializeViews(RRDbAdapter dbAdapter) {
		mFrameView = (FrameLayout) RRUtil.createViewsFromLayout(getContext(),
				R.layout.rr_carousel_receipt_list, this);
		mCarouselView = (RRCarouselFlowView) mFrameView.findViewById(R.id.carouselView);

		/* Install back button listener */
		Button backButton = (Button) mFrameView.findViewById(R.id.back_button);
		backButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				/* Finish activity */
				/* TODO: Remove this button */
			}
		});

		/* Collect receipt data from database */
		mAdapter = dbAdapter;
		refreshData();
		int numOfReceipts = mCursor.getCount();
		if (numOfReceipts < 1) {
			/* TODO: Show receipt data first */
			/*
			 * No data is in there. Just finisth the activity.
			 */
			Toast.makeText(getContext(),
					"No receipt data. Please take a receipt first.",
					Toast.LENGTH_LONG).show();
			return false;
		}

		/* Initialize carousel view */
		final RRCarouselFlowView carouselView = (RRCarouselFlowView) findViewById(R.id.carouselView);
		carouselView.initialize(numOfReceipts, 120, 160, 60, 9, 25);
		carouselView.setFocusable(true);
		carouselView.setOnActiveItemClickListener(this);
		carouselView.setOnActiveItemChangeListener(this);

		/* Set custom drawer */
		carouselView
				.setOnCarouselItemCustomDrawListener(new ReceiptItemCustomDrawer());

		/* Install money input dialog listener */
		initializeMoneyButtonHandler(carouselView);

		/* Install date change button */
		initializeDateChangeButton(carouselView);

		/* Initialize tag box and tag button */
		initializeTagBoxAndTagButton();

		/* Give default focus to carousel view */
		carouselView.requestFocus();

		return true;
	}

	/*
	 * Initialize tag box and tag button
	 */
	private void initializeTagBoxAndTagButton() {
		/* Set tag data provider */
		final RRTagBox tagBox = (RRTagBox) mFrameView.findViewById(R.id.tag_box);
		mTagDataProvider = new RRTagDataProviderFromDb(mAdapter);
		tagBox.setTagProvider(mTagDataProvider);

		/* Initialize tag button */
		Button tagButton = (Button) findViewById(R.id.button_tag);
		tagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/* Show tag box */
				RRTagBox tagBox = (RRTagBox) RRDailyExpenseCarouselView.this.findViewById(R.id.tag_box);
				tagBox.setVisibility(View.VISIBLE);

				/* Request layout */
				RRDailyExpenseCarouselView.this.findViewById(R.id.rr_receipt_carousel_list)
						.requestLayout();

				mTagDataProvider.setActiveReceiptId(getActiveReceiptId());

				/* Refresh tag data */
				tagBox.refreshTags();
			}
		});
	}

	private void initializeDateChangeButton(
			final RRCarouselFlowView carouselView) {
		Button datePickBtn = (Button) findViewById(R.id.button_date_pick);
		datePickBtn.setOnClickListener(new View.OnClickListener() {
			/*
			 * Date pick button is clicked. Show date pick dialog & change date
			 */
			public void onClick(View v) {
				final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(
						RRDailyExpenseCarouselView.this.getContext());
				dlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							public void onDismiss(DialogInterface dialog) {
								if (false == dlg.isDateSelected())
									return;
								/* Date is selected */
								RRCarouselItem item = carouselView
										.getActiveItem();
								mCursor.moveToPosition(item.seq);
								/* Assume 0th is id */
								int id = mCursor.getInt(0);
								/* Update db. */
								mAdapter.updateDate(mCursor, dlg
										.getSelectedDateInMillis());
								/* Refresh db cursor */
								RRDailyExpenseCarouselView.this.refreshData();

								/*
								 * Let's keep current view position. To do that
								 * first we find new sequence of the item and
								 * then move to that sequence.
								 */
								int newSeq = 0;
								mCursor.moveToFirst();
								while (mCursor.isAfterLast() == false) {
									if (mCursor.getInt(0) == id)
										break;
									mCursor.moveToNext();
									++newSeq;
								}
								carouselView.setActiveItem(newSeq);

								/* Invalidate view */
								carouselView.invalidate();

							}
						});

				/* Get date information */
				mCursor.moveToPosition(carouselView.getActiveItem().seq);
				String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
				dlg.setActiveDate(dateStr);

				/* Show dialog */
				dlg.show();
			}
		});
	}

	private void initializeMoneyButtonHandler(
			final RRCarouselFlowView carouselView) {
		Button moneyButton = (Button) this.findViewById(R.id.button_numpad);
		moneyButton.setOnClickListener(new Button.OnClickListener() {
			/* Money input button is clicked */
			public void onClick(View v) {

				final RRCarouselItem item = carouselView.getActiveItem();
				/* Move cursor position */
				mCursor.moveToPosition(item.seq);
				/* Get total amount of money */
				int packedTotal = mCursor.getInt(mCursor
						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));

				/* Show money input dialog */
				final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(RRDailyExpenseCarouselView.this.getContext());
				inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
				inputDlg.setOnDismissListener(new OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						/*
						 * Money input dialog is dismissed. Let's save if we
						 * should do
						 */
						if (inputDlg.isCanceled())
							return;

						/* Insert new total money to db */
						mCursor.moveToPosition(item.seq);
						int rid = mCursor.getInt(0);
						mAdapter.updateTotalMoney(rid, inputDlg.getDollars(),
								inputDlg.getCents());
						/* Refresh db items */
						RRDailyExpenseCarouselView.this.refreshData();
						/* Invalidate screen */
						carouselView.invalidate();
					}
				});
				inputDlg.show();
			}
		});
	}

	public void refreshData() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		mCursor = mAdapter.queryAllReceipts();
	}

	/**
	 * Update matrix in order to show bitmap on center
	 */
	private void zoomToFit(int dstX, int dstY, int dstW, int dstH,
			Bitmap srcBmp, Matrix mat) {
		RectF rcBmp = new RectF(0.0f, 0.0f, (float) srcBmp.getWidth(),
				(float) srcBmp.getHeight());
		RectF rcScreen = new RectF(dstX, dstY, dstX + dstW, dstY + dstH);

		mat.reset();
		mat.setRectToRect(rcBmp, rcScreen, Matrix.ScaleToFit.CENTER);
	}

	/**
	 * Item is clicked within carousel item
	 */
	public void onClicked(RRCarouselFlowView view, RRCarouselItem item) {
		mCursor.moveToPosition(item.seq);
		long rid = mCursor.getInt(0);

		/** See receipt list */
		/*
		 * Intent i = new Intent(this, RRReceiptDetailActivity.class);
		 * i.putExtra(RRReceiptDetailActivity.RECEIPT_ID, rid);
		 * this.startActivity(i);
		 */
	}

	/*
	 * Get active receipt id
	 */
	private int getActiveReceiptId() {
		RRCarouselFlowView view = (RRCarouselFlowView) this
				.findViewById(R.id.carouselView);
		Assert.assertTrue(view != null);

		RRCarouselItem item = view.getActiveItem();
		Assert.assertTrue(item != null);

		mCursor.moveToPosition(item.seq);
		/* Assume 0 indicates id */
		return mCursor.getInt(0);
	}

	/*
	 * Active item is changed
	 */
	public void onActiveItemChanged(RRCarouselFlowView view, RRCarouselItem item) {
		/*
		 * If tag view is opened, let's refresh tag view
		 */
		RRTagBox tagBox = (RRTagBox) findViewById(R.id.tag_box);
		if (tagBox.getVisibility() == View.VISIBLE) {
			String activeTag = tagBox.getActiveTag();

			/* Set active tag */
			mTagDataProvider.setActiveReceiptId(getActiveReceiptId());
			tagBox.refreshTags();

			tagBox.scrollToTag(activeTag);
		}
	}

	private class ReceiptItemCustomDrawer implements
			OnCarouselItemCustomDrawListener {
		private static final int TEXT_SIZE = 20;
		private Paint mPaint;
		private Typeface mFont;
		private SimpleDateFormat mDateFormatter = new SimpleDateFormat(
				"MM-dd-yyyy");

		public ReceiptItemCustomDrawer() {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(0xFFFF0000);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint.setStrokeWidth(1);

			mFont = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
			mPaint.setTypeface(mFont);
			mPaint.setTextSize(TEXT_SIZE);
			mPaint.setTextAlign(Paint.Align.CENTER);
		}

		/**
		 * Custome draw for CAROUSL item
		 */
		public void onDraw(View view, Canvas canvas, RRCarouselItem item,
				boolean isItemActive) {
			/*
			 * If image file name was not fetched from DB, get it.
			 */
			mCursor.moveToPosition(item.seq);
			String imgFileName = mCursor.getString(mCursor
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_SMALL_IMG_FILE));

			/* Look up bitmap image from bitmap pool */
			Bitmap bmp = null;
			Bitmap reflectedBmp = null;
			if (true == mBmpPool.containsKey(imgFileName)) {
				/* We found already loaded bitmap */
				bmp = mBmpPool.get(imgFileName);
				reflectedBmp = mBmpPool.get(imgFileName
						+ POSTFIX_REFLECTION_BMP);
			} else {
				/* Load bitmap from file */
				bmp = BitmapFactory.decodeFile(imgFileName);
				mBmpPool.put(imgFileName, bmp);
				/* Generate reflected bitmap */
				reflectedBmp = this.createReflectedBitmap(bmp);
				mBmpPool
						.put(imgFileName + POSTFIX_REFLECTION_BMP, reflectedBmp);
			}

			int x = item.x - (item.w >> 1);
			int y = item.y - item.h * 2 / 3;

			/* Compute zoom to fit matrix */
			zoomToFit(x, y, item.w, item.h, bmp, mMatrixZoomToFit);
			canvas.drawBitmap(bmp, mMatrixZoomToFit, mPaint);

			/* Draw reflection image */
			mMatrixZoomToFit.postTranslate(0, item.h);
			canvas.drawBitmap(reflectedBmp, mMatrixZoomToFit, mPaint);

			/*
			 * Draw money & date
			 */
			drawReceiptInformation(canvas, item, isItemActive, y);
		}

		/*
		 * Draw receipt information
		 */
		private void drawReceiptInformation(Canvas canvas, RRCarouselItem item,
				boolean isItemActive, int y) {
			/* Draw money */
			installShadow();
			mCursor.moveToPosition(item.seq);
			long total = mCursor.getLong(mCursor
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
			mPaint.setColor(Color.WHITE);
			mPaint.setTextAlign(Paint.Align.CENTER);

			String moneyString = RRUtil.formatMoney((int) total / 100,
					(int) total % 100, true);
			canvas
					.drawText(moneyString, item.x, y + item.h + TEXT_SIZE,
							mPaint);

			/* Only active item represents date */
			if (isItemActive) {
				/* Draw date */
				/* Format date */
				String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
				canvas.drawText(dateStr, item.x, y + item.h + TEXT_SIZE
						+ TEXT_SIZE, mPaint);

				/* Draw tag information */
				int rid = mCursor.getInt(0);
				String allTags = mAdapter.queryReceiptTagsAsOneString(rid);
				if (allTags.length() > 0) {
					/* There is tags */
					canvas.drawText(allTags, item.x, y + item.h + TEXT_SIZE
							+ TEXT_SIZE + TEXT_SIZE, mPaint);
				}
			}
			uninstallShadow();
		}

		private void installShadow() {
			mPaint.setShadowLayer((float) 2.0, 0, 0, Color.BLACK);
		}

		private void uninstallShadow() {
			mPaint.setShadowLayer((float) 0.0, 0, 0, Color.BLACK);
		}

		/**
		 * Create refelected bitmap
		 * 
		 * @param src
		 * @return
		 */
		private Bitmap createReflectedBitmap(Bitmap srcBmp) {

			int srcHeight = srcBmp.getHeight();
			int srcWidth = srcBmp.getWidth();

			int reflectionHeight = srcHeight >> 1;

			Bitmap newBmp = Bitmap.createBitmap(srcWidth, reflectionHeight,
					Bitmap.Config.ARGB_8888);

			int[] pixels = new int[srcWidth];

			int srcReflectionTop = srcHeight - reflectionHeight;
			int alpha = newBmp.getHeight();
			int newY = 0;
			for (int y = srcBmp.getHeight() - 1; y > srcReflectionTop; --y, --alpha, ++newY) {
				srcBmp.getPixels(pixels, 0, srcWidth, 0, y, srcWidth, 1);

				for (int x = srcWidth - 1; x >= 0; --x) {
					pixels[x] = (pixels[x] & 0x00FFFFFF) | (alpha << 24);
				}

				newBmp.setPixels(pixels, 0, srcWidth, 0, newY, srcWidth, 1);
			}

			return newBmp;
		}
	}
}
