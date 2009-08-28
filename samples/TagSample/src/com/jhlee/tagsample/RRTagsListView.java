package com.jhlee.tagsample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.jhlee.tagsample.RRTagStreamView.RRTagDataProvider;

/*
 * Show all tags and provides selection
 */
public class RRTagsListView extends ListView {

	private RRTagDataProvider mProvider;
	
	public RRTagsListView(Context context) {
		this(context, null);
	}

	public RRTagsListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RRTagsListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/*
	 * Initialize tags view
	 */
	public void initialize(RRTagDataProvider provider) {
		mProvider = provider;
		this.setAdapter(new RRTagsGridAdapter());
	}

	
	/*
	 * Data adapter 
	 */
	private class RRTagsGridAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mProvider.getCount();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RRTagTextView tagTextView = (RRTagTextView)convertView;
			String tagString = mProvider.getTag(position);
			if(tagTextView == null) {
				tagTextView = new RRTagTextView(RRTagsListView.this.getContext());
				tagTextView.setTagText(tagString);
			} else {
				tagTextView.setTagText(tagString);
			}
			
			if(mProvider.isChecked(position)) {
				tagTextView.check();
			} else {
				tagTextView.uncheck();
			}
			
			return tagTextView;
		}
		
	}
	
	public interface OnTagItemStateChangeListener {
		public void onTagItemStateChanged(String tag, boolean checked);
	}
}
