package com.jhlee.vbudget.statistics;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.statistics.RRChartBarStreamView.RRChartBarDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRStatisticsView extends FrameLayout {
	
	private RRDbAdapter	mDbAdapter;

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
		RRChartBarGraph graph= (RRChartBarGraph) createDayByDayExpenseGraph();
		this.addView(graph,
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.FILL_PARENT));
	}
	
	public void refreshData() {
		RRChartBarGraph graph = (RRChartBarGraph) this.getChildAt(0);
		graph.refreshData();
	}
	
	
	/**
	 * Create day-by-day expense graph
	 * @return
	 */
	private View createDayByDayExpenseGraph() {
		RRChartBarGraph graph = new RRChartBarGraph(this.getContext());
		graph.setChartBarDataProvider(new RRDayByDayExpenseDataProvider());
		graph.setBarWidth(45);
        graph.setBarValueNameTextSize(9);
        graph.setTitleTextSize(20);
        graph.setXYAxisName("Date", "Money");
        graph.setGraphTitle("Day by day expense\nThe graph shows how you spend out money for each day");
        graph.setEmptyText("Day by day expense - N/A");
        graph.setBarMaxHeight(100);
        return graph;
	}
	
	/**
	 * Create day-by-day expense graph
	 * @return
	 */
	private View createDayOfWeekExpenseGraph() {
		RRChartBarGraph graph = new RRChartBarGraph(this.getContext());
		graph.setChartBarDataProvider(new RRDayOfWeekExpenseDataProvider());
		graph.setBarWidth(45);
        graph.setBarValueNameTextSize(9);
        graph.setTitleTextSize(20);
        graph.setXYAxisName("DayOfWeek", "Money");
        graph.setGraphTitle("Day of week expense\nWhich day do you usually spend money?");
        graph.setEmptyText("Day of week expense - N/A");
        graph.setBarMaxHeight(100);
        return graph;
	}
	
	
	
	public class RRDayOfWeekExpenseDataProvider implements RRChartBarDataProvider {
		private static final int COL_DAY_OF_WEEK = 0;
		private static final int COL_TOTAL = 1;
		private Cursor mCursor;
		public RRDayOfWeekExpenseDataProvider() {
			mCursor = mDbAdapter.queryExpenseDayOfWeek();
		}
		public long getBarMaxValue() {
			mCursor.moveToFirst();
			long maxValue = 0;
			while(false == mCursor.isAfterLast()) {
				maxValue = Math.max(maxValue, mCursor.getLong(COL_TOTAL));
				mCursor.moveToNext();
			}
			return maxValue;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return RRUtil.formatMoney(expense/100, expense%100, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_TOTAL);
			return (long)expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			switch(mCursor.getInt(COL_DAY_OF_WEEK)) {
			case 0:	return "SUN";
			case 1: return "MON";
			case 2: return "TUE";
			case 3: return "WED";
			case 4: return "THU";
			case 5: return "FRI";
			case 6: return "SAT";
			}
			return "N/A";
		}

		public int getCount() {
			return mCursor.getCount();
		}
	}
	public class RRDayByDayExpenseDataProvider implements RRChartBarDataProvider {
		private static final int COL_DATE = 0;
		private static final int COL_EXPENSE = 1;
		/* Minimum expense $10 */
		private static final long MINIMUM_MAX_EXPENSE = 1000;
		private long mMaxExpense;
		private Cursor mCursor;
		public RRDayByDayExpenseDataProvider() {
			mMaxExpense = Math.max(MINIMUM_MAX_EXPENSE, mDbAdapter.getMaxExpenseAmongEachDays());
			
			mCursor = mDbAdapter.queryExpenseDayByDay();
			mCursor.moveToFirst();
			while(mCursor.isAfterLast()) {
				mCursor.moveToNext();
			}
		}
		public long getBarMaxValue() {
			return mMaxExpense;
		}

		public String getBarTitle(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return RRUtil.formatMoney(expense/100, expense%100, true);
		}

		public long getBarValue(int position) {
			mCursor.moveToPosition(position);
			long expense = mCursor.getLong(COL_EXPENSE);
			return (int)expense;
		}

		public String getBarValueName(int position) {
			mCursor.moveToPosition(position);
			return RRUtil.formatCalendar(mCursor.getLong(COL_DATE));
		}

		public int getCount() {
			return mCursor.getCount();
		}
		
	}
	

}
