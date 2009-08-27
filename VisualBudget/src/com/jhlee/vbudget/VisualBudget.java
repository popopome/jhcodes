package com.jhlee.vbudget;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jhlee.vbudget.CommandBar.OnCommandExecuteListener;
import com.jhlee.vbudget.camera.RRTakeReceiptActivity;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRDailyExpenseCarouselView;
import com.jhlee.vbudget.plan.RRBudgetView;
import com.jhlee.vbudget.plan.RRDbBasedBudgetDataProvider;

public class VisualBudget extends Activity implements OnCommandExecuteListener {
	private static final String 	TAG = "VisualBudget";
	
	/* Db adapter */
	private RRDbAdapter mDbAdapter;
	/* Command bar */
	private CommandBar mCmdBar;
	/* Frame */
	private FrameLayout mContentFrame;
	
	/* Active view index */
	private int	mActiveViewIndex = -1;

	/* Views */
	private HashMap<Integer, View> mContentViewPool = new HashMap<Integer, View>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Initialize Db */
		mDbAdapter = new RRDbAdapter(this);
		mDbAdapter.setOwner(this);

		/* Layout set up */
		setContentView(R.layout.visual_budget);

		/* Command bar */
		mCmdBar = (CommandBar) findViewById(R.id.command_bar);
		mCmdBar.setOnCommandExecuteListener(this);

		/* Frame */
		mContentFrame = (FrameLayout) findViewById(R.id.view_frame);
	}

	/*
	 * Command is clicked
	 */
	@Override
	public void onCommandExecute(int cmd) {
		switch (cmd) {
		case CommandBar.COMMAND_CAMERA: {
			Intent i = new Intent(this, RRTakeReceiptActivity.class);
			this.startActivity(i);
		}
			break;
		case CommandBar.COMMAND_SHOW_THE_PLAN: {
			/* Find plan content view */
			RRBudgetView budgetView = (RRBudgetView) mContentViewPool.get(cmd);
			if (null == budgetView) {
				budgetView = new RRBudgetView(this);
				mContentViewPool.put(cmd, budgetView);
				RRDbBasedBudgetDataProvider dataProvider = new RRDbBasedBudgetDataProvider(
						this, mDbAdapter);
				budgetView.setBudgetDataProvider(dataProvider);
			} else {
				budgetView.refreshData();
			}

			this.setMoneyContent(budgetView);
		}
			break;
			/* Show carousel view */
		case CommandBar.COMMAND_DAILY_CAROUSEL: {
			RRDailyExpenseCarouselView carouselView = (RRDailyExpenseCarouselView)mContentViewPool.get(cmd);
			if(null == carouselView) {
				carouselView = new RRDailyExpenseCarouselView(this);
				if(false == carouselView.initializeViews(mDbAdapter)) {
					Log.e(TAG, "Unable to initialize carousel view");
					return;
				}
				mContentViewPool.put(cmd, carouselView);
			} else {
				carouselView.refreshData();
			}
			
			this.setMoneyContent(carouselView);
		}
			break;
		}
	}

	/*
	 * Set view
	 */
	private void setMoneyContent(View view) {
		/* If there is view inside of frame,
		 * let's blow it.
		 */
		int viewIndex = mContentFrame.indexOfChild(view);
		if(-1 != viewIndex) {
			if(mActiveViewIndex == viewIndex) {
				/* View is already active */
				return;
			}
			
			/* Make previous view as gone. */
			View prevView = mContentFrame.getChildAt(viewIndex);
			if(prevView.hasFocus())
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
		}

		/* Give focus */
		if (false == mCmdBar.hasFocus()) {
			/*
			 * Command bar has no focus, give focus to content
			 */
			view.requestFocus();
		}
	}
}
