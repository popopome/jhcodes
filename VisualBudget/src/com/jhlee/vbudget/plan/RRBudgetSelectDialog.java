package com.jhlee.vbudget.plan;

import java.util.Calendar;
import java.util.SimpleTimeZone;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.plan.RRMonthBudgetView.OnBudgetItemClickListener;

public class RRBudgetSelectDialog extends Dialog {

	private RRMonthBudgetView mBudgetView;
	private RRDbBasedBudgetDataProvider mBudgetDataProvider;
	private RRDbAdapter mDbAdapter;

	private Button mDoneButton;

	private int mYear;
	private int mMonth;
	private String mSelectedBudgetName;
	private long mSelectedBudgetAmount;
	private long mSelectedBudgetBalance;

	private boolean mIsCanceled = true;

	public RRBudgetSelectDialog(Context context) {
		super(context);

	}

	public void initialize(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;

		setContentView(R.layout.plan_budget_select_dialog);

		mDoneButton = (Button) findViewById(R.id.button_done);
		mDoneButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsCanceled = true;
				RRBudgetSelectDialog.this.dismiss();

			}

		});

		setUpMonthBudgetView();
	}

	private void setUpMonthBudgetView() {
		mBudgetDataProvider = new RRDbBasedBudgetDataProvider(mDbAdapter);

		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH) + 1;

		mBudgetView = (RRMonthBudgetView) findViewById(R.id.month_budget_view);
		mBudgetView.setYearMonth(mYear, mMonth);
		mBudgetView.hideDeleteButton();
		mBudgetView.setBudgetDataProvider(mBudgetDataProvider);
		/*
		 * Budget item is clicked
		 */
		mBudgetView
				.setOnBudgetItemClickListener(new OnBudgetItemClickListener() {

					@Override
					public void onBudgetItemClicked(int year, int month,
							String budgetName, long budgetAmount,
							long budgetBalance) {
						mYear = year;
						mMonth = month;

						mSelectedBudgetName = budgetName;
						mSelectedBudgetAmount = budgetAmount;
						mSelectedBudgetBalance = budgetBalance;

						mIsCanceled = false;

						RRBudgetSelectDialog.this.dismiss();
					}

				});
	}

	public boolean isCanceled() {
		return mIsCanceled;
	}
	
	public String getSelectedBudgetName() {
		return mSelectedBudgetName;
	}
	public long getSelectedBudgetAmount() {
		return mSelectedBudgetAmount;
	}
	public long getSelectedBudgetBalance() {
		return mSelectedBudgetBalance;
	}
	public int getSelectedBudgetYear() {
		return mYear;
	}
	public int getSelectedBudgetMonth() {
		return mMonth;
	}
}
