package com.jhlee.vbudget.plan;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.plan.RRBudgetMainView.RRBudgetDataProvider;

public class RRDbBasedBudgetDataProvider implements RRBudgetDataProvider{
	
	private static final String TAG = "RRDbBasedBudgetDataProvider";

	
	private RRDbAdapter	mDbAdapter;
	private Cursor	mItemCursor;
	private Cursor	mMonthCursor;
	
	/*
	 * CTOR
	 */
	public RRDbBasedBudgetDataProvider(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;
		
		refreshData();
	}
	
	public void refreshData() {
		if(mItemCursor != null) {
			mItemCursor.close();
			mItemCursor = null;
		}
		
		mItemCursor = mDbAdapter.queryAllBudgetItems();
		mMonthCursor = mDbAdapter.queryMonthBudgets();
	}
	
	private void requery() {
		mItemCursor.requery();
		mMonthCursor.requery();
	}
	
	/*
	 * Append budget item
	 */
	@Override
	public int appendBudgetItem(int year, int month, RRBudgetItemData budgetData) {
		mDbAdapter.insertBudgetItem(year, month, budgetData.mBudgetName, budgetData.mBudgetAmount);
	
		requery();
		return 0;
	}

	/*
	 * Delete budget item
	 */
	@Override
	public boolean deleteBudgetItem(int year, int month, String budgetName) {
		long id = mDbAdapter.findBudgetItem(year, month, budgetName);
		if(-1 == id) {
			Log.e(TAG, "Unable to find budget:budgetName=" + budgetName);
			return false;
		}
		boolean bresult = mDbAdapter.deleteBudgetItem(id);
		if(bresult)
			requery();
		return bresult;
	}

	/*
	 * Move given cursor to matched year/month position 
	 */
	private boolean moveCursorToYearMonth(Cursor c, int year, int month) {
		c.moveToFirst();
		
		int y = 0;
		int m = 0;
		while(c.isAfterLast() == false) {
			y = c.getInt(RRDbAdapter.COL_BUDGET_YEAR);
			m = c.getInt(RRDbAdapter.COL_BUDGET_MONTH);
			if(y == year && m == month) {
				break;
			}
			c.moveToNext();
		}
		
		/* Did we find year.month? */
		if(c.isAfterLast())
			return false;
		
		return true;
	}

	/*
	 * Get total budget amount
	 */
	@Override
	public long getBudgetAmount(int year, int month) {
		
		Cursor c = mMonthCursor;
		if(false ==moveCursorToYearMonth(c, year, month))
			return 0;
		
		return c.getLong(RRDbAdapter.COL_BUDGET_MONTH_AMOUNT_SUM);
	}

	@Override
	public int getBudgetMonthCount() {
		return mMonthCursor.getCount();
	}

	@Override
	public boolean getBudgetItem(int year, int month, int position,
			RRBudgetItemData budgetData) {
		Cursor c = mItemCursor;
		if(false == moveCursorToYearMonth(c, year, month)) {
			/* Not found */
			return false;
		}
		
		c.move(position);
		budgetData.mBudgetName = c.getString(RRDbAdapter.COL_BUDGET_NAME);
		budgetData.mBudgetAmount = c.getLong(RRDbAdapter.COL_BUDGET_AMOUNT);
		budgetData.mBudgetBalance = c.getLong(RRDbAdapter.COL_BUDGET_BALANCE);
		
		return false;
	}

	@Override
	public int getBudgetItemCount(int year, int month) {
		Cursor c = mMonthCursor;
		if(false ==moveCursorToYearMonth(c, year, month))
			return 0;
		
		return c.getInt(RRDbAdapter.COL_BUDGET_MONTH_ITEM_COUNT);
	}

	/*
	 * Get default budget names
	 */
	@Override
	public void getDefaultBudgetNames(ArrayList<String> budgetNames) {
		Cursor c = mDbAdapter.queryAllDefaultBudgetNames();
		while(c.isAfterLast() == false) {
			budgetNames.add(c.getString(0));
			c.moveToNext();
		}
	}

	/*
	 * Update budget item
	 */
	@Override
	public boolean updateBudgetItem(int year, int month, 
			RRBudgetItemData budgetData) {
		if(false ==moveCursorToYearMonth(mItemCursor, year, month))
			return false;
		
		return mDbAdapter.updateBudgetItem(year, month,budgetData.mBudgetName, budgetData.mBudgetAmount); 
	}
	
	/*
	 * Find budget name
	 */
	public boolean findBudgetItem(int year, int month, String budgetName) {
		long id = mDbAdapter.findBudgetItem(year, month, budgetName);
		return (id != -1);
	}

	/*
	 * Check whether budget item is actually used or not
	 */
	@Override
	public boolean isBudgetItemUsed(int year, int month, String budgetName) {
		return mDbAdapter.isBudgetItemUsed(year, month, budgetName);
	}
	
	
}
