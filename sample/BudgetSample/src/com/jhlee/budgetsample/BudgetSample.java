package com.jhlee.budgetsample;

import android.app.Activity;
import android.os.Bundle;

public class BudgetSample extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        RRBudgetInputDialog dlg = new RRBudgetInputDialog(this);
        dlg.show();
    }
}