package com.jhlee.vbudget;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public interface RRBudgetContent {
	public void refreshContent();
	public void createMenu(Menu menu, MenuInflater inflater);
	public void onMenuItemSelected(MenuItem mi);
}
