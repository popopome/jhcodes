package com.jhlee.budgetsample;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.jhlee.budgetsample.RRBudgetView.RRBudgetDataProvider;

public class BudgetSample extends Activity {
	public class SampleBudgetDataProvider implements RRBudgetDataProvider {
		
		private ArrayList<RRBudgetItemData> mItems = new ArrayList<RRBudgetItemData>();
		
		public SampleBudgetDataProvider(int year, int month) {
			RRBudgetItemData itemData =new RRBudgetItemData("Grocery", 10000); 
			mItems.add(itemData );
			mItems.add(new RRBudgetItemData("Restaurant", 30000));
			mItems.add(new RRBudgetItemData("Education", 25000));
			mItems.add(new RRBudgetItemData("Movie", 1000));
		}

		@Override
		public int appendBudgetItem(int year, int month,
				RRBudgetItemData budgetData) {
			mItems.add(new RRBudgetItemData(budgetData));
			return mItems.size()-1;
		}

		@Override
		public boolean deleteBudgetItem(int year, int month, int position) {
			return false;
		}

		@Override
		public long getBudgetAmount(int year, int month) {
			/* Get total */
			int cnt = mItems.size();
			long total = 0;
			for(int i=cnt-1;i>=0;--i)
				total += mItems.get(i).mBudgetAmount;
			return total;
		}

		@Override
		public int getBudgetCount() {
			return 1;
		}

		@Override
		public boolean getBudgetItem(int year, int month, int position,
				RRBudgetItemData budgetData) {
			if(mItems == null)
				return false;
			
			budgetData.mBudgetAmount = mItems.get(position).mBudgetAmount;
			budgetData.mBudgetName = mItems.get(position).mBudgetName;
			return true;
		}

		@Override
		public int getBudgetItemCount(int year, int month) {
			if(mItems == null)
				return 0;
			
			return mItems.size();
		}
		
		
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        RRBudgetView stmView = (RRBudgetView) findViewById(R.id.budget_view);
        stmView.setBudgetDataProvider(new SampleBudgetDataProvider(2009, 8));
        
        /*
        RRMonthBudgetView view = (RRMonthBudgetView) findViewById(R.id.month_budget_view);
        view.setMonthBudgetDataProvider(new SampleBudgetDataProvider(2009, 8));
        */
        
        /*RRBudgetInputDialog dlg = new RRBudgetInputDialog(this);
        dlg.show();*/
    }
}