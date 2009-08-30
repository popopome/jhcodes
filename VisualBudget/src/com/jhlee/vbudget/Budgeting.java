package com.jhlee.vbudget;

import android.app.TabActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

import com.jhlee.vbudget.collect.RRCollectView;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRDailyExpenseCarouselView;
import com.jhlee.vbudget.expense.RRDetailExpenseView;
import com.jhlee.vbudget.overview.RRMoneyOverview;
import com.jhlee.vbudget.plan.RRBudgetMainView;
import com.jhlee.vbudget.plan.RRDbBasedBudgetDataProvider;
import com.jhlee.vbudget.statistics.RRStatisticsView;
import com.jhlee.vbudget.util.RRUtil;

public class Budgeting extends TabActivity implements TabHost.TabContentFactory {

	private static final String TAG = "Budgeting";
	private static final String VIEW_TAG_OVERVIEW = "overview";
	private static final String VIEW_TAG_EXPENSES = "expenses";
	private static final String VIEW_TAG_BUDGETING = "budgeting";
	private static final String VIEW_TAG_STATISTICS = "statistics";

	/* Db adapter */
	private RRDbAdapter mDbAdapter;
	/* Command bar */
	private RRCommandBar mCmdBar;

	/* Active view index */
	private int mActiveViewIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* No title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Initialize Db */
		mDbAdapter = new RRDbAdapter(this);
		mDbAdapter.setOwner(this);

		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_OVERVIEW)
		// .setIndicator("Overview",
				// getResources().getDrawable(R.drawable.star_big_on))
				.setIndicator("Overview").setContent(this));
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_EXPENSES).setIndicator("Expenses")
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_BUDGETING)
				.setIndicator("Budgeting").setContent(this));
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_STATISTICS).setIndicator(
				"Statistics").setContent(this));
		
		/* Set background */
		tabHost.setBackgroundResource(R.drawable.global_background);
		
	}

	/** {@inheritDoc} */
	public View createTabContent(String tag) {
		if(0 == tag.compareTo(VIEW_TAG_OVERVIEW)) {
			return getOverviewView();
		} else if(0 == tag.compareTo(VIEW_TAG_EXPENSES)) {
			return getCarouselView();
		} else if(0 == tag.compareTo(VIEW_TAG_BUDGETING)) {
			return getPlanView();
		} else if(0 == tag.compareTo(VIEW_TAG_STATISTICS)) {
			return getStatisticsView();
		}
		return null;
	}

	private View getOverviewView() {
		RRMoneyOverview view = new RRMoneyOverview(this);
		view.initialize(mDbAdapter);

		return view;
	}

	/*
	 * Get collect view
	 */
	private View getCollectView() {
		/* Find plan content view */
		RRCollectView collectView = new RRCollectView(this);
		collectView.initialize(mDbAdapter);
		return collectView;
	}

	/*
	 * Get plan view
	 */
	private View getPlanView() {
		/* Find plan content view */
		RRBudgetMainView budgetView = new RRBudgetMainView(this);
		RRDbBasedBudgetDataProvider dataProvider = new RRDbBasedBudgetDataProvider(
				mDbAdapter);
		budgetView.setBudgetDataProvider(dataProvider);
		return budgetView;
	}

	/*
	 * Carousel expense view
	 */
	private View getCarouselView() {
		RRDailyExpenseCarouselView carouselView = new RRDailyExpenseCarouselView(
				this);
		if (false == carouselView.initializeViews(mDbAdapter)) {
			Log.e(TAG, "Unable to initialize carousel view");
			/* Let's return empty data */
			return RRUtil.createViewsFromLayout(this, R.layout.empty_data_guide, null);
		}

		return carouselView;
	}

//	/*
//	 * Get detail view
//	 */
//	private View getDetailView() {
//
//		/*
//		 * If expense id is not given, here the program uses latest expense.
//		 */
//		Integer expenseId;
//		if (null == param) {
//			expenseId = mDbAdapter.queryLatestExpenseId();
//			if (-1 == expenseId) {
//				/* TODO: return empty view. */
//				return null;
//			}
//		} else {
//			/*
//			 * ?? I don't know java well. Following code is somewhat silly.
//			 */
//			long val = (Long) param;
//			expenseId = (int) val;
//		}
//
//		RRDetailExpenseView detailView = new RRDetailExpenseView(this);
//		detailView.setExpense(mDbAdapter, expenseId);
//		return detailView;
//	}

	/*
	 * Statistics view
	 */
	private View getStatisticsView() {
		RRStatisticsView statView = new RRStatisticsView(this);
		statView.setUp(mDbAdapter);

		statView.refreshData();

		return statView;
	}
}