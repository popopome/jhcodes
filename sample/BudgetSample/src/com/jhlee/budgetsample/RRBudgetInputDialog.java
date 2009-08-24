package com.jhlee.budgetsample;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RRBudgetInputDialog extends Dialog {

	private EditText mBudgetNameEdit;
	private TextView mBudgetAmountView;
	private ListView mRecommendsList;

	private String mBudgetName;
	private long mAmount;
	private boolean mIsCanceled = false;

	public RRBudgetInputDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public RRBudgetInputDialog(Context context, int theme) {
		super(context, theme);
	}

	public RRBudgetInputDialog(Context context) {
		super(context);
	}

	/**
	 * Dialog is created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rr_budget_dialog);

		mBudgetNameEdit = (EditText) findViewById(R.id.budget_name);
		mBudgetAmountView = (TextView) findViewById(R.id.budget_total);
		mRecommendsList = (ListView) findViewById(R.id.budget_recoomends);

		mBudgetAmountView.setTextSize(mBudgetNameEdit.getTextSize());

		ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
		String[] sampleNames = new String[] { "Grocery", "Gas", "Dinning",
				"Cell phones", "Video games", "Restaurants", "Entertainment",
				"Cars", "Computers", "Internet", "Clothes", "Shoes",
				"Handbags", "Cable TV", "Statelite TV", "Credit Cards",
				"School", "Education", "Child care", "Travel", "Services",
				"Medical", "Recreation" };
		/* Sort names */
		java.util.Arrays.sort(sampleNames, String.CASE_INSENSITIVE_ORDER);

		int cnt = sampleNames.length;
		for (int i = 0; i < cnt; ++i) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("BudgetName", sampleNames[i]);
			itemList.add(item);
		}

		SimpleAdapter adapter = new SimpleAdapter(this.getContext(), itemList,
				R.layout.rr_budget_sample_item, new String[] { "BudgetName" },
				new int[] { R.id.budget_sample_name });
		mRecommendsList.setAdapter(adapter);

		mRecommendsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				TextView tv = (TextView) view;
				mBudgetNameEdit.setText(tv.getText());
			}
		});

		/* OK button */
		Button btnOk = (Button) findViewById(R.id.ok_button);
		btnOk.setOnClickListener(new View.OnClickListener() {
			/* OK button is clicked */
			@Override
			public void onClick(View v) {
				mIsCanceled = false;
				mBudgetName = mBudgetNameEdit.getText().toString();
				String valueString = (String) mBudgetAmountView.getText();
				/* Expect value will be $123.xx */
				Double tmp = new Double(valueString.substring(1));
				mAmount = (long) (tmp * 100);

				RRBudgetInputDialog.this.dismiss();
			}
		});

		/* Cancel button */
		Button btnCancel = (Button) findViewById(R.id.cancel_button);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsCanceled = true;
				RRBudgetInputDialog.this.cancel();
			}
		});

		this.setTitle("New budget");
	}

	public void editBudget(String budgetName, long budgetMoney) {
		mBudgetNameEdit.setText(budgetName);
		mBudgetAmountView.setText(Long.toString(budgetMoney));
		this.setTitle("Change your budget");
	}
	
	public boolean isCanceled() {
		return mIsCanceled;
	}

}
