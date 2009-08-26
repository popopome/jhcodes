package com.jhlee.budgetsample;

import java.util.ArrayList;

import com.jhlee.budgetsample.RRMonthBudgetView.RRMonthBudgetDataProvider;

import android.app.Activity;
import android.os.Bundle;

public class BudgetSample extends Activity {
	public class SampleBudgetDataProvider implements RRMonthBudgetDataProvider {
		private int mYear;
		private int mMonth;
		private ArrayList<String>	mNames = new ArrayList<String>();
		private ArrayList<Long>		mAmounts = new ArrayList<Long>();
		
		public SampleBudgetDataProvider(int year, int month) {
			mNames.add("Grocery");
			mAmounts.add((long)(30.24*100));
			mNames.add("Restaurant");
			mAmounts.add((long)(20*100));
			mNames.add("Education");
			mAmounts.add((long)(300.24*100));
			
			mYear = year;
			mMonth = month;
		}

		@Override
		public int appendBudget(String budgetName, long totalAmount) {
			return 0;
		}

		@Override
		public boolean deleteBudget(int position) {
			return false;
		}

		@Override
		public long getBudgetAmount(int position) {
			return mAmounts.get(position);
		}

		@Override
		public int getBudgetCount() {
			return mNames.size();
		}

		@Override
		public String getBudgetName(int position) {
			return mNames.get(position);
		}

		@Override
		public int getMonth() {
			return mMonth;
		}

		@Override
		public long getTotalAmount() {
			int count = mAmounts.size();
			long sum = 0;
			for(int i=count-1;i>=0;--i) {
				sum += mAmounts.get(i);
			}
			return sum;
		}

		@Override
		public int getYear() {
			return mYear;
		}
		
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        RRMonthBudgetView view = (RRMonthBudgetView) findViewById(R.id.month_budget_view);
        view.setMonthBudgetDataProvider(new SampleBudgetDataProvider(2009, 8));
        
        /*RRBudgetInputDialog dlg = new RRBudgetInputDialog(this);
        dlg.show();*/
    }
}