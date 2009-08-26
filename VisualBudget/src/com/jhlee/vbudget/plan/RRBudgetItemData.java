package com.jhlee.vbudget.plan;

public class RRBudgetItemData {
	public String mBudgetName;
	public long 	mBudgetAmount;
	public RRBudgetItemData() {}
	public RRBudgetItemData(String name, long amount) {
		mBudgetName = name;
		mBudgetAmount = amount;
	}
	public RRBudgetItemData(RRBudgetItemData rhs ) {
		mBudgetName = rhs.mBudgetName;
		mBudgetAmount = rhs.mBudgetAmount;
	}
};