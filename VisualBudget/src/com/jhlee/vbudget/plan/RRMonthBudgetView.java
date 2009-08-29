package com.jhlee.vbudget.plan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.plan.RRBudgetItemView.OnDeleteButtonClickListener;
import com.jhlee.vbudget.plan.RRBudgetMainView.RRBudgetDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRMonthBudgetView extends LinearLayout {

	private static final int PADDING_HORZ = 10;

	private ListView mBudgetListView;
	private RRBudgetDataProvider mProvider;
	private RRMonthBudgetAdapter mAdapter;
	private int mYear;
	private int mMonth;
	private int mDesiredWidth;
	private boolean mUseDeleteButton;

	private RRBudgetItemData mTmpBudgetItemData = new RRBudgetItemData();
	private OnBudgetItemClickListener mBudgetItemClickListener;

	public RRMonthBudgetView(Context context) {
		this(context, null);
	}

	public RRMonthBudgetView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mUseDeleteButton = true;
		buildLayout();
	}

	private void buildLayout() {
		createViewsFromLayout(R.layout.plan_month_budget, this);
		mBudgetListView = (ListView) findViewById(R.id.budget_list);

		/* Item click listener */
		mBudgetListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						if (position == 0 || position == 1)
							return;

						final RRBudgetEditDialog dlg = new RRBudgetEditDialog(
								RRMonthBudgetView.this.getContext());
						dlg.initialize(mProvider);
						if (position == 2) {
							dlg
									.setOnDismissListener(new DialogInterface.OnDismissListener() {

										@Override
										public void onDismiss(
												DialogInterface dialog) {
											if (dlg.isCanceled())
												return;

											long budgetAmount = dlg
													.getBudgetAmount();
											String budgetName = dlg
													.getBudgetName();
											RRMonthBudgetView.this
													.addNewBudget(budgetName,
															budgetAmount);
										}
									});
							/* Add budget icon is clicked */
							dlg.show();
							return;
						}

						final int dataPosition = (position - 3);
						mProvider.getBudgetItem(mYear, mMonth, dataPosition,
								mTmpBudgetItemData);

						/* If budget item listener is clicked,
						 * then use it.
						 * 
						 */
						if (mBudgetItemClickListener != null) {
							mBudgetItemClickListener.onBudgetItemClicked(mYear,
									mMonth, mTmpBudgetItemData.mBudgetName,
									mTmpBudgetItemData.mBudgetAmount,
									mTmpBudgetItemData.mBudgetBalance);
							return;
						}
						dlg.editBudget(mTmpBudgetItemData.mBudgetName,
								mTmpBudgetItemData.mBudgetAmount);
						dlg
								.setOnDismissListener(new DialogInterface.OnDismissListener() {

									@Override
									public void onDismiss(DialogInterface dialog) {
										if (dlg.isCanceled())
											return;
										long budgetAmount = dlg
												.getBudgetAmount();
										String budgetName = dlg.getBudgetName();
										RRMonthBudgetView.this.updateBudget(
												budgetName, budgetAmount);
									}
								});
						dlg.show();
					}
				});
	}

	private View createViewsFromLayout(int layoutId, ViewGroup parent) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater) getContext().getSystemService(infService);
		return li.inflate(layoutId, parent, true);
	}

	/*
	 * Set year/month data
	 */
	public void setYearMonth(int year, int month) {
		mYear = year;
		mMonth = month;
	}

	/*
	 * Set data provider
	 */
	public void setBudgetDataProvider(RRBudgetDataProvider provider) {
		mProvider = provider;
		mAdapter = new RRMonthBudgetAdapter();
		mBudgetListView.setAdapter(mAdapter);
		requestLayout();
	}

	/*
	 * Add new budget
	 */
	private void addNewBudget(final String budgetName, final long budgetAmount) {
		/* Let's find whether the data is already in there */
		boolean found = mProvider.findBudgetItem(mYear, mMonth, budgetName);
		if (found == true) {
			/*
			 * Dup is found. Ask to user whether overwrite previous data or not.
			 */
			new AlertDialog.Builder(RRMonthBudgetView.this.getContext())
					.setTitle(
							"The budget item is already in program. Are you sure to overwrite previous budget data?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									updateBudget(budgetName, budgetAmount);
								}
							}).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create().show();
			return;
		}

		/*
		 * Add budget to db
		 */
		addBudgetToDb(budgetName, budgetAmount);
	}

	private void addBudgetToDb(String budgetName, long budgetAmount) {
		/* Add new budget to DB */
		mTmpBudgetItemData.mBudgetName = budgetName;
		mTmpBudgetItemData.mBudgetAmount = budgetAmount;
		mProvider.appendBudgetItem(mYear, mMonth, mTmpBudgetItemData);
		mProvider.refreshData();

		/* Add budget item to list */
		mAdapter.notifyDataSetChanged();
	}

	/*
	 * Update old budget
	 */
	private void updateBudget(String budgetName, long budgetAmount) {
		mTmpBudgetItemData.mBudgetName = budgetName;
		mTmpBudgetItemData.mBudgetAmount = budgetAmount;
		if (false == mProvider.updateBudgetItem(mYear, mMonth,
				mTmpBudgetItemData))
			return;

		mProvider.refreshData();

		/* Add budget item to list */
		mAdapter.notifyDataSetChanged();
	}

	/*
	 * Delete budget item
	 */
	private void deleteBudgetItem(String budgetName) {
		mProvider.deleteBudgetItem(mYear, mMonth, budgetName);

		mProvider.refreshData();
		mAdapter.notifyDataSetChanged();
	}

	/*
	 * Hide delete button
	 */
	public void hideDeleteButton() {
		mUseDeleteButton = false;
	}

	public void setOnBudgetItemClickListener(OnBudgetItemClickListener listener) {
		mBudgetItemClickListener = listener;
	}

	/*
	 * Basic adapter
	 */
	public class RRMonthBudgetAdapter extends BaseAdapter {
		public int getCount() {
			/*
			 * 0: Year/Month title 1: Total amount 2: New budget commands
			 */
			return mProvider.getBudgetItemCount(mYear, mMonth) + 3;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup viewGroup) {
			if (position == 0 || position == 1 || position == 2) {
				/* Return title */
				TextView textView = (TextView) createViewsFromLayout(
						R.layout.rr_month_budget_list_command, null);
				textView.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL);
				if (0 == position) {
					/* Get month/year */
					StringBuilder sb = new StringBuilder();
					sb.append(Integer.toString(mYear));
					sb.append(".");
					sb.append(Integer.toString(mMonth));
					textView.setText(sb.toString());
					return textView;
				}
				if (1 == position) {
					long totalMoney = mProvider.getBudgetAmount(mYear, mMonth);
					String moneyStr = RRUtil.formatMoney(totalMoney / 100,
							totalMoney % 100, true);
					textView.setText(moneyStr);
					return textView;
				}
				if (2 == position) {
					textView.setText("Click here to add budget");
					textView.setTextSize(20);
					textView.setPadding(0, 10, 0, 10);
					return textView;
				}
			}

			/*
			 * Exclude first 3 commands items Set budget name
			 */
			int dataPosition = position - 3;

			mProvider.getBudgetItem(mYear, mMonth, dataPosition,
					mTmpBudgetItemData);

			final String budgetName = new String(mTmpBudgetItemData.mBudgetName);

			RRBudgetItemView itemView = new RRBudgetItemView(
					RRMonthBudgetView.this.getContext());
			itemView.setBudgetItemData(mTmpBudgetItemData.mBudgetName,
					mTmpBudgetItemData.mBudgetAmount,
					mTmpBudgetItemData.mBudgetBalance);
			if (mUseDeleteButton) {
				itemView
						.setOnDeleteButtonClickListener(new OnDeleteButtonClickListener() {

							@Override
							public void onDeleteButtonClicked(View view) {
								RRMonthBudgetView.this
										.deleteBudgetItem(budgetName);
							}

						});
			} else {
				itemView.hideDeleteButton();
			}

			return itemView;
		}

	}

	/*
	 * Budget item click listener
	 */
	public interface OnBudgetItemClickListener {
		public void onBudgetItemClicked(int year, int month, String budgetName,
				long budgetAmount, long budgetBalance);

	}

}
