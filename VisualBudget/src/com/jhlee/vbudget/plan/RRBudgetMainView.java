package com.jhlee.vbudget.plan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.SimpleTimeZone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.RRBudgetContent;

public class RRBudgetMainView extends LinearLayout implements RRBudgetContent {

	private static final int BASE_YEAR = 2009;
	private static final int BASE_MONTH = 1;

	public interface RRBudgetDataProvider {
		public void refreshData();

		public int getBudgetMonthCount();

		public long getBudgetAmount(int year, int month);

		public boolean getBudgetItem(int year, int month, int position,
				RRBudgetItemData budgetData);

		public int getBudgetItemCount(int year, int month);

		public boolean deleteBudgetItem(int year, int month, String budgetName);

		public int appendBudgetItem(int year, int month,
				RRBudgetItemData budgetData);

		public boolean updateBudgetItem(int year, int month,
				RRBudgetItemData budgetData);

		public void getDefaultBudgetNames(ArrayList<String> budgetNames);

		public boolean findBudgetItem(int year, int month, String budgetName);

		public boolean isBudgetItemUsed(int year, int month, String budgetName);
	};

	private RRBudgetDataProvider mProvider;
	private LinearLayout mLayout;
	private RRMonthBudgetView mMonthBudgetView;
	private Gallery mYearMonthGallery;

	public RRBudgetMainView(Context context) {
		this(context, null);
	}

	public RRBudgetMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLayout = (LinearLayout) createViewsFromLayout(R.layout.rr_budget_view,
				this);

		/* Initialize month budget view */
		mMonthBudgetView = (RRMonthBudgetView) mLayout
				.findViewById(R.id.budget_month);

		mYearMonthGallery = (Gallery) mLayout
				.findViewById(R.id.year_month_list);
		mYearMonthGallery
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long id) {
						/* Let's compute year/month from position */
						int year = BASE_YEAR + position / 12;
						int month = BASE_MONTH + position % 12;

						/* View is changed */
						mMonthBudgetView.setYearMonth(year, month);
						mMonthBudgetView.setBudgetDataProvider(mProvider);

						mMonthBudgetView.requestLayout();
						mMonthBudgetView.invalidate();
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});
	}

	/*
	 * Set data provider
	 */
	public void setBudgetDataProvider(RRBudgetDataProvider provider) {
		mProvider = provider;
		/* Set current year, month */
		Calendar cal = Calendar.getInstance();
		mMonthBudgetView.setYearMonth(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH));
		mYearMonthGallery.setAdapter(new RRYearMonthAdapter());
		mYearMonthGallery.setSelection(getCurrentYearMonthPosition());

		/* Request layout change */
		this.requestLayout();
	}

	private View createViewsFromLayout(int layoutId, ViewGroup parent) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater) getContext().getSystemService(infService);
		return li.inflate(layoutId, parent, true);
	}

	/*
	 * Refresh content data
	 */
	@Override
	public void refreshContent() {
		if (mProvider != null) {
			mProvider.refreshData();
			mMonthBudgetView.setBudgetDataProvider(mProvider);
		}

		/* Request layout change */
		this.requestLayout();
	}

	private class RRYearMonthAdapter extends BaseAdapter {

		private static final int BASE_YEAR = 2009;
		private static final int BASE_MONTH = 1;

		/*
		 * Month starts from 2009.01.01 to 20019.01.01
		 */
		public int getCount() {
			/* 10 years and 12 months */
			return 120;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		/*
		 * Let's generate budget month view at here
		 */
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView monthTextView = new TextView(RRBudgetMainView.this
					.getContext());
			monthTextView.setPadding(15, 15, 15, 15);

			DisplayMetrics dm = RRBudgetMainView.this.getResources()
					.getDisplayMetrics();
			float textSize = (float) (dm.scaledDensity * 32.00);
			monthTextView.setTextSize(textSize);

			int year = BASE_YEAR + position / 12;
			int month = BASE_MONTH + position % 12;
			String ymStr = Integer.toString(year) + "."
					+ Integer.toString(month);
			monthTextView.setText(ymStr);
			return monthTextView;
		}
	}

	private int yearMonthToPosition(int year, int month) {
		int pos = (year * 12 + month) - (BASE_YEAR * 12 + BASE_MONTH);
		if (pos < 0)
			return 0;
		return pos;
	}

	@Override
	public void createMenu(Menu menu, MenuInflater inflater) {
	}

	/*
	 * Menu item is selected
	 */
	@Override
	public void onMenuItemSelected(MenuItem mi) {

	}
	
	private int getCurrentYearMonthPosition() {
		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		return yearMonthToPosition(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH) + 1);
	}
}
