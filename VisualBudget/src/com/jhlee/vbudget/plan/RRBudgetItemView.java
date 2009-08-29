package com.jhlee.vbudget.plan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.util.RRUtil;

public class RRBudgetItemView extends FrameLayout {
	String mBudgetName;
	long mBudgetAmount;
	long mBudgetBalance;

	View mFrame;
	ProgressBar mPrgBar;
	TextView mBudgetNameTextView;
	TextView mBudgetAmountTextView;
	Button mDeleteButton;

	OnDeleteButtonClickListener mListener;

	public RRBudgetItemView(Context context) {
		this(context, null);
	}

	public RRBudgetItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRBudgetItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mFrame = RRUtil.createViewsFromLayout(getContext(),
				R.layout.plan_budget_item_view, this);
		mPrgBar = (ProgressBar) mFrame.findViewById(R.id.budget_progress);
		mBudgetNameTextView = (TextView) mFrame.findViewById(R.id.budget_name);
		mBudgetAmountTextView = (TextView) mFrame
				.findViewById(R.id.budget_amount);
		mDeleteButton = (Button) mFrame.findViewById(R.id.button_delete);
	}

	public void setBudgetItemData(String budgetName, long budgetAmount,
			long budgetBalance) {
		mBudgetName = budgetName;
		mBudgetAmount = budgetAmount;
		mBudgetBalance = budgetBalance;

		mBudgetNameTextView.setText(mBudgetName);

		String balanceStr = RRUtil.formatMoney(mBudgetBalance / 100,
				mBudgetBalance % 100, true);
		String totalStr = RRUtil.formatMoney(mBudgetAmount / 100,
				mBudgetAmount % 100, true);

		mBudgetAmountTextView.setText(balanceStr + "/" + totalStr);
		mPrgBar.setMax((int) mBudgetAmount);
		mPrgBar.setProgress((int) mBudgetBalance);

		requestLayout();
	}

	public void disableDeleteButton() {
		mDeleteButton.setVisibility(View.GONE);
		requestLayout();
	}

	public void setOnDeleteButtonClickListener(
			OnDeleteButtonClickListener listener) {
		mListener = listener;

		mDeleteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null)
					mListener.onDeleteButtonClicked(RRBudgetItemView.this);

			}

		});
	}

	public interface OnDeleteButtonClickListener {
		public void onDeleteButtonClicked(View view);
	}
}
