package com.jhlee.vbudget.collect;

import java.util.HashSet;
import java.util.Iterator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRMoneyInputDialog;
import com.jhlee.vbudget.tags.RRTagDataProviderFromDb;
import com.jhlee.vbudget.tags.RRTagSelectDialog;
import com.jhlee.vbudget.tags.RRTagsListView.RRTagDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRCollectView extends ScrollView {

	private static final String TAG = "RRCollectView";

	private View mFrame;
	private RRDbAdapter mDbAdapter;

	/*
	 * CTOR
	 */
	public RRCollectView(Context context) {
		this(context, null);
	}

	public RRCollectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRCollectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		/* Create view from layout */
		mFrame = (View) RRUtil.createViewsFromLayout(context,
				R.layout.collect_view, this);

		/* Camera button */
		Button cameraBtn = (Button) mFrame
				.findViewById(R.id.collect_view_camera);
		cameraBtn.setOnClickListener(new OnClickListener() {

			/* Camera button is clicked */
			@Override
			public void onClick(View v) {
				/* Launch capture activity */
				Context ctx = RRCollectView.this.getContext();
				Intent i = new Intent(ctx, RRTakeReceiptActivity.class);
				ctx.startActivity(i);
			}
		});

		/* Manual insert */
		Button addTrans = (Button) mFrame
				.findViewById(R.id.collect_view_add_trans);
		addTrans.setOnClickListener(new OnClickListener() {

			/* Transaction button is clicked */
			@Override
			public void onClick(View v) {
				final RRTransactionEditDialog dlg = new RRTransactionEditDialog(RRCollectView.this.getContext());
				dlg.initialize(mDbAdapter);
				
				dlg
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						/* Dialog is closed */
						if (dlg.isCanceled())
							return;

						long transId = mDbAdapter.newTransaction();
						if (-1 == transId) {
							Log.e(TAG, "Unable to create new transaction");
							return;
						}
						
						/* Read-only tag list */
						Iterator<String> it = dlg.getTaglistIterator();
						while(it.hasNext()) {
							mDbAdapter.addTagToReceipt(transId, it.next());
						}
						long money = dlg.getExpenseAmount();
						mDbAdapter.updateTotalMoney(transId, (int)money/100, (int)money%100);
						
						/* Apply budget field */
						long budgetId = dlg.getSelectedBudgetId();
						if(-1 == budgetId)
							return;
						
						mDbAdapter.makeTransactionFromBudget(
								transId,
								money,
								budgetId);
					}
				});
				
				
				dlg.show();
//				final RRTagSelectDialog dlg = new RRTagSelectDialog(
//						RRCollectView.this.getContext());
//				final RRTransactionTagDataProvider transTag = new RRTransactionTagDataProvider();
//
//				dlg.initialize(transTag);
//				dlg
//						.setOnDismissListener(new DialogInterface.OnDismissListener() {
//							@Override
//							public void onDismiss(DialogInterface dialog) {
//								/* Dialog is closed */
//								if (dlg.isCanceled())
//									return;
//
//								if (false == transTag.hasCheckedTags())
//									return;
//
//								long transId = RRCollectView.this
//										.addTransaction(transTag);
//								if (-1 != transId) {
//									RRCollectView.this
//											.updateAmountFromUser(transId);
//								}
//							}
//						});
//
//				dlg.show();
			}
		});
	}

	/*
	 * Initialize collect view
	 */
	public void initialize(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;
	}

	/*
	 * Transaction
	 */
	public long addTransaction(RRTransactionTagDataProvider transTags) {
		long id = mDbAdapter.newTransaction();
		if (-1 == id) {
			Log.e(TAG, "Unable to create new transaction");
			return -1;
		}

		/* Set tags */
		Iterator<String> it = transTags.getCheckedTagsIterator();
		while (it.hasNext()) {
			mDbAdapter.addTagToReceipt(id, it.next());
		}

		return id;
	}

	/*
	 * Get transaction amount from user
	 */
	public void updateAmountFromUser(final long transId) {
		final RRMoneyInputDialog dlg = new RRMoneyInputDialog(this.getContext());
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if(dlg.isCanceled())
					return;
				
				int dollars = dlg.getDollars();
				int cents = dlg.getCents();
				mDbAdapter.updateTotalMoney(transId, dollars, cents);
			}
		});
		dlg.show();
	}

	/*
	 * Transaction tag data
	 */
	private class RRTransactionTagDataProvider implements RRTagDataProvider {
		private HashSet<String> mCheckedTags = new HashSet<String>();
		private RRTagDataProviderFromDb mTagDataProvider = new RRTagDataProviderFromDb(
				mDbAdapter);

		@Override
		public boolean addTag(String tag, boolean checked) {
			boolean result = mTagDataProvider.addTag(tag, checked);
			if (result == true && checked == true) {
				/* Let's keep checked item */
				mCheckedTags.add(tag);
			}
			return result;
		}

		@Override
		public void check(int index) {
			String tag = mTagDataProvider.getTag(index);
			mCheckedTags.add(tag);
		}

		@Override
		public int findTag(String tagName) {
			return mTagDataProvider.findTag(tagName);
		}

		@Override
		public int getCount() {
			return mTagDataProvider.getCount();
		}

		@Override
		public String getTag(int index) {
			return mTagDataProvider.getTag(index);
		}

		@Override
		public boolean isChecked(int index) {
			String tag = getTag(index);
			return mCheckedTags.contains(tag);
		}

		@Override
		public void uncheck(int index) {
			String tag = getTag(index);
			mCheckedTags.remove(tag);
		}

		public boolean hasCheckedTags() {
			return mCheckedTags.isEmpty() == false;
		}

		public Iterator<String> getCheckedTagsIterator() {
			return mCheckedTags.iterator();
		}
	};
}
