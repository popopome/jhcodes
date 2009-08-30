package com.jhlee.vbudget;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

import com.jhlee.vbudget.collect.RRCollectView;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRDailyExpenseCarouselView;
import com.jhlee.vbudget.overview.RRMoneyOverview;
import com.jhlee.vbudget.plan.RRBudgetMainView;
import com.jhlee.vbudget.plan.RRDbBasedBudgetDataProvider;
import com.jhlee.vbudget.statistics.RRStatisticsView;
import com.jhlee.vbudget.util.RRUtil;

public class Budgeting extends Activity implements TabHost.TabContentFactory {

	private static final String TAG = "Budgeting";
	private static final String VIEW_TAG_OVERVIEW = "overview";
	private static final String VIEW_TAG_EXPENSES = "expenses";
	private static final String VIEW_TAG_BUDGETING = "budgeting";
	private static final String VIEW_TAG_STATISTICS = "statistics";

	/* Db adapter */
	private RRDbAdapter mDbAdapter;
	
	private TabHost	mTabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* No title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setContentView(R.layout.budgeting);

		/* Initialize Db */
		mDbAdapter = new RRDbAdapter(this);
		mDbAdapter.setOwner(this);

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		final TabHost tabHost = mTabHost;
		tabHost.setup();

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
		
		/* Add tab change event handler */
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			/* Tab is changed */
			@Override
			public void onTabChanged(String tabId) {
				/* Find view */
				FrameLayout frame = tabHost.getTabContentView();
				int cnt = frame.getChildCount();
				for(int pos=cnt-1;pos>=0;--pos) {
					View child = frame.getChildAt(pos);
					String viewTag = (String) child.getTag();
					if(0 == viewTag.compareToIgnoreCase(tabId)) {
						((RRBudgetContent)child).refreshContent();
					}
				}
			}
		});

		/* Set background */
		TabWidget tabWidget = tabHost.getTabWidget();
		tabWidget.setBackgroundColor(0xff00853E);
		tabHost.getTabContentView().setBackgroundResource(R.drawable.global_background);
		
		/* Change text style */
		int cnt = tabWidget.getChildCount();
		for(int pos=cnt-1;pos>=0;--pos) {
			View view = tabWidget.getChildAt(pos);
			TextView titleView = (TextView) view.findViewById(android.R.id.title);
			titleView.setTextColor(Color.WHITE);
			titleView.setShadowLayer((float) 2.0, 0, 0, Color.BLACK);
			titleView.setPadding(2, 0, 2, 0);
		}
	}

	/** {@inheritDoc} */
	public View createTabContent(String tag) {
		View view = null;
		
		if(0 == tag.compareTo(VIEW_TAG_OVERVIEW)) {
			view = getOverviewView();
		} else if(0 == tag.compareTo(VIEW_TAG_EXPENSES)) {
			view = getCarouselView();
		} else if(0 == tag.compareTo(VIEW_TAG_BUDGETING)) {
			view = getPlanView();
		} else if(0 == tag.compareTo(VIEW_TAG_STATISTICS)) {
			view = getStatisticsView();
		} else {
			return null;
		}
		
		view.setTag(tag);
		return view;
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

		statView.refreshContent();

		return statView;
	}
}