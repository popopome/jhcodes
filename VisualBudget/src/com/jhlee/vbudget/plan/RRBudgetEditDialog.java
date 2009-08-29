package com.jhlee.vbudget.plan;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.expense.RRMoneyInputDialog;
import com.jhlee.vbudget.plan.RRBudgetMainView.RRBudgetDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRBudgetEditDialog extends Dialog {
	private static final int DEFAULT_BUDGET_MAX_AMOUNT = 150;
	private static final int DEFAULT_BUDGET_AMOUNT = 100;
	private RRBudgetDataProvider mProvider;
	private Spinner mBudgetSpinner;
	private SeekBar mSeekBar;
	private Button mBudgetAmountButton;
	private Button mOkButton;
	private Button mCancelButton;
	private Button mNewNameBtn;
	private boolean mIsCanceled = true;
	private boolean mIsEditMode = false;
	private ArrayAdapter<String> mBudgetNameAdapter;

	public RRBudgetEditDialog(Context context) {
		super(context);
		setContentView(R.layout.plan_budget_edit_dialog);

		this.setTitle("New Budget");
		mBudgetSpinner = (Spinner) findViewById(R.id.spinner_budget_name);

		mOkButton = (Button) findViewById(R.id.button_ok);
		mOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsCanceled = false;
				RRBudgetEditDialog.this.dismiss();
			}
		});
		mCancelButton = (Button) findViewById(R.id.button_cancel);
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* CANCEL BUTTON IS CLICKED */
				mIsCanceled = true;
				RRBudgetEditDialog.this.dismiss();
			}
		});

		mBudgetAmountButton = (Button) findViewById(R.id.button_budget_amount);
		mBudgetAmountButton.setText(RRUtil.formatMoney(
				DEFAULT_BUDGET_MAX_AMOUNT, 0, true));

		/* Initialize seek bar */
		mSeekBar = (SeekBar) findViewById(R.id.seekbar_amount);
		mSeekBar.setMax(DEFAULT_BUDGET_MAX_AMOUNT);
		mSeekBar.setProgress(DEFAULT_BUDGET_AMOUNT);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int val,
					boolean fromUser) {
				if (false == fromUser)
					return;

				val = (val + 5) / 10 * 10;
				mSeekBar.setProgress(val);

				/* Update budget amount value */
				String moneyStr = RRUtil.formatMoney(val, 0, true);
				mBudgetAmountButton.setText(moneyStr);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});

		/* New name button is clicked */
		mNewNameBtn = (Button) findViewById(R.id.button_new_budget_name);
		mNewNameBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final View view = RRUtil.createViewsFromLayout(
						RRBudgetEditDialog.this.getContext(),
						R.layout.plan_budget_new_entry, null);

				new AlertDialog.Builder(RRBudgetEditDialog.this.getContext())
						.setTitle("Enter new budget name").setView(view)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* User clicked OK so do some stuff */
										EditText budgetNameEdit = (EditText) view
												.findViewById(R.id.budget_name_edit);
										String budgetName = budgetNameEdit
												.getText().toString();
										budgetName.trim();
										if (budgetName.length() == 0)
											return;

										/* Find duplication */
										int pos = RRBudgetEditDialog.this
												.findBudgetName(budgetName);
										if (pos != -1) {
											/* Dup is found */
											mBudgetSpinner.setSelection(pos);
											return;
										}

										/* Add budget name to adapter */
										mBudgetNameAdapter.add(budgetName);
										mBudgetNameAdapter
												.notifyDataSetChanged();
										mBudgetSpinner
												.setSelection(mBudgetNameAdapter
														.getCount() - 1);

									}
								}).setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* Do nothing */
									}
								}).create().show();
			}
		});

		/* Initialize budget amount button */
		mBudgetAmountButton.setOnClickListener(new View.OnClickListener() {
			/* Button is clicked */
			@Override
			public void onClick(View v) {
				/* Let's show money input dialog */
				final RRMoneyInputDialog dlg = new RRMoneyInputDialog(
						RRBudgetEditDialog.this.getContext());
				dlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (dlg.isCanceled())
									return;
								/*
								 * Set the budget amount to button text &
								 * progress bar
								 */
								int dollars = dlg.getDollars();
								int cents = dlg.getCents();

								/* At least budget should be more than $1.00 */
								if (dollars == 0)
									return;

								String moneyStr = RRUtil.formatMoney(dollars,
										cents, true);
								mBudgetAmountButton.setText(moneyStr);

								/* Set progress bar */
								int maxDollars = mSeekBar.getMax();
								if (dollars > maxDollars) {
									int newMax = dollars + dollars / 3;
									mSeekBar.setMax(newMax);
								}

								mSeekBar.setProgress(dollars);
							}
						});
				dlg.show();
			}
		});

	}

	public void initialize(RRBudgetDataProvider provider) {
		mProvider = provider;

		mBudgetNameAdapter = new ArrayAdapter<String>(this.getContext(),
				android.R.layout.simple_spinner_item);
		mBudgetNameAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayList<String> budgetNames = new ArrayList<String>();
		mProvider.getDefaultBudgetNames(budgetNames);
		int cnt = budgetNames.size();
		for (int i = 0; i < cnt; ++i) {
			mBudgetNameAdapter.add(budgetNames.get(i));
		}
		mBudgetSpinner.setAdapter(mBudgetNameAdapter);
	}

	/*
	 * Budget editing. Only amount can be changed.
	 */
	public void editBudget(String budgetName, long budgetAmount) {
		this.setTitle("Edit budget");
		final int pos = findBudgetName(budgetName);
		if (pos == -1) {
			/* Add budget name */
			addBudgetNameAndSelect(budgetName);
		} else {
			mBudgetSpinner.setSelection(pos);
		}
		mIsEditMode = true;
		mNewNameBtn.setVisibility(View.GONE);
		mBudgetAmountButton.setText(RRUtil.formatMoney(budgetAmount/100, budgetAmount%100, true));

		mBudgetSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				/*
				 * If current mode is edit mode, then we ignore item selection
				 * event
				 */
				if (mIsEditMode) {
					if(position == pos)
						return;
					
					mBudgetSpinner.setSelection(pos);
					Toast.makeText(RRBudgetEditDialog.this.getContext(),
							"In edit mode you cannot change budget name.",
							Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
	}

	private int findBudgetName(String budgetName) {
		int cnt = mBudgetNameAdapter.getCount();
		for (int pos = cnt - 1; pos >= 0; --pos) {
			String n = mBudgetNameAdapter.getItem(pos);
			if (0 == n.compareToIgnoreCase(budgetName)) {
				return pos;
			}
		}

		return -1;
	}

	private void addBudgetNameAndSelect(String budgetName) {
		mBudgetNameAdapter.add(budgetName);
		mBudgetNameAdapter.notifyDataSetChanged();
		mBudgetSpinner.setSelection(mBudgetNameAdapter.getCount() - 1);
	}

	public long getBudgetAmount() {
		String moneyStr = (String) mBudgetAmountButton.getText();
		if (null == moneyStr)
			return 0;
		double money = new Double(moneyStr.substring(1));
		return (long) (money * 100);
	}

	public String getBudgetName() {
		return (String) mBudgetSpinner.getSelectedItem();
	}

	public boolean isCanceled() {
		return mIsCanceled;
	}

}
