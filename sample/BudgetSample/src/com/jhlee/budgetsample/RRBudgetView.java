package com.jhlee.budgetsample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RRBudgetView extends LinearLayout {
	
	public interface RRBudgetDataProvider {
		public int getBudgetCount();
		public long getBudgetAmount(int year, int month);
		public boolean getBudgetItem(int year, int month, int position, RRBudgetItemData budgetData);
		public int getBudgetItemCount(int year, int month);
		public boolean deleteBudgetItem(int year, int month, int position);
		public int appendBudgetItem(int year, int month, RRBudgetItemData budgetData);
	};
	
	private RRBudgetDataProvider mProvider;
	
	public RRBudgetView(Context context) {
		this(context, null);
	}
	
	public RRBudgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createViewsFromLayout(R.layout.rr_month_budget, this);
	}
	
	public void setBudgetDataProvider(RRBudgetDataProvider provider) {
		mProvider = provider;
		RRMonthBudgetView monthView = (RRMonthBudgetView) findViewById(R.id.budget_month);
		monthView.setBudgetDataProvider(provider);
	}

	private View createViewsFromLayout(int layoutId, ViewGroup parent) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater) getContext().getSystemService(infService);
		return li.inflate(layoutId, parent, true);
	}
	
	private class RRYearMonthAdapter extends BaseAdapter {

		private static final int	BASE_YEAR = 2009;
		private static final int	BASE_MONTH = 1; 
		/*
		 * Month starts from 2009.01.01 to 20019.01.01
		 */
		@Override
		public int getCount() {
			/* 10 years and 12 months */
			return 120;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/*
		 * Let's generate budget month view at here
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			TextView monthView = new TextView(RRBudgetView.this.getContext());
			
			int year = BASE_YEAR + position / 12;
			int month = BASE_MONTH + position % 12;
			String ymStr = Integer.toString(year) + "." + Integer.toString(month);
			monthView.setText(ymStr);
			return monthView;
		}
		
		
		
	}
}
