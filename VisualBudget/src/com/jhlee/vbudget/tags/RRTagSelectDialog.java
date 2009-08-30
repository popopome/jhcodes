package com.jhlee.vbudget.tags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.tags.RRTagsListView.OnTagStateChangeListener;
import com.jhlee.vbudget.tags.RRTagsListView.RRTagDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRTagSelectDialog extends Dialog {
	private RRTagsListView mTagsListView;
	private Button mNewButton;
	private Button mDoneButton;
	private Button mCancelButton;
	private RRTagDataProvider mProvider;
	private boolean mIsCanceled = false;
	private boolean mIsTagChanged = false;

	public RRTagSelectDialog(Context context) {
		super(context);
		this.setTitle("SELECT TAG");
	}

	public boolean initialize(RRTagDataProvider provider) {
		mProvider = provider;

		/* Create views */
		View view = RRUtil.createViewsFromLayout(this.getContext(),
				R.layout.tag_select_dialog, null);
		

		/* Initialize tags list view */
		mTagsListView = (RRTagsListView) view.findViewById(R.id.tags_list_view);
		mTagsListView.initialize(provider);
		mTagsListView.setOnTagStateChangeListener(new RRTagsListView.OnTagStateChangeListener() {
			@Override
			public void onTagStateChanged(View view, String tag, boolean checked) {
				mIsTagChanged = true;
			}
		});
		

		mNewButton = (Button) view.findViewById(R.id.button_new_tag);
		mDoneButton = (Button) view.findViewById(R.id.button_done);
		mCancelButton = (Button)view.findViewById(R.id.button_cancel);
		
		/* Cancel button is clicked */
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsCanceled = true;
				RRTagSelectDialog.this.cancel();
			}
		});

		/* Done button is clicked */
		mDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* If nothing is changed,
				 * here program considers this case as canceled one.
				 */
				if(mIsCanceled == false && mIsTagChanged == false) {
					mIsCanceled = false;
				}
				RRTagSelectDialog.this.dismiss();
			}
		});
		

		/* New button is clicked */
		mNewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final View view = RRUtil.createViewsFromLayout(
						RRTagSelectDialog.this.getContext(),
						R.layout.tag_new_entry, null);

				new AlertDialog.Builder(RRTagSelectDialog.this.getContext())
						.setTitle("Create new tag").setView(view)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* User clicked OK so do some stuff */
										EditText tagNameEdit = (EditText) view
												.findViewById(R.id.tag_name_edit);
										String tagName = tagNameEdit.getText()
												.toString();
										RRTagSelectDialog.this
												.onNewTagEntered(tagName);
									}
								}).setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* Do nothing */
									}
								}).create().show();
			}
		});
		
		this.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		
		return true;
	}
	
	/*
	 * Check canceled.
	 */
	public boolean isCanceled() {
		return mIsCanceled;
	}

	/*
	 * New tag entered. This function is from new tag name dialog
	 */
	private void onNewTagEntered(String tagName) {
		tagName.trim();
		if (tagName.length() == 0) {
			Toast.makeText(this.getContext(), "Please enter tag name",
					Toast.LENGTH_LONG).show();
			return;
		}

		/* Add to provider */
		if (false == mProvider.addTag(tagName, true)) {
			Toast.makeText(this.getContext(), "Unable to insert tag to db",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		/* Refill tag list */
		mTagsListView.refreshData();
		mTagsListView.scrollToTag(tagName);
	}
}
