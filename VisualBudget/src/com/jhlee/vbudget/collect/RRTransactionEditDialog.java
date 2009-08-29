package com.jhlee.vbudget.collect;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.db.RRDbAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

public class RRTransactionEditDialog extends Dialog {

	private RRDbAdapter	mDbAdapter;
	
	public RRTransactionEditDialog(Context context) {
		super(context);
	}

	public void initialize(RRDbAdapter dbAdapter) {
		mDbAdapter = dbAdapter;
		this.setContentView(R.layout.collect_transaction_edit_dialog);
		
	}
}
