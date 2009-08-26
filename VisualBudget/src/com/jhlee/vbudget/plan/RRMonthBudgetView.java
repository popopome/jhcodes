package com.jhlee.vbudget.plan;

import android.content.Context;
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

import com.jhlee.rr.RRBudgetView.RRBudgetDataProvider;

public class RRMonthBudgetView extends LinearLayout {

	private static final int PADDING_HORZ = 10;

	private ListView mBudgetListView;
	private RRBudgetDataProvider mProvider;
	private int mYear;
	private int mMonth;
	private int mDesiredWidth;

	private RRBudgetItemData mTmpBudgetItemData = new RRBudgetItemData();

	public RRMonthBudgetView(Context context) {
		this(context, null);
	}

	public RRMonthBudgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildLayout();
	}

	private void buildLayout() {
		createViewsFromLayout(R.layout.rr_month_budget, this);
		mBudgetListView = (ListView) findViewById(R.id.budget_list);

		/* Item click listener */
		mBudgetListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						if (position == 0 || position == 1)
							return;

						RRBudgetInputDialog dlg = new RRBudgetInputDialog(
								RRMonthBudgetView.this.getContext());
						if (position == 2) {
							/* Add budget icon is clicked */
							dlg.show();
							return;
						}

						dlg.show();

						int dataPosition = (position - 3);
						mProvider.getBudgetItem(mYear, mMonth, dataPosition,
								mTmpBudgetItemData);
						dlg.editBudget(mTmpBudgetItemData.mBudgetName,
								mTmpBudgetItemData.mBudgetAmount);
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
		RRMonthBudgetAdapter adapter = new RRMonthBudgetAdapter();
		mBudgetListView.setAdapter(adapter);
		requestLayout();
	}

	/*
	 * Basic adapter
	 */
	public class RRMonthBudgetAdapter extends BaseAdapter {
		public int getCount() {
			/*
			 * 0: Year/Month title 1: Total amount 2: New budget commands
			 */
			return mProvider.getBudgetCount() + 3;
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
					String moneyStr = "$" + Long.toString(totalMoney / 100)
							+ Long.toString(totalMoney % 100);
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

			/* Generate view for budget */
			View itemView = createViewsFromLayout(
					R.layout.rr_month_budget_list_item, null);
			TextView textView = (TextView) itemView
					.findViewById(R.id.budget_item_name);
			TextView budgetAmountView = (TextView) itemView
					.findViewById(R.id.budget_item_amount);

			/*
			 * Exclude first 3 commands items Set budget name
			 */
			int dataPosition = position - 3;

			mProvider.getBudgetItem(mYear, mMonth, dataPosition,
					mTmpBudgetItemData);
			textView.setText(mTmpBudgetItemData.mBudgetName);
			textView.setFocusable(false);
			textView.setClickable(false);
			textView.setLongClickable(false);

			/* Set budget amount */
			String budgetAmountString = Long
					.toString(mTmpBudgetItemData.mBudgetAmount);
			budgetAmountView.setText(budgetAmountString);

			return itemView;
		}

	}

}
