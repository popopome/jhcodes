package com.jhlee.vbudget.plan;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jhlee.vbudget.R;

public class RRBudgetView extends LinearLayout {
	
	private static final int BASE_YEAR	=	2009;
	private static final int BASE_MONTH = 1;
	
	public interface RRBudgetDataProvider {
		public int getBudgetCount();
		public long getBudgetAmount(int year, int month);
		public boolean getBudgetItem(int year, int month, int position, RRBudgetItemData budgetData);
		public int getBudgetItemCount(int year, int month);
		public boolean deleteBudgetItem(int year, int month, int position);
		public int appendBudgetItem(int year, int month, RRBudgetItemData budgetData);
	};
	
	private RRBudgetDataProvider mProvider;
	private LinearLayout	mLayout;
	private RRMonthBudgetView mMonthBudgetView;
	private Gallery mYearMonthGallery;
	
	public RRBudgetView(Context context) {
		this(context, null);
	}
	
	public RRBudgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLayout = (LinearLayout) createViewsFromLayout(R.layout.rr_budget_view, this);
		mMonthBudgetView = (RRMonthBudgetView) mLayout.findViewById(R.id.budget_month);
		mYearMonthGallery = (Gallery)mLayout.findViewById(R.id.year_month_list);
		
		mYearMonthGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				/* Let's compute year/month from position */
				int year = BASE_YEAR + position / 12;
				int month = BASE_MONTH + position % 12;
				
				/* View is changed */
				mMonthBudgetView.setYearMonth(year, month);
				mMonthBudgetView.setBudgetDataProvider(mProvider);
				
				mMonthBudgetView.requestLayout();
				mMonthBudgetView.invalidate();
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
	}
	
	/*
	 * Set data provider
	 */
	public void setBudgetDataProvider(RRBudgetDataProvider provider) {
		mProvider = provider;
		
		mMonthBudgetView.setBudgetDataProvider(provider);
		
		/* Set current year, month */
		Calendar cal = Calendar.getInstance();
		mMonthBudgetView.setYearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
		mYearMonthGallery.setAdapter(new RRYearMonthAdapter());
		
		/* Request layout change */
		this.requestLayout();
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
		public int getCount() {
			/* 10 years and 12 months */
			return 120;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		/*
		 * Let's generate budget month view at here
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			
			TextView monthTextView = new TextView(RRBudgetView.this.getContext());
			monthTextView.setPadding(15, 15, 15, 15);
			
			DisplayMetrics dm = RRBudgetView.this.getResources().getDisplayMetrics();
			float textSize = (float) (dm.scaledDensity * 32.00);
			monthTextView.setTextSize(textSize);
			
			int year = BASE_YEAR + position / 12;
			int month = BASE_MONTH + position % 12;
			String ymStr = Integer.toString(year) + "." + Integer.toString(month);
			monthTextView.setText(ymStr);
			return monthTextView;
		}
		
		
		
	}
}
