package com.jhlee.tagsample;

import com.jhlee.tagsample.RRTagStreamView.RRTagDataProvider;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class RRTagBox extends RelativeLayout {

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
				mTagDataProvider.addTag(text, true);
				mTagStreamView.refreshTags();
			}
		});
		
		/* CLOSE BUTTON
		 * If close button is clicked, then make view.
		 */
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RRTagBox.this.setVisi
			}
		});
	}
	
	public void setTagProvider(RRTagDataProvider provider) {
		mTagDataProvider = provider;
		mTagStreamView.setTagProvider(provider);
	}
}
