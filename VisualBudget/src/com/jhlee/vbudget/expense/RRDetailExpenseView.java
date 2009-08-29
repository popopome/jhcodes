package com.jhlee.vbudget.expense;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.tags.RRTagDataProviderFromDb;
import com.jhlee.vbudget.tags.RRTagSelectDialog;
import com.jhlee.vbudget.util.RRUtil;

/**
 * Handle detail information about receipt
 * 
 * @author popopome
 */
public class RRDetailExpenseView extends FrameLayout {
	public static final String RECEIPT_ID = "rid";
	private static final String TAG = "RREditor";
	private RRDbAdapter mDbAdapter;
	private Cursor mCursor;
	private int mRID;
	
	private float mZoomRatioAtDown;
	
	private View	mFrame;
	
	private RRTagDataProviderFromDb	mTagDataProvider;
	
	


	public RRDetailExpenseView(Context context) {
		this(context, null);
	}
	public RRDetailExpenseView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public RRDetailExpenseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mFrame = (View)RRUtil.createViewsFromLayout(context, R.layout.expense_detail, this);
	}
	
	public void setExpense(RRDbAdapter dbAdapter, int expenseId) {
		mRID = expenseId;
		if (mRID == -1) {
			this.showErrorMessage("Proper rid is not given");
			return;
		}

		/* Get receipt information */
		mDbAdapter = dbAdapter;
		refreshDbCursor();

		/* Load receipt image */
		int colIndexImgFile = mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_IMG_FILE);
		int colCount = mCursor.getColumnCount();
		String imgFilePath = mCursor.getString(colIndexImgFile);
		
		Bitmap bmp = null;
		if( 0 == imgFilePath.compareTo("NT")) {
			bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.new_trans);
		} else {
			bmp = BitmapFactory.decodeFile(imgFilePath);	
		}
		
		if (null == bmp) {
			this.showErrorMessage("Unable to load file:" + imgFilePath);
			return;
		}

		/* Set bitmap */
		final RRZoomView zoomView = (RRZoomView) this.findViewById(R.id.rr_zoomview);
		zoomView.setBitmap(bmp);

		/* Set up money text view */
		refreshMoneyViewText();
		/* Set date */
		refreshDateView();
		/* Set tag info view */
		refreshTagInfoView();

		final RRDetailExpenseView self = this;

		/* Initialize money button */
		initializeMoneyButton(self);
		/* Initialize date pick button */
		initializeDatePickButton(self);
		/* Initialize tag box and tag button */
		initializeTagBoxAndTagButton(self);
		
		
		/* Connect zoom button view & zoom view */
		final RRZoomButtonView zoomBtnView = (RRZoomButtonView)this.findViewById(R.id.rr_zoombutton_view);
		zoomBtnView.setOnZoomButtonEventListener(new RRZoomButtonView.OnZoomButtonEventListener() {
			public void onZoomButtonBeforeMoving(View view) {
				mZoomRatioAtDown = zoomView.getCurrentZoomRatio();
			}
			public void onZoomButtonMoved(View view, boolean isPlusButton, long distance) {
				float zoomRatioOffset = (float) (1.0 * (distance / (float)view.getHeight()));
				zoomView.zoomTo(mZoomRatioAtDown + zoomRatioOffset);
				zoomView.invalidate();
			}
			public void onZoomButtonAfterMoving(View view) {
				zoomView.invalidate();
			}
		});
		
		/*
		 * Request layout again. Expect view size is changed with proper content
		 */
		View rootView = this.findViewById(R.id.receipt_detail_layout);
		rootView.requestLayout();
	}

	/**
	 * Refresh date view
	 */
	private void refreshDateView() {
		TextView dateView = (TextView) findViewById(R.id.date_view);
		
		String dateString = RRUtil.formatCalendar(mCursor.getLong(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
		dateView.setText(dateString);
		dateView.invalidate();
	}

	private void refreshMoneyViewText() {
		TextView moneyView = (TextView) findViewById(R.id.money_view);
		int total = mCursor.getInt(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
		moneyView.setText(RRUtil.formatMoney(total / 100, total % 100, true));
		moneyView.invalidate();
	}

	private void initializeMoneyButton(final RRDetailExpenseView self) {
		Button moneyButton = (Button) this.findViewById(R.id.button_numpad);
		moneyButton.setOnClickListener(new Button.OnClickListener() {
			/* Money input button is clicked */
			public void onClick(View v) {
				/* Get total amount of money */
				int packedTotal = mCursor.getInt(mCursor
						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));

				/* Show money input dialog */
				final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(RRDetailExpenseView.this.getContext());
				inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
				inputDlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							public void onDismiss(DialogInterface dialog) {
								/*
								 * Money input dialog is dismissed. Let's save
								 * if we should do
								 */
								if (inputDlg.isCanceled())
									return;

								/* Insert new total money to db */
								int rid = mCursor.getInt(0);
								mDbAdapter.updateTotalMoney(rid, inputDlg
										.getDollars(), inputDlg.getCents());
								/* Refresh db items */
								self.refreshDbCursor();
								self.refreshMoneyViewText();
							}
						});
				inputDlg.show();
			}
		});
	}

	

	private void showErrorMessage(String msg) {
		Log.e(TAG, msg);
		Toast.makeText(this.getContext(), "Error happend:" + msg, Toast.LENGTH_LONG).show();	
		return;
	}

	/*
	 * Refresh db cursor
	 */
	private void refreshDbCursor() {
		Cursor c = mCursor;
		if(c != null) {
			c.requery();
			c.moveToFirst();
		} else {
			c = mDbAdapter.queryReceipt((int) mRID);
			if (null == c) {
				this.showErrorMessage("Unable to get db cursor");
				return;
			}
			/* Set cursor back */
			mCursor = c;
		}
	}
	
	private void initializeDatePickButton(final RRDetailExpenseView self) {
		Button datePickBtn = (Button)findViewById(R.id.button_date_pick);
		datePickBtn.setOnClickListener(new View.OnClickListener() {
			/*
			 * Date pick button is clicked. 
			 * Show date pick dialog & change date
			 */
			public void onClick(View v) {
				final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(self.getContext());
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if(false == dlg.isDateSelected())
							return;
						/* Update db. */
						mDbAdapter.updateDate(
								mCursor,
								dlg.getSelectedDateInMillis());
						/* Refresh db cursor */
						self.refreshDbCursor();
						self.refreshDateView();
												
					}
				});
				
				/* Get date information */
				String dateStr =
					RRUtil.formatCalendar(mCursor.getLong(mCursor.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
					
				dlg.setActiveDate(dateStr);
				
				/* Show dialog */
				dlg.show();
			}
		});
	}
	
	/*
	 * Initialize tag box and tag button
	 */
	private void initializeTagBoxAndTagButton(final RRDetailExpenseView self) {
		/* Set tag data provider */
//		final RRTagBox tagBox = (RRTagBox) self.findViewById(R.id.tag_box);
		mTagDataProvider = new RRTagDataProviderFromDb(mDbAdapter);
//		tagBox.setTagProvider(mTagDataProvider);
//		tagBox.setOnTagItemStateChangeListener(new RRTagStreamView.OnTagItemStateChangeListener() {
//			/*
//			 * Let's update tag item status
//			 */
//			public void onTagItemStateChanged(String tag, boolean checked) {
//				RRDetailExpenseView.this.refreshTagInfoView();
//				
//			}
//		});
		
		
		/* Initialize tag button */
		Button tagButton = (Button)findViewById(R.id.button_tag);
		tagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				/* Show tag box */
//				RRTagBox tagBox = (RRTagBox) self.findViewById(R.id.tag_box);
//				tagBox.setVisibility(View.VISIBLE);
				
				mTagDataProvider.setActiveReceiptId(mRID);
				
				/* Show tag selection dialog */
				final RRTagSelectDialog dlg = new RRTagSelectDialog(RRDetailExpenseView.this.getContext());
				dlg.initialize(mTagDataProvider);
				dlg.show();
			}
		});
	}

	/*
	 * Refresh tag info view
	 */
	private void refreshTagInfoView() {
		String tagStr = mDbAdapter.queryReceiptTagsAsMultiLineString(mRID);
		TextView tagInfoView = (TextView)findViewById(R.id.tag_info_view);
		tagInfoView.setText(tagStr);
	}
}

