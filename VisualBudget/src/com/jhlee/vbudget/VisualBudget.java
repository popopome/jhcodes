package com.jhlee.vbudget;

import java.util.HashMap;

import junit.framework.Assert;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.jhlee.vbudget.RRCommandBar.OnCommandExecuteListener;
import com.jhlee.vbudget.collect.RRCollectView;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRDailyExpenseCarouselView;
import com.jhlee.vbudget.expense.RRDetailExpenseView;
import com.jhlee.vbudget.plan.RRBudgetMainView;
import com.jhlee.vbudget.plan.RRDbBasedBudgetDataProvider;
import com.jhlee.vbudget.statistics.RRStatisticsView;

public class VisualBudget extends Activity implements OnCommandExecuteListener {
	private static final String TAG = "VisualBudget";

	
	public static final int RR_CMD_OVERVIEW = 1;
	public static final int RR_CMD_COLLECT = 2;
	public static final int RR_CMD_PLAN = 3;
	public static final int RR_CMD_DAILY_EXPENSE_CAROUSEL = 4;
	public static final int RR_CMD_DETAIL_EXPENSE = 5;
	public static final int RR_CMD_STATISTICS = 6;

	/* Db adapter */
	private RRDbAdapter mDbAdapter;
	/* Command bar */
	private RRCommandBar mCmdBar;
	/* Frame */
	private FrameLayout mContentFrame;

	/* Content host */
	private RRMoneyContentHost mContentHost = new RRMoneyContentHost();

	/* Active view index */
	private int mActiveViewIndex = -1;

	/* Views */
	private HashMap<Integer, View> mContentViewPool = new HashMap<Integer, View>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* No title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* Initialize Db */
		mDbAdapter = new RRDbAdapter(this);
		mDbAdapter.setOwner(this);

		/* Layout set up */
		setContentView(R.layout.visual_budget);

		/* Command bar */
		mCmdBar = (RRCommandBar) findViewById(R.id.command_bar);
		mCmdBar.setOnCommandExecuteListener(this);

		int[] cmdIds = new int[] { RR_CMD_OVERVIEW, RR_CMD_COLLECT, RR_CMD_PLAN,
				RR_CMD_DAILY_EXPENSE_CAROUSEL, RR_CMD_DETAIL_EXPENSE,
				RR_CMD_STATISTICS };
		String[] cmdLabels = new String[] { "Overview", "Collect", "Plan",
				"Carousel", "Deatail", "Statistics" };
		int cnt = cmdIds.length;
		for (int i = 0; i < cnt; ++i) {
			mCmdBar.addCommandSpec(cmdIds[i], cmdLabels[i]);
		}
		mCmdBar.updatedCommandSpecs();

		/* Frame */
		mContentFrame = (FrameLayout) findViewById(R.id.view_frame);

	}

	/*
	 * Command is clicked
	 */
	@Override
	public void onCommandExecute(int cmdId, String cmdLabel) {
		performCommand(cmdId, cmdLabel, null);
	}

	private void performCommand(int cmdId, String cmdLabel, Object param) {
		switch (cmdId) {
		case RR_CMD_COLLECT: {
			this.setMoneyContent(getCollectView(cmdId, cmdLabel));
			
		}
			break;
		case RR_CMD_PLAN:
			this.setMoneyContent(getPlanView(cmdId, cmdLabel));
			break;
		/* Show carousel view */
		case RR_CMD_DAILY_EXPENSE_CAROUSEL:
			this.setMoneyContent(getCarouselView(cmdId, cmdLabel));
			break;
		case RR_CMD_DETAIL_EXPENSE:
			this.setMoneyContent(getDetailView(cmdId, cmdLabel, param));
			break;
		case RR_CMD_STATISTICS:
			this.setMoneyContent(getStatisticsView(cmdId, cmdLabel));
			break;
		}
	}
	
	/*
	 * Get collect view
	 */
	private View getCollectView(int cmdId, String cmdLabel) {
		/* Find plan content view */
		RRCollectView collectView = (RRCollectView) mContentViewPool.get(cmdId);
		if (null == collectView) {
			collectView = new RRCollectView(this);
			collectView.initialize(mDbAdapter);
			mContentViewPool.put(cmdId, collectView);
		}
		return collectView;
	}

	/*
	 * Get plan view
	 */
	private View getPlanView(int cmdId, String cmdLabel) {
		/* Find plan content view */
		RRBudgetMainView budgetView = (RRBudgetMainView) mContentViewPool.get(cmdId);
		if (null == budgetView) {
			budgetView = new RRBudgetMainView(this);
			mContentViewPool.put(cmdId, budgetView);
			RRDbBasedBudgetDataProvider dataProvider = new RRDbBasedBudgetDataProvider(
					mDbAdapter);
			budgetView.setBudgetDataProvider(dataProvider);
		} else {
			budgetView.refreshData();
		}
		return budgetView;
	}

	/*
	 * Carousel expense view
	 */
	private View getCarouselView(int cmdId, String cmdLabel) {
		RRDailyExpenseCarouselView carouselView = (RRDailyExpenseCarouselView) mContentViewPool
				.get(cmdId);
		if (null == carouselView) {
			/* Create carousel view */
			carouselView = new RRDailyExpenseCarouselView(this);
			if (false == carouselView.initializeViews(mDbAdapter)) {
				Log.e(TAG, "Unable to initialize carousel view");
				return null;
			}
			carouselView.setMoneyContentHost(mContentHost);
			mContentViewPool.put(cmdId, carouselView);
		} else {
			carouselView.refreshData();
		}

		return carouselView;
	}

	/*
	 * Get detail view
	 */
	private View getDetailView(int cmdId, String cmdLabel, Object param) {

		/*
		 * If expense id is not given, here the program uses latest expense.
		 */
		Integer expenseId;
		if (null == param) {
			expenseId = mDbAdapter.queryLatestExpenseId();
			if (-1 == expenseId) {
				/* TODO: return empty view. */
				return null;
			}
		} else {
			/*
			 * ?? I don't know java well. Following code is somewhat silly.
			 */
			long val = (Long) param;
			expenseId = (int) val;
		}

		RRDetailExpenseView detailView = (RRDetailExpenseView) mContentViewPool
				.get(cmdId);
		if (null == detailView) {
			detailView = new RRDetailExpenseView(this);
			mContentViewPool.put(cmdId, detailView);
		}

		detailView.setExpense(mDbAdapter, expenseId);
		return detailView;
	}

	/*
	 * Statistics view
	 */
	private View getStatisticsView(int cmdId, String cmdLabel) {
		RRStatisticsView statView = (RRStatisticsView) mContentViewPool
				.get(cmdId);
		if (null == statView) {
			statView = new RRStatisticsView(this);
			statView.setUp(mDbAdapter);

			mContentViewPool.put(cmdId, statView);
		}

		statView.refreshData();

		return statView;
	}

	/*
	 * Set view
	 */
	private void setMoneyContent(View view) {
		/*
		 * If there is view inside of frame, let's blow it.
		 */
		int viewIndex = mContentFrame.indexOfChild(view);
		if (-1 != viewIndex) {
			if (mActiveViewIndex == viewIndex) {
				/* View is already active */
				return;
			}
		}

		/* Make previous view as gone. 
		 * If mActiveViewIndex == -1, there is no active view index
		 */
		if(mActiveViewIndex != -1) {
			View prevView = mContentFrame.getChildAt(mActiveViewIndex);
			if (prevView.hasFocus())
				prevView.clearFocus();
			prevView.setVisibility(View.GONE);	
		}
		

		/*
		 * Check view has parent or not. If it has parent, then we consider the
		 * view is already attached to frame layout.
		 */
		if (view.getParent() == null) {
			mContentFrame.addView(view, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT));
			mActiveViewIndex = mContentFrame.indexOfChild(view);
		} else {
			/*
			 * We know view has parent and the parent should be mContentFrame.
			 */
			Assert.assertTrue(viewIndex != -1);
			mActiveViewIndex = viewIndex;

			/* Make it visible */
			view.setVisibility(View.VISIBLE);
		}

		/* Give focus */
		if (false == mCmdBar.hasFocus()) {
			/*
			 * Command bar has no focus, give focus to content
			 */
			view.requestFocus();
		}

		/* Apply layout computation */
		mContentFrame.requestLayout();
	}

	/*
	 * Content manipulation which is provided to content view
	 */
	public class RRMoneyContentHost {
		public void showMoneyContent(int command, Object param1, Object param2) {
			VisualBudget.this.performCommand(command, "MoneyMoney", param1);

			/* Change command button */
			mCmdBar.setActiveCommand(command);
		}
	}
}
