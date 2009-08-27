package com.jhlee.vbudget.plan;

import android.app.Activity;
import android.database.Cursor;

import com.jhlee.vbudget.db.RRDbAdapter;
import com.jhlee.vbudget.plan.RRBudgetView.RRBudgetDataProvider;

public class RRDbBasedBudgetDataProvider implements RRBudgetDataProvider{

	private static final int	COL_ID		= 	0;
	private static final int	COL_YEAR	=	1;
	private static final int	COL_MONTH	=	2;
	private static final int	COL_NAME	=	3;
	private static final int	COL_SUM		=	3;
	private static final int	COL_AMOUNT	=	4;
	private static final int	COL_BUDGET_ITEM_COUNT = 4;
	
	private Activity mOwnerActivity;
	private RRDbAdapter	mDbAdapter;
	private Cursor	mItemCursor;
	private Cursor	mMonthCursor;
	
	/*
	 * CTOR
	 */
	public RRDbBasedBudgetDataProvider(Activity activity, RRDbAdapter dbAdapter) {
		mOwnerActivity = activity;
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
		mOwnerActivity.startManagingCursor(mItemCursor);
		mOwnerActivity.startManagingCursor(mMonthCursor);
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
	public boolean deleteBudgetItem(int year, int month, int position) {
		Cursor c = mItemCursor;
		
		if(false == moveCursorToYearMonth(c, year, month)) {
			/* Not found */
			return false;
		}
		c.move(position);
		
		/* get id */
		int budgetId = c.getInt(RRDbAdapter.COL_BUDGET_ID);
		boolean bresult = mDbAdapter.removeBudgetItem(budgetId);
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
			y = c.getInt(COL_YEAR);
			m = c.getInt(COL_MONTH);
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
		
		return c.getLong(COL_SUM);
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
		budgetData.mBudgetName = c.getString(COL_NAME);
		
		return false;
	}

	@Override
	public int getBudgetItemCount(int year, int month) {
		Cursor c = mMonthCursor;
		if(false ==moveCursorToYearMonth(c, year, month))
			return 0;
		
		return c.getInt(RRDbAdapter.COL_BUDGET_MONTH_ITEM_COUNT);
	}

}
