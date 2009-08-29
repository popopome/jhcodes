package com.jhlee.vbudget.plan;

public class RRBudgetItemData {
	public String mBudgetName;
	public long 	mBudgetAmount;
	public long 	mBudgetBalance;
	public RRBudgetItemData() {}
	public RRBudgetItemData(String name, long amount, long balance) {
		mBudgetName = name;
		mBudgetAmount = amount;
		mBudgetBalance = balance;
	}
	public RRBudgetItemData(RRBudgetItemData rhs ) {
		mBudgetName = rhs.mBudgetName;
		mBudgetAmount = rhs.mBudgetAmount;
		mBudgetBalance = rhs.mBudgetBalance;
	}
};