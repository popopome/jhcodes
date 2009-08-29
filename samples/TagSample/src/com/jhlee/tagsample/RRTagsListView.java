package com.jhlee.tagsample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.jhlee.tagsample.RRTagStreamView.RRTagDataProvider;
import com.jhlee.tagsample.RRTagTextView.OnTagClickListener;

/*
 * Show all tags and provides selection
 */
public class RRTagsListView extends ListView {

	private RRTagDataProvider mProvider;
	private OnTagStateChangeListener mOnTagStateChangeListener;

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
	 * Refresh data
	 */
	public void refreshData() {
		this.setAdapter(new RRTagsGridAdapter());
	}

	public void scrollToTag(String tagName) {
		/* Find tag among views */
		int pos = mProvider.findTag(tagName);
		if (pos == -1)
			return;

		this.setSelection(pos);

	}

	public void setOnTagStateChangeListener(OnTagStateChangeListener listener) {
		mOnTagStateChangeListener = listener;
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
			RRTagTextView tagTextView = (RRTagTextView) convertView;
			String tagString = mProvider.getTag(position);
			if (tagTextView == null) {
				tagTextView = new RRTagTextView(RRTagsListView.this
						.getContext());
				tagTextView.setTagText(tagString);
				tagTextView
						.setOnTagClickListener(new RRTagTextView.OnTagClickListener() {
							@Override
							public void onTagClicked(View view, String tagName,
									boolean checked) {
								int pos = mProvider.findTag(tagName);
								if (pos == -1)
									return;

								if (checked)
									mProvider.check(pos);
								else
									mProvider.uncheck(pos);

								if (mOnTagStateChangeListener != null)
									mOnTagStateChangeListener
											.onTagStateChanged(
													RRTagsListView.this,
													tagName, checked);
							}
						});
			} else {
				tagTextView.setTagText(tagString);
			}

			if (mProvider.isChecked(position)) {
				tagTextView.check();
			} else {
				tagTextView.uncheck();
			}

			return tagTextView;
		}

	}

	public interface OnTagStateChangeListener {
		public void onTagStateChanged(View view, String tag, boolean checked);
	}
}
