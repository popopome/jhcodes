package com.jhlee.vbudget;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

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
	private static final String VIEW_TAG_EMPTY_EXPENSES = "empty_expenses";

	/* Db adapter */
	private RRDbAdapter mDbAdapter;

	private TabHost mTabHost;

	/*
	 * Hold menu
	 */
	private Menu mMenu;

	/*
	 * Activity is created. Initialize stuffs
	 */
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

		Resources res = getResources();
		
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_OVERVIEW)
				.setIndicator("Overview", res.getDrawable(R.drawable.icon_overview)).setContent(this));
		
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_EXPENSES).setIndicator(
				"Expenses", res.getDrawable(R.drawable.icon_expense)).setContent(this));
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_BUDGETING).setIndicator(
				"Budgeting", res.getDrawable(R.drawable.icon_budgeting)).setContent(this));
		tabHost.addTab(tabHost.newTabSpec(VIEW_TAG_STATISTICS).setIndicator(
				"Statistics", res.getDrawable(R.drawable.icon_statistics)).setContent(this));

		/* Add tab change event handler */
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			/* Tab is changed */
			@Override
			public void onTabChanged(String tabId) {
				RRBudgetContent content = (RRBudgetContent) mTabHost
						.getCurrentView();
				content.refreshContent();

				/* Let's create menu. */
				if (mMenu != null) {
					int menuCnt = mMenu.size();
					for (int menuPos = menuCnt - 1; menuPos >= 0; --menuPos) {
						int id = mMenu.getItem(menuPos).getItemId();
						mMenu.removeItem(id);
					}

					content.createMenu(mMenu, Budgeting.this.getMenuInflater());
				}
				return;
			}
		});

		/* Set background */
		TabWidget tabWidget = tabHost.getTabWidget();
		tabWidget.setBackgroundColor(0xff00853E);
		tabHost.getTabContentView().setBackgroundResource(
				R.drawable.global_background);

		/* Change text style */
		int cnt = tabWidget.getChildCount();
		for (int pos = cnt - 1; pos >= 0; --pos) {
			View view = tabWidget.getChildAt(pos);
			TextView titleView = (TextView) view
					.findViewById(android.R.id.title);
			titleView.setTextColor(Color.WHITE);
			titleView.setShadowLayer((float) 2.0, 0, 0, Color.BLACK);
			titleView.setPadding(2, 0, 2, 0);
		}
	}

	/** {@inheritDoc} */
	public View createTabContent(String tag) {
		View view = null;

		if (0 == tag.compareTo(VIEW_TAG_OVERVIEW)) {
			view = getOverviewView();
		} else if (0 == tag.compareTo(VIEW_TAG_EXPENSES)) {
			view = getExpenseView();
		} else if (0 == tag.compareTo(VIEW_TAG_BUDGETING)) {
			view = getPlanView();
		} else if (0 == tag.compareTo(VIEW_TAG_STATISTICS)) {
			view = getStatisticsView();
		} else {
			return null;
		}

		return view;
	}


	/*
	 * Resumed
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mTabHost != null) {
			RRBudgetContent content = (RRBudgetContent) mTabHost.getCurrentView();
			if(content != null) {
				content.onViewResumed();
			}
		}
	}

	private View findContentView(String tabId) {
		/* Find view */
		FrameLayout frame = mTabHost.getTabContentView();
		int cnt = frame.getChildCount();
		int pos = cnt - 1;
		View child = null;
		for (; pos >= 0; --pos) {
			child = frame.getChildAt(pos);
			/*
			 * View can have no tag.
			 */
			String viewTag = (String) child.getTag();
			if (null == viewTag) {
				continue;
			}

			if (0 == viewTag.compareToIgnoreCase(tabId)) {
				return child;
			}
		}

		return null;
	}

	private View getOverviewView() {
		RRMoneyOverview view = new RRMoneyOverview(this);
		view.initialize(mDbAdapter);
		view.setTag(VIEW_TAG_OVERVIEW);

		/*
		 * For overview case, I call refresh content explicitly at here. At very
		 * first time Android tab widget does not give tab change event.
		 */
		view.refreshContent();

		return view;
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
		budgetView.setTag(VIEW_TAG_BUDGETING);
		return budgetView;
	}

	/*
	 * Carousel expense view
	 */
	private View getExpenseView() {
		RRDailyExpenseCarouselView carouselView = new RRDailyExpenseCarouselView(
				this);
		carouselView.initializeViews(mDbAdapter);
		// if (false == carouselView.initializeViews(mDbAdapter)) {
		// Log.e(TAG, "Unable to initialize carousel view");
		// /* Let's return empty data */
		// return RRUtil.createViewsFromLayout(this,
		// R.layout.empty_data_guide, null);
		// }

		carouselView.setTag(VIEW_TAG_EXPENSES);
		return carouselView;
	}

	// /*
	// * Get detail view
	// */
	// private View getDetailView() {
	//
	// /*
	// * If expense id is not given, here the program uses latest expense.
	// */
	// Integer expenseId;
	// if (null == param) {
	// expenseId = mDbAdapter.queryLatestExpenseId();
	// if (-1 == expenseId) {
	// /* TODO: return empty view. */
	// return null;
	// }
	// } else {
	// /*
	// * ?? I don't know java well. Following code is somewhat silly.
	// */
	// long val = (Long) param;
	// expenseId = (int) val;
	// }
	//
	// RRDetailExpenseView detailView = new RRDetailExpenseView(this);
	// detailView.setExpense(mDbAdapter, expenseId);
	// return detailView;
	// }

	/*
	 * Statistics view
	 */
	private View getStatisticsView() {
		RRStatisticsView statView = new RRStatisticsView(this);
		statView.setUp(mDbAdapter);

		statView.refreshContent();
		statView.setTag(VIEW_TAG_STATISTICS);

		return statView;
	}

	/*
	 * Menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Keep menu */
		mMenu = menu;

		RRBudgetContent content = (RRBudgetContent) mTabHost.getCurrentView();
		content.createMenu(menu, getMenuInflater());

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Option item is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		RRBudgetContent content = (RRBudgetContent) mTabHost.getCurrentView();
		content.onMenuItemSelected(item);
		
		return super.onOptionsItemSelected(item);
	}
	
	

}