package com.jhlee.tagsample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.jhlee.tagsample.RRTagStreamView.RRTagDataProvider;

public class RRTagBox extends RelativeLayout {
	private static String TAG = "RRTagBox";

	private Button 	mAddBtn;
	private Button	mCloseBtn;
	private EditText	mNewTagEdit;
	private RRTagStreamView	mTagStreamView;
	private RRTagDataProvider	mTagDataProvider;
	
	public RRTagBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		buildLayout();
	}

	public RRTagBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildLayout();
	}

	public RRTagBox(Context context) {
		super(context);
		buildLayout();
	}

	private void buildLayout() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.tagbox, this, true);
		
		mAddBtn = (Button)findViewById(R.id.tag_add);
		mCloseBtn = (Button)findViewById(R.id.tag_box_close);
		mNewTagEdit = (EditText)findViewById(R.id.tag_edit);
		mTagStreamView = (RRTagStreamView)findViewById(R.id.tag_stream_view);

		/*
		 * Tag addition button is clicked
		 */
		mAddBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = mNewTagEdit.getText().toString();
				if(text.length() == 0)
					return;
				
				/* Add tag with checked status */
				boolean succeeded = mTagDataProvider.addTag(text, true);
				if(succeeded == false) {
					Log.e(TAG, "Unable to add tag:" + text);
					return;
				}
				mTagStreamView.refreshTags();
				
				/* Clear tag addition view */
				mNewTagEdit.setText("");
			}
		});
		
		/* CLOSE BUTTON
		 * If close button is clicked, then make view.
		 */
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RRTagBox.this.setVisibility(GONE);
			}
		});
	}
	
	public void setTagProvider(RRTagDataProvider provider) {
		mTagDataProvider = provider;
		mTagStreamView.setTagProvider(provider);
	}
}
