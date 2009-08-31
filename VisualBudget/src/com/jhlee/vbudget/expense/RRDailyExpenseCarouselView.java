package com.jhlee.vbudget.expense;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.RRBudgetContent;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselActiveItemChanged;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselActiveItemClickListener;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselItemCustomDrawListener;
import com.jhlee.vbudget.expense.RRCarouselFlowView.OnCarouselScrollEventListener;
import com.jhlee.vbudget.expense.RRCarouselFlowView.RRCarouselItem;
import com.jhlee.vbudget.plan.RRBudgetSelectDialog;
import com.jhlee.vbudget.tags.RRTagDataProviderFromDb;
import com.jhlee.vbudget.tags.RRTagSelectDialog;
import com.jhlee.vbudget.util.RRUtil;

public class RRDailyExpenseCarouselView extends FrameLayout implements
		OnCarouselActiveItemClickListener, OnCarouselActiveItemChanged,
		OnCarouselScrollEventListener, RRBudgetContent {
	private static final String TAG = "rrdailyExpenseCarouselView";
	private static final String POSTFIX_REFLECTION_BMP = "@#$";

	private int COL_INDEX_SMALL_IMAGE_FILE_PATH = -1;
	private int COL_INDEX_RECEIPT_TOTAL = -1;
	private int COL_INDEX_RECEIPT_TAKEN_DATE = -1;
	private int COL_INDEX_BUDGET_NAME = -1;

	private FrameLayout mFrameView;
	private RRCarouselFlowView mCarouselView;

	private View mExpenseWrapperViewGroup;
	private View mEmptyDataView;
	private Button mDeleteButton;

	private ImageButton mBudgetButton;

	private RRDbAdapter mAdapter;
	private Cursor mCursor;

	private ItemBitmapContainer mBmpContainer = new ItemBitmapContainer();

	/*
	 * Content host
	 */

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
				R.layout.expense_carousel, this);
		mCarouselView = (RRCarouselFlowView) mFrameView
				.findViewById(R.id.carouselView);
		mExpenseWrapperViewGroup = mFrameView
				.findViewById(R.id.expense_wrapper);
		mEmptyDataView = mFrameView.findViewById(R.id.expense_empty);
//		mBudgetButton = (ImageButton) mFrameView
//				.findViewById(R.id.button_budget);
		mDeleteButton = (Button) mFrameView.findViewById(R.id.button_delete);

		// /* Install back button listener */
		// Button backButton = (Button)
		// mFrameView.findViewById(R.id.back_button);
		// backButton.setOnClickListener(new Button.OnClickListener() {
		// public void onClick(View v) {
		// /* Finish activity */
		// /* TODO: Remove this button */
		// }
		// });

		/* Collect receipt data from database */
		mAdapter = dbAdapter;

		/* Initialize carousel view */
		final RRCarouselFlowView carouselView = (RRCarouselFlowView) findViewById(R.id.carouselView);
		carouselView.setFocusable(true);
		carouselView.setOnActiveItemClickListener(this);
		carouselView.setOnActiveItemChangeListener(this);
		carouselView.setOnCarouselScrollEventListener(this);

		/* Set custom drawer */
		carouselView
				.setOnCarouselItemCustomDrawListener(new ReceiptItemCustomDrawer());

//		/* Install money input dialog listener */
//		initializeMoneyButtonHandler(carouselView);
//
//		/* Install date change button */
//		initializeDateChangeButton(carouselView);
//
//		/* Initialize tag box and tag button */
//		initializeTagBoxAndTagButton();
//		
		
		mTagDataProvider = new RRTagDataProviderFromDb(mAdapter);
		

//		/*
//		 * Budget button is clicked
//		 */
//		initializeBudgetButton();

		/* Give default focus to carousel view */
		carouselView.requestFocus();
		
		/*
		 * Initialize delete button
		 */
		mDeleteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				long id = getActiveReceiptId();
				if(id == -1)
					return;
				
				/* Ask to user
				 * Are you sure to delete the item?
				 */
				new AlertDialog.Builder(RRDailyExpenseCarouselView.this.getContext())
                .setTitle("Are you sure to delete the expense?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked OK so do some stuff */
                    	RRDailyExpenseCarouselView.this.deleteActiveItem();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create().show();
			}
			
		});
		
		/*
		 * Update x button position
		 */
		updateXButtonPos();

		/* Let's check there is data or not. */
		return showViewByDataExistence();
	}
	
	/*
	 * Delete active item
	 */
	private void deleteActiveItem() {
		long id = getActiveReceiptId();
		RRCarouselItem item = mCarouselView.getActiveItem();
		int oldSeq = item.seq;
		
		/* Delete data from Db */
		if(false == mAdapter.deleteExpense(id)) {
			Log.e(TAG, "Unable to delete receipt");
			return;
		}
		
		/*
		 * Refresh content
		 */
		refreshContent();
		
		int cnt = mCursor.getCount();
		if(cnt <= oldSeq) {
			oldSeq = cnt - 1;
		}
		
		if(oldSeq >= 0)
			mCarouselView.setActiveItem(oldSeq);
		
		/*
		 * Check data existence
		 */
		showViewByDataExistence();
		
		invalidate();
	}

//	private void initializeBudgetButton() {
//		mBudgetButton.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				/* Show budet dialog */
//				final RRBudgetSelectDialog budgetDlg = new RRBudgetSelectDialog(
//						RRDailyExpenseCarouselView.this.getContext());
//				budgetDlg.initialize(mAdapter);
//
//				budgetDlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//							@Override
//							public void onDismiss(DialogInterface dialog) {
//								if (budgetDlg.isCanceled())
//									return;
//
//								String budgetName = budgetDlg
//										.getSelectedBudgetName();
//								int budgetYear = budgetDlg
//										.getSelectedBudgetYear();
//								int budgetMonth = budgetDlg
//										.getSelectedBudgetMonth();
//								long budgetBalance = budgetDlg
//										.getSelectedBudgetBalance();
//								applyBudget(budgetYear, budgetMonth,
//										budgetName, budgetBalance);
//
//							}
//
//						});
//
//				budgetDlg.show();
//			}
//
//		});
//	}

	/*
	 * Apply budget
	 */
	private void applyBudget(int year, int month, String budgetName,
			long budgetBalance) {
		long transId = getActiveReceiptId();
		mAdapter.changeBudget(transId, year, month, budgetName);

		/* Requery data */
		requery();

		/* Invalidate screen */
		invalidate();
	}

	/*
	 * Show view by data existence
	 */
	private boolean showViewByDataExistence() {
		Cursor c = mAdapter.queryAllReceipts();
		int numTrans = c.getCount();
		if (numTrans < 1) {
			/*
			 * No expense data. We return false.
			 */
			mExpenseWrapperViewGroup.setVisibility(View.GONE);
			mEmptyDataView.setVisibility(View.VISIBLE);
			mDeleteButton.setVisibility(View.GONE);
			return false;
		}
		mExpenseWrapperViewGroup.setVisibility(View.VISIBLE);
		mEmptyDataView.setVisibility(View.GONE);
		mDeleteButton.setVisibility(View.VISIBLE);

		return true;
	}

	/*
	 * Initialize tag box and tag button
	 */
//	private void initializeTagBoxAndTagButton() {
//		/* Set tag data provider */
//		/*
//		 * final RRTagBox tagBox = (RRTagBox)
//		 * mFrameView.findViewById(R.id.tag_box); mTagDataProvider = new
//		 * RRTagDataProviderFromDb(mAdapter);
//		 * tagBox.setTagProvider(mTagDataProvider);
//		 */
//		mTagDataProvider = new RRTagDataProviderFromDb(mAdapter);
//
//		/* Initialize tag button */
//		Button tagButton = (Button) findViewById(R.id.button_tag);
//		tagButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				/* Set active receipt id */
//				mTagDataProvider.setActiveReceiptId(getActiveReceiptId());
//
//				/* Show tag selection dialog */
//				final RRTagSelectDialog dlg = new RRTagSelectDialog(
//						RRDailyExpenseCarouselView.this.getContext());
//				dlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//							/* Dialog dismissed */
//							@Override
//							public void onDismiss(DialogInterface dialog) {
//								if (dlg.isCanceled())
//									return;
//							}
//
//						});
//				dlg.initialize(mTagDataProvider);
//				dlg.show();
//			}
//		});
//	}

//	private void initializeDateChangeButton(
//			final RRCarouselFlowView carouselView) {
//		Button datePickBtn = (Button) findViewById(R.id.button_date_pick);
//		datePickBtn.setOnClickListener(new View.OnClickListener() {
//			/*
//			 * Date pick button is clicked. Show date pick dialog & change date
//			 */
//			public void onClick(View v) {
//				final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(
//						RRDailyExpenseCarouselView.this.getContext());
//				dlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//							public void onDismiss(DialogInterface dialog) {
//								if (false == dlg.isDateSelected())
//									return;
//								/* Date is selected */
//								RRCarouselItem item = carouselView
//										.getActiveItem();
//								mCursor.moveToPosition(item.seq);
//								/* Assume 0th is id */
//								int id = mCursor.getInt(0);
//								/* Update db. */
//								mAdapter.updateDate(mCursor, dlg
//										.getSelectedDateInMillis());
//								/* Refresh db cursor */
//								RRDailyExpenseCarouselView.this
//										.refreshContent();
//
//								/*
//								 * Let's keep current view position. To do that
//								 * first we find new sequence of the item and
//								 * then move to that sequence.
//								 */
//								int newSeq = 0;
//								mCursor.moveToFirst();
//								if (mCursor.getCount() > 0) {
//
//									while (mCursor.isAfterLast() == false) {
//										if (mCursor.getInt(0) == id)
//											break;
//										mCursor.moveToNext();
//										++newSeq;
//									}
//								}
//								carouselView.setActiveItem(newSeq);
//
//								/* Invalidate view */
//								carouselView.invalidate();
//
//							}
//						});
//
//				/* Get date information */
//				mCursor.moveToPosition(carouselView.getActiveItem().seq);
//				String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
//						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
//				dlg.setActiveDate(dateStr);
//
//				/* Show dialog */
//				dlg.show();
//			}
//		});
//	}

//	private void initializeMoneyButtonHandler(
//			final RRCarouselFlowView carouselView) {
//		Button moneyButton = (Button) this.findViewById(R.id.button_numpad);
//		moneyButton.setOnClickListener(new Button.OnClickListener() {
//			/* Money input button is clicked */
//			public void onClick(View v) {
//
//				final RRCarouselItem item = carouselView.getActiveItem();
//				/* Move cursor position */
//				mCursor.moveToPosition(item.seq);
//				/* Get total amount of money */
//				int packedTotal = mCursor.getInt(mCursor
//						.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));
//
//				/* Show money input dialog */
//				final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(
//						RRDailyExpenseCarouselView.this.getContext());
//				inputDlg.setMoney(packedTotal / 100, packedTotal % 100);
//				inputDlg.setOnDismissListener(new OnDismissListener() {
//					public void onDismiss(DialogInterface dialog) {
//						/*
//						 * Money input dialog is dismissed. Let's save if we
//						 * should do
//						 */
//						if (inputDlg.isCanceled())
//							return;
//
//						/* Insert new total money to db */
//						mCursor.moveToPosition(item.seq);
//						int rid = mCursor.getInt(0);
//						mAdapter.updateTotalMoney(rid, inputDlg.getDollars(),
//								inputDlg.getCents());
//						/* Refresh db items */
//						RRDailyExpenseCarouselView.this.refreshContent();
//						/* Invalidate screen */
//						carouselView.invalidate();
//					}
//				});
//				inputDlg.show();
//			}
//		});
//	}

	/*
	 * Refresh content
	 */
	@Override
	public void refreshContent() {
		requery();
		int numOfReceipts = mCursor.getCount();
		if (numOfReceipts <= 0) {
			/* TODO: Show receipt data first */
			Log.e(TAG, "NO expense data-----------------!");
		}
		/* Initialize carousel view */
		final RRCarouselFlowView carouselView = (RRCarouselFlowView) findViewById(R.id.carouselView);
		carouselView.initialize(numOfReceipts, 120, 160, 60, 9, 25);

		/*
		 * Show view by data data existence
		 */
		showViewByDataExistence();
	}

	private void requery() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		mCursor = mAdapter.queryAllReceiptsWithBudgetName();
		updateColumnIndexes(mCursor);
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
	public void onCarouselItemClicked(RRCarouselFlowView view, RRCarouselItem item) {
		mCursor.moveToPosition(item.seq);
		long rid = mCursor.getInt(0);

		/** See receipt list */
		
		Intent i = new Intent(this.getContext(), RRDetailExpenseActivity.class);
		i.putExtra(RRDetailExpenseActivity.RECEIPT_ID, rid);
		this.getContext().startActivity(i);
		 
		// if(mHost != null) {
		// mHost.showMoneyContent(VisualBudget.RR_CMD_DETAIL_EXPENSE, rid,
		// null);
		// }
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
	
	

	@Override
	public void createMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_detail_view, menu);
	}
	
	
	/*
	 * Menu item is selected
	 */
	@Override
	public void onMenuItemSelected(MenuItem mi) {

		switch(mi.getItemId()) {
		case R.id.menu_budget:
			changeBudgetWithUser();
			break;
		case R.id.menu_amount:
			changeMoneyWithUser();
			break;
		case R.id.menu_date:
			changeDateWithUser();
			break;
		case R.id.menu_tag:
			changeTagWithUser();
			break;
		}
	}
	
	private void changeTagWithUser() {
		/* Set active receipt id */
		mTagDataProvider.setActiveReceiptId(getActiveReceiptId());

		/* Show tag selection dialog */
		final RRTagSelectDialog dlg = new RRTagSelectDialog(
				RRDailyExpenseCarouselView.this.getContext());
		dlg
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					/* Dialog dismissed */
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.isCanceled())
							return;
					}

				});
		dlg.initialize(mTagDataProvider);
		dlg.show();
	}
	
	/*
	 * Date pick button is clicked. Show date pick dialog & change date
	 */
	private void changeDateWithUser() {
		final RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(
				RRDailyExpenseCarouselView.this.getContext());
		dlg
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if (false == dlg.isDateSelected())
							return;
						/* Date is selected */
						RRCarouselItem item = mCarouselView
								.getActiveItem();
						mCursor.moveToPosition(item.seq);
						/* Assume 0th is id */
						int id = mCursor.getInt(0);
						/* Update db. */
						mAdapter.updateDate(mCursor, dlg
								.getSelectedDateInMillis());
						/* Refresh db cursor */
						RRDailyExpenseCarouselView.this
								.refreshContent();

						/*
						 * Let's keep current view position. To do that
						 * first we find new sequence of the item and
						 * then move to that sequence.
						 */
						int newSeq = 0;
						mCursor.moveToFirst();
						if (mCursor.getCount() > 0) {

							while (mCursor.isAfterLast() == false) {
								if (mCursor.getInt(0) == id)
									break;
								mCursor.moveToNext();
								++newSeq;
							}
						}
						mCarouselView.setActiveItem(newSeq);

						/* Invalidate view */
						mCarouselView.invalidate();

					}
				});

		/* Get date information */
		mCursor.moveToPosition(mCarouselView.getActiveItem().seq);
		String dateStr = RRUtil.formatCalendar(mCursor.getLong(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE)));
		dlg.setActiveDate(dateStr);

		/* Show dialog */
		dlg.show();
	}
	
	/* Money input button is clicked */
	private void changeMoneyWithUser() {
		final RRCarouselFlowView carouselView = (RRCarouselFlowView) this.findViewById(R.id.carouselView);
		final RRCarouselItem item = carouselView.getActiveItem();
		/* Move cursor position */
		mCursor.moveToPosition(item.seq);
		/* Get total amount of money */
		int packedTotal = mCursor.getInt(mCursor
				.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL));

		/* Show money input dialog */
		final RRMoneyInputDialog inputDlg = new RRMoneyInputDialog(
				RRDailyExpenseCarouselView.this.getContext());
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
				mAdapter.updateExpenseAmount(rid, inputDlg.getDollars(),
						inputDlg.getCents());
				/* Refresh db items */
				RRDailyExpenseCarouselView.this.refreshContent();
				/* Invalidate screen */
				carouselView.invalidate();
			}
		});
		inputDlg.show();
	}
	private void changeBudgetWithUser() {
		/* Show budet dialog */
		final RRBudgetSelectDialog budgetDlg = new RRBudgetSelectDialog(
				RRDailyExpenseCarouselView.this.getContext());
		budgetDlg.initialize(mAdapter);

		budgetDlg
				.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						if (budgetDlg.isCanceled())
							return;

						String budgetName = budgetDlg
								.getSelectedBudgetName();
						int budgetYear = budgetDlg
								.getSelectedBudgetYear();
						int budgetMonth = budgetDlg
								.getSelectedBudgetMonth();
						long budgetBalance = budgetDlg
								.getSelectedBudgetBalance();
						applyBudget(budgetYear, budgetMonth,
								budgetName, budgetBalance);

					}

				});

		budgetDlg.show();
	}

	/*
	 * Scroll started
	 */
	@Override
	public void onScrollStarted() {
		if (mDeleteButton != null) {
			mDeleteButton.setVisibility(View.GONE);
		}
	}

	/*
	 * Update column indexes
	 */
	private void updateColumnIndexes(Cursor c) {
		if (COL_INDEX_SMALL_IMAGE_FILE_PATH == -1) {
			COL_INDEX_SMALL_IMAGE_FILE_PATH = c
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_SMALL_IMG_FILE);
			COL_INDEX_RECEIPT_TOTAL = c
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TOTAL);
			COL_INDEX_RECEIPT_TAKEN_DATE = c
					.getColumnIndex(RRDbAdapter.KEY_RECEIPT_TAKEN_DATE);
			COL_INDEX_BUDGET_NAME = c
					.getColumnIndex(RRDbAdapter.KEY_BUDGET_NAME);
		}
	}


	/*
	 * Active item is changed
	 */
	public void onActiveItemChanged(RRCarouselFlowView view, RRCarouselItem item) {
		// /*
		// * If tag view is opened, let's refresh tag view
		// */
		// RRTagBox tagBox = (RRTagBox) findViewById(R.id.tag_box);
		// if (tagBox.getVisibility() == View.VISIBLE) {
		// String activeTag = tagBox.getActiveTag();
		//
		// /* Set active tag */
		// mTagDataProvider.setActiveReceiptId(getActiveReceiptId());
		// tagBox.refreshTags();
		//
		// tagBox.scrollToTag(activeTag);
		// }

		/*
		 * If item is active one, then we move delete button position to
		 * right-top of image.
		 */
		if (mDeleteButton != null) {

			mDeleteButton.setVisibility(View.VISIBLE);

		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		updateXButtonPos();
	}

	/*
	 * Update x button pos
	 */
	private void updateXButtonPos() {
		final RRCarouselItem item = mCarouselView.getActiveItem();
		if (null == item)
			return;

		getItemBitmaps(mBmpContainer, item, mCursor);

		int x = item.x - (item.w >> 1);
		int y = item.y - item.h * 2 / 3;

		Matrix mat = new Matrix();
		/* Compute zoom to fit matrix */
		zoomToFit(x, y, item.w, item.h, mBmpContainer.mBmp, mat);

		RectF rect = new RectF(0, 0, mBmpContainer.mBmp.getWidth(),
				mBmpContainer.mBmp.getHeight());
		mat.mapRect(rect);

		/* Move position to button next */
		// int buttonX = item.x + (item.w >> 1);
		// int buttonY = item.y - (item.h * 2 / 3);
		int btnW = mDeleteButton.getWidth();
		int btnH = mDeleteButton.getHeight();

		int buttonX = (int) rect.right - btnW / 2;
		int buttonY = (int) rect.top - btnH / 2;
		mDeleteButton.layout(buttonX, buttonY, buttonX + btnW, buttonY + btnH);

	}

	private class ItemBitmapContainer {
		Bitmap mBmp;
		Bitmap mReflectedBmp;
	}

	private void getItemBitmaps(ItemBitmapContainer bmpContainer,
			RRCarouselItem item, Cursor c) {
		/*
		 * If image file name was not fetched from DB, get it.
		 */
		c.moveToPosition(item.seq);
		String imgFileName = c.getString(COL_INDEX_SMALL_IMAGE_FILE_PATH);

		/* Look up bitmap image from bitmap pool */

		if (true == mBmpPool.containsKey(imgFileName)) {
			/* We found already loaded bitmap */
			bmpContainer.mBmp = mBmpPool.get(imgFileName);
			bmpContainer.mReflectedBmp = mBmpPool.get(imgFileName
					+ POSTFIX_REFLECTION_BMP);
		} else {
			/* Load bitmap from file */
			if (0 == imgFileName.compareTo("NTS")) {
				/* Use default small image */
				bmpContainer.mBmp = BitmapFactory.decodeResource(
						RRDailyExpenseCarouselView.this.getResources(),
						R.drawable.new_trans_small);
			} else {
				bmpContainer.mBmp = BitmapFactory.decodeFile(imgFileName);
			}

			mBmpPool.put(imgFileName, bmpContainer.mBmp);
			/* Generate reflected bitmap */
			bmpContainer.mReflectedBmp = this
					.createReflectedBitmap(bmpContainer.mBmp);
			mBmpPool.put(imgFileName + POSTFIX_REFLECTION_BMP,
					bmpContainer.mReflectedBmp);
		}
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

	/**
	 * Custom drawer
	 * 
	 * @author Administrator
	 * 
	 */
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

			Cursor c = mCursor;
			getItemBitmaps(mBmpContainer, item, c);

			int x = item.x - (item.w >> 1);
			int y = item.y - item.h * 2 / 3;

			/* Compute zoom to fit matrix */
			zoomToFit(x, y, item.w, item.h, mBmpContainer.mBmp,
					mMatrixZoomToFit);
			canvas.drawBitmap(mBmpContainer.mBmp, mMatrixZoomToFit, mPaint);

			/* Draw reflection image */
			mMatrixZoomToFit.postTranslate(0, item.h);
			canvas.drawBitmap(mBmpContainer.mReflectedBmp, mMatrixZoomToFit,
					mPaint);

			/*
			 * Draw money & date
			 */
			drawExpenseInformation(canvas, item, isItemActive, y);

		}

		
		/*
		 * Draw receipt information
		 */
		private void drawExpenseInformation(Canvas canvas, RRCarouselItem item,
				boolean isItemActive, int y) {

			Cursor c = mCursor;
			Paint p = mPaint;

			/* Draw money */
			installShadow();
			c.moveToPosition(item.seq);
			long total = c.getLong(COL_INDEX_RECEIPT_TOTAL);
			p.setColor(Color.WHITE);
			p.setTextAlign(Paint.Align.CENTER);

			String moneyString = RRUtil.formatMoney(total, true);
			canvas.drawText(moneyString, item.x, y + item.h + TEXT_SIZE, p);

			/* Only active item represents date */
			if (isItemActive) {
				int sy = y + item.h + TEXT_SIZE + TEXT_SIZE;
				/* Draw date */
				/* Format date */
				String dateStr = RRUtil.formatCalendar(c
						.getLong(COL_INDEX_RECEIPT_TAKEN_DATE));
				canvas.drawText(dateStr, item.x, sy, p);

				/* Draw budget information */
				if (false == c.isNull(COL_INDEX_BUDGET_NAME)) {
					String budgetName = c.getString(COL_INDEX_BUDGET_NAME);
					sy += TEXT_SIZE;
					canvas.drawText(budgetName, item.x, sy, mPaint);
				}

				sy += TEXT_SIZE;

				/* Draw tag information */
				int rid = c.getInt(0);
				String allTags = mAdapter.queryReceiptTagsAsOneString(rid);
				if (allTags.length() > 0) {
					/* There is tags */
					canvas.drawText(allTags, item.x, sy, p);
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

	}
}
