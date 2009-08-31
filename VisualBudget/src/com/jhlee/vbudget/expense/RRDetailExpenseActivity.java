package com.jhlee.vbudget.expense;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.plan.RRBudgetSelectDialog;
import com.jhlee.vbudget.tags.RRTagDataProviderFromDb;
import com.jhlee.vbudget.tags.RRTagSelectDialog;
import com.jhlee.vbudget.util.RRUtil;

/**
 * Handle detail information about receipt
 * 
 * @author popopome
 */
public class RRDetailExpenseActivity extends Activity {
	public static final String RECEIPT_ID = "rid";
	private static final String TAG = "RREditor";
	private RRDbAdapter mDbAdapter;
	private Cursor mCursor;
	private int mRID;

	private float mZoomRatioAtDown;

	private RRTagDataProviderFromDb mTagDataProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Remove title bar */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		/* Fullscreen */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		this.setContentView(R.layout.expense_detail);

		/** Get receipt id */
		Intent i = this.getIntent();
		if (null == i) {
			return;
		}
		mRID = (int) i.getExtras().getLong(RECEIPT_ID, -1);
		if (mRID == -1) {
			return;
		}

		/** Get receipt information */
		mDbAdapter = new RRDbAdapter(this);
		mDbAdapter.setOwner(this);

		this.setExpense(mRID);
	}

	public void setExpense(int expenseId) {
		/* Get receipt information */
		refreshDbCursor();

		/* Load receipt image */
		int colIndexImgFile = mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_IMG_FILE);
		int colCount = mCursor.getColumnCount();
		String imgFilePath = mCursor.getString(colIndexImgFile);

		Bitmap bmp = null;
		if (0 == imgFilePath.compareTo("NT")) {
			bmp = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.new_trans);
		} else {
			bmp = BitmapFactory.decodeFile(imgFilePath);
		}

		if (null == bmp) {
			this.showErrorMessage("Unable to load file:" + imgFilePath);
			return;
		}

		/* Set bitmap */
		final RRZoomView zoomView = (RRZoomView) this
				.findViewById(R.id.rr_zoomview);
		zoomView.setBitmap(bmp);

		/* Set up money text view */
		refreshMoneyViewText();
		/* Set date */
		refreshDateView();
		/* Set tag info view */
		refreshTagInfoView();
		/* Budget view */
		refreshBudgetView();

		final RRDetailExpenseActivity self = this;

		/*
		 * Initialize tag data provider
		 */
		mTagDataProvider = new RRTagDataProviderFromDb(mDbAdapter);
		
		/* Connect zoom button view & zoom view */
		final RRZoomButtonView zoomBtnView = (RRZoomButtonView) this
				.findViewById(R.id.rr_zoombutton_view);
		zoomBtnView
				.setOnZoomButtonEventListener(new RRZoomButtonView.OnZoomButtonEventListener() {
					public void onZoomButtonBeforeMoving(View view) {
						mZoomRatioAtDown = zoomView.getCurrentZoomRatio();
					}

					public void onZoomButtonMoved(View view,
							boolean isPlusButton, long distance) {
						float zoomRatioOffset = (float) (1.0 * (distance / (float) view
								.getHeight()));
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

	/*
	 * Create options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_detail_view, menu);

		/* Consumed */
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Menu item is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_amount:
			changeAmountWithUser();
			break;
		case R.id.menu_tag:
			changeTagWithUser();
			break;
		case R.id.menu_date:
			changeDateWithUser();
			break;
		case R.id.menu_budget:
			changeBudgetWithUser();
			break;

		}
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	private void changeBudgetWithUser() {
		/* Show budget dialog */
		final RRBudgetSelectDialog budgetDlg = new RRBudgetSelectDialog(this);
		budgetDlg.initialize(mDbAdapter);
		budgetDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (budgetDlg.isCanceled())
					return;

				String budgetName = budgetDlg.getSelectedBudgetName();
				int budgetYear = budgetDlg.getSelectedBudgetYear();
				int budgetMonth = budgetDlg.getSelectedBudgetMonth();
				long budgetBalance = budgetDlg.getSelectedBudgetBalance();

				mDbAdapter.changeBudget(mRID, budgetYear, budgetMonth,
						budgetName);
				
				refreshDbCursor();
				refreshBudgetView();
			}

		});

		budgetDlg.show();
	}

	/*
	 * Date pick button is clicked. Show date pick dialog & change date
	 */
	private void changeDateWithUser() {
		final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(this);
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				if (false == dlg.isDateSelected())
					return;
				/* Update db. */
				mDbAdapter.updateDate(mCursor, dlg.getSelectedDateInMillis());
				/* Refresh db cursor */
				refreshDbCursor();
				refreshDateView();

			}
		});

		/* Get date information */
		String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));

		dlg.setActiveDate(dateStr);

		/* Show dialog */
		dlg.show();
	}

	/*
	 * Change tag
	 */
	private void changeTagWithUser() {
		mTagDataProvider.setActiveReceiptId(mRID);

		/* Show tag selection dialog */
		final RRTagSelectDialog dlg = new RRTagSelectDialog(
				RRDetailExpenseActivity.this);
		dlg.initialize(mTagDataProvider);

		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (dlg.isCanceled())
					return;

				refreshTagInfoView();
			}

		});
		dlg.show();
	}

	/*
	 * Change amount with user
	 */
	private void changeAmountWithUser() {
		/* Get total amount of money */
		int packedTotal = mCursor.getInt(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));

		/* Show money input dialog */
		final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(this);
		inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
		inputDlg.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				/*
				 * Money input dialog is dismissed. Let's save if we should do
				 */
				if (inputDlg.isCanceled())
					return;

				/* Insert new total money to db */
				int rid = mCursor.getInt(0);
				mDbAdapter.updateExpenseAmount(rid, inputDlg.getDollars(),
						inputDlg.getCents());

				refreshDbCursor();
				refreshMoneyViewText();
			}
		});
		inputDlg.show();
	}

	/*
	 * Refresh budget view
	 */
	private void refreshBudgetView() {
		TextView budgetView = (TextView) findViewById(R.id.budget_view);

		int budgetCol = mCursor.getColumnIndex(RRDbAdapter.KEY_BUDGET_NAME);
		if (mCursor.isNull((int) budgetCol)) {
			budgetView.setText("");
			findViewById(R.id.budget_view_wrapper).setVisibility(View.GONE);
		} else {
			String budgetName = mCursor.getString(budgetCol);
			budgetView.setText(budgetName);
			findViewById(R.id.budget_view_wrapper).setVisibility(View.VISIBLE);
		}

		budgetView.requestLayout();
		budgetView.invalidate();
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
		moneyView.setText(RRUtil.formatMoney(total, true));
		moneyView.invalidate();
	}

//	private void initializeMoneyButton(final RRDetailExpenseActivity self) {
//		Button moneyButton = (Button) this.findViewById(R.id.button_numpad);
//		moneyButton.setOnClickListener(new Button.OnClickListener() {
//			/* Money input button is clicked */
//			public void onClick(View v) {
//				/* Get total amount of money */
//				int packedTotal = mCursor.getInt(mCursor
//						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
//
//				/* Show money input dialog */
//				final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(
//						RRDetailExpenseActivity.this);
//				inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
//				inputDlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//							public void onDismiss(DialogInterface dialog) {
//								/*
//								 * Money input dialog is dismissed. Let's save
//								 * if we should do
//								 */
//								if (inputDlg.isCanceled())
//									return;
//
//								/* Insert new total money to db */
//								int rid = mCursor.getInt(0);
//								mDbAdapter.updateTotalMoney(rid, inputDlg
//										.getDollars(), inputDlg.getCents());
//								/* Refresh db items */
//								self.refreshDbCursor();
//								self.refreshMoneyViewText();
//							}
//						});
//				inputDlg.show();
//			}
//		});
//	}

	private void showErrorMessage(String msg) {
		Log.e(TAG, msg);
		Toast.makeText(this, "Error happend:" + msg, Toast.LENGTH_LONG).show();
		return;
	}

	/*
	 * Refresh db cursor
	 */
	private void refreshDbCursor() {
		Cursor c = mCursor;
		if (c != null) {
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

//	private void initializeDatePickButton(final RRDetailExpenseActivity self) {
//		Button datePickBtn = (Button) findViewById(R.id.button_date_pick);
//		datePickBtn.setOnClickListener(new View.OnClickListener() {
//			/*
//			 * Date pick button is clicked. Show date pick dialog & change date
//			 */
//			public void onClick(View v) {
//				final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(
//						self);
//				dlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//							public void onDismiss(DialogInterface dialog) {
//								if (false == dlg.isDateSelected())
//									return;
//								/* Update db. */
//								mDbAdapter.updateDate(mCursor, dlg
//										.getSelectedDateInMillis());
//								/* Refresh db cursor */
//								self.refreshDbCursor();
//								self.refreshDateView();
//
//							}
//						});
//
//				/* Get date information */
//				String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
//						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
//
//				dlg.setActiveDate(dateStr);
//
//				/* Show dialog */
//				dlg.show();
//			}
//		});
//	}

//	/*
//	 * Initialize tag box and tag button
//	 */
//	private void initializeTagBoxAndTagButton(final RRDetailExpenseActivity self) {
//		/* Set tag data provider */
//		// final RRTagBox tagBox = (RRTagBox) self.findViewById(R.id.tag_box);
		
//		// tagBox.setTagProvider(mTagDataProvider);
//		// tagBox.setOnTagItemStateChangeListener(new
//		// RRTagStreamView.OnTagItemStateChangeListener() {
//		// /*
//		// * Let's update tag item status
//		// */
//		// public void onTagItemStateChanged(String tag, boolean checked) {
//		// RRDetailExpenseView.this.refreshTagInfoView();
//		//				
//		// }
//		// });
//
//		/* Initialize tag button */
//		Button tagButton = (Button) findViewById(R.id.button_tag);
//		tagButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// /* Show tag box */
//				// RRTagBox tagBox = (RRTagBox) self.findViewById(R.id.tag_box);
//				// tagBox.setVisibility(View.VISIBLE);
//
//				mTagDataProvider.setActiveReceiptId(mRID);
//
//				/* Show tag selection dialog */
//				final RRTagSelectDialog dlg = new RRTagSelectDialog(
//						RRDetailExpenseActivity.this);
//				dlg.initialize(mTagDataProvider);
//				dlg.show();
//			}
//		});
//	}

	/*
	 * Refresh tag info view
	 */
	private void refreshTagInfoView() {
		String tagStr = mDbAdapter.queryReceiptTagsAsMultiLineString(mRID);
		TextView tagInfoView = (TextView) findViewById(R.id.tag_info_view);
		tagInfoView.setText(tagStr);
	}
}
