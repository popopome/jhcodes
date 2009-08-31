package com.jhlee.vbudget.statistics;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jhlee.vbudget.RRBudgetContent;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.statistics.RRChartBarStreamView.RRChartBarDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRStatisticsView extends FrameLayout implements RRBudgetContent {

	private RRDbAdapter mDbAdapter;

	public RRStatisticsView(Context context) {
		this(context, null);
	}

	public RRStatisticsView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRStatisticsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setUp(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;

		/* Create day-by-day expense graph */
		RRChartBarGraph graph = (RRChartBarGraph) createDayByDayExpenseGraph();
		this.addView(graph, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
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


	/**
	 * Create day-by-day expense graph
	 * 
	 * @return
	 */
	private View createDayByDayExpenseGraph() {
		RRChartBarGraph graph = new RRChartBarGraph(this.getContext());
		graph.setChartBarDataProvider(new RRDayByDayExpenseDataProvider());
		graph.setBarWidth(45);
		graph.setBarValueNameTextSize(9);
		graph.setTitleTextSize(20);
		graph.setXYAxisName("Date", "Money");
		graph
				.setGraphTitle("Day by day expense\nThe graph shows how you spend out money for each day");
		graph.setEmptyText("Day by day expense - N/A");
		graph.setBarMaxHeight(100);
		return graph;
	}

	/**
	 * Create day-by-day expense graph
	 * 
	 * @return
	 */
	private View createDayOfWeekExpenseGraph() {
		RRChartBarGraph graph = new RRChartBarGraph(this.getContext());
		graph.setChartBarDataProvider(new RRDayOfWeekExpenseDataProvider());
		graph.setBarWidth(45);
		graph.setBarValueNameTextSize(9);
		graph.setTitleTextSize(20);
		graph.setXYAxisName("DayOfWeek", "Money");
		graph
				.setGraphTitle("Day of week expense\nWhich day do you usually spend money?");
		graph.setEmptyText("Day of week expense - N/A");
		graph.setBarMaxHeight(100);
		return graph;
	}

	/*
	 * Refresh content
	 */
	@Override
	public void refreshContent() {
		RRChartBarGraph graph = (RRChartBarGraph) this.getChildAt(0);
		graph.refreshData();

		/* Request layout change */
		graph.requestLayout();
	}

	public class RRDayOfWeekExpenseDataProvider implements
			RRChartBarDataProvider {
		private static final int COL_DAY_OF_WEEK = 0;
		private static final int COL_TOTAL = 1;
		private Cursor mCursor;

		public RRDayOfWeekExpenseDataProvider() {
			refreshData();

		}

		@Override
		public void refreshData() {
			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			mCursor = mDbAdapter.queryExpenseDayOfWeek();
		}

		public long getBarMaxValue() {
			mCursor.moveToFirst();
			long maxValue = 0;
			while (false == mCursor.isAfterLast()) {
				maxValue = Math.max(maxValue, mCursor.getLong(COL_TOTAL));
				mCursor.moveToNext();
			}
			return maxValue;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return RRUtil.formatMoney(expense, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return (long) expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			switch (mCursor.getInt(COL_DAY_OF_WEEK)) {
			case 0:
				return "SUN";
			case 1:
				return "MON";
			case 2:
				return "TUE";
			case 3:
				return "WED";
			case 4:
				return "THU";
			case 5:
				return "FRI";
			case 6:
				return "SAT";
			}
			return "N/A";
		}

		public int getCount() {
			return mCursor.getCount();
		}
	}

	public class RRDayByDayExpenseDataProvider implements
			RRChartBarDataProvider {
		private static final int COL_DATE = 0;
		private static final int COL_EXPENSE = 1;
		/* Minimum expense $10 */
		private static final long MINIMUM_MAX_EXPENSE = 1000;
		private long mMaxExpense;
		private Cursor mCursor;

		public RRDayByDayExpenseDataProvider() {
			refreshData();
		}

		public long getBarMaxValue() {
			return mMaxExpense;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return RRUtil.formatMoney(expense, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return (int) expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			return RRUtil.formatCalendar(mCursor.getLong(COL_DATE));
		}

		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public void refreshData() {
			mMaxExpense = Math.max(MINIMUM_MAX_EXPENSE, mDbAdapter
					.getMaxExpenseAmongEachDays());

			if (mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
			mCursor = mDbAdapter.queryExpenseDayByDay();
			mCursor.moveToFirst();
		}

	}

}
