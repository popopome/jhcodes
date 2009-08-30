package com.jhlee.vbudget.collect;

import java.util.HashSet;
import java.util.Iterator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.expense.RRMoneyInputDialog;
import com.jhlee.vbudget.plan.RRBudgetSelectDialog;
import com.jhlee.vbudget.tags.RRTagDataProviderFromDb;
import com.jhlee.vbudget.tags.RRTagSelectDialog;
import com.jhlee.vbudget.util.RRUtil;

public class RRTransactionEditDialog extends Dialog {

	private RRDbAdapter mDbAdapter;
	private Button mBudgetBrowseButton;
	private Button mMoneyPadButton;
	private Button mTagBrowserButton;
	private Button mOkButton;
	private Button mCancelButton;

	private TextView mBudgetNameView;
	private TextView mTransAmountView;
	private TextView mTagListView;
	
	private HashSet<String>		mSelectedTags = new HashSet<String>();
	private int mBudgetYear;
	private int mBudgetMonth;
	
	private boolean mIsCanceled = true; 

	public RRTransactionEditDialog(Context context) {
		super(context);
	}

	public void initialize(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;
		this.setContentView(R.layout.collect_transaction_edit_dialog);
		this.setTitle("ENTER EXPENSE");

		mBudgetBrowseButton = (Button) findViewById(R.id.button_budget_browse);
		mMoneyPadButton = (Button) findViewById(R.id.button_numpad);
		mTagBrowserButton = (Button) findViewById(R.id.button_tag_browser);

		mBudgetNameView = (TextView) findViewById(R.id.budget_name);
		mTransAmountView = (TextView) findViewById(R.id.trans_amount);
		mTagListView = (TextView) findViewById(R.id.tag_list);

		installMoneyPadButtonEventHandler();
		installTagBrowserButtonEventHandler();		
		installBudgetBrowserButtonEventHandler();
		
		mOkButton = (Button)findViewById(R.id.button_ok);
		mCancelButton = (Button)findViewById(R.id.button_cancel);
		
		mOkButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/* Check expense amount and tags.
				 * It is minimum requirement to save budget
				 */
				long money = getExpenseAmount();
				if(money == 0) {
					showGuideMessage("Please enter expense amount");
					return;
				}
				
				if(mSelectedTags.isEmpty()) {
					showGuideMessage("Please tag for expense");
					return;
				}
				
				mIsCanceled = false;
				RRTransactionEditDialog.this.dismiss();
			}
			
		});
		mCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsCanceled = true;
				RRTransactionEditDialog.this.dismiss();
			}
			
		});
		
	}

	private void installBudgetBrowserButtonEventHandler() {
		mBudgetBrowseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final RRBudgetSelectDialog dlg = new RRBudgetSelectDialog(RRTransactionEditDialog.this.getContext());
				dlg.initialize(mDbAdapter);
				
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						if(dlg.isCanceled())
							return;

						mBudgetNameView.setText(dlg.getSelectedBudgetName());
						mBudgetYear = dlg.getSelectedBudgetYear();
						mBudgetMonth = dlg.getSelectedBudgetMonth();
						return;
					}
					
				});
				dlg.show();
			}
		});
	}

	private void installMoneyPadButtonEventHandler() {
		/* Install money pad button */
		mMoneyPadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final RRMoneyInputDialog dlg = new RRMoneyInputDialog(
						RRTransactionEditDialog.this.getContext());
				String moneyStr = mTransAmountView.getText().toString();
				long totalMoney = 0;
				if(moneyStr.length() > 0) {
					moneyStr = moneyStr.substring(1);
					try {
						totalMoney = (long) (100 * new Double(moneyStr));
					} catch (Exception e) {
						totalMoney = 0;
					}
				}
				
				dlg.setMoney((int) totalMoney / 100, (int) totalMoney % 100);
				dlg
						.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								if (dlg.isCanceled())
									return;
								String moneyStr = RRUtil.formatMoney(dlg
										.getDollars(), dlg.getCents(), true);
								mTransAmountView.setText(moneyStr);
								mTransAmountView.requestLayout();
							}

						});
				dlg.show();
			}

		});
	}
	
	/*
	 * Install tag browser button event handler
	 */
	private void installTagBrowserButtonEventHandler() {
		mTagBrowserButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final RRTagSelectDialog dlg = new RRTagSelectDialog(RRTransactionEditDialog.this.getContext());
				dlg.initialize(new RRStaticTagDataProvider());
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						if(dlg.isCanceled())
							return;
						
						StringBuilder sb = new StringBuilder();
						Iterator<String> it = mSelectedTags.iterator();
						while(it.hasNext()) {
							String tag = it.next();
							sb.append(tag);
							if(it.hasNext())
								sb.append(",");
						}
						mTagListView.setText(sb.toString());
						mTagListView.requestLayout();
					}
					
				});
				dlg.show();
			}
			
		});
	}
	
	public boolean isCanceled() {
		return mIsCanceled;
	}
	
	/*
	 * Get expense amount
	 */
	public long getExpenseAmount() {
		String moneyStr = mTransAmountView.getText().toString();
		moneyStr.trim();
		if(moneyStr.length() == 0)
			return 0;
		
		long money = 0;
		moneyStr = moneyStr.substring(1);
		try {
			money = (long) (100 * (new Double(moneyStr)));
		} catch(Exception e) {
			money = 0;
		}
		return money;
	}

	/*
	 * Show guide message
	 */
	private void showGuideMessage(String msg) {
		Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * Return tag list iterator
	 */
	public Iterator<String> getTaglistIterator()
	{
		return mSelectedTags.iterator();
	}
	
	/*
	 * Get budget id
	 */
	public long getSelectedBudgetId() {
		String budgetName = mBudgetNameView.getText().toString();
		return mDbAdapter.findBudgetItem(mBudgetYear, mBudgetMonth, budgetName);
	}
	
	/*
	 * Static tag data provider
	 */
	private class RRStaticTagDataProvider extends RRTagDataProviderFromDb {
		public RRStaticTagDataProvider() {
			super(mDbAdapter);
		}
		@Override
		public void check(int index) {
			String tag = this.getTag(index);
			mSelectedTags.add(tag);
		}

		@Override
		public boolean isChecked(int index) {
			String tag = this.getTag(index);
			return mSelectedTags.contains(tag);
		}

		@Override
		public void uncheck(int index) {
			String tag = this.getTag(index);
			mSelectedTags.remove(tag);
		}
		
	};
	
}
