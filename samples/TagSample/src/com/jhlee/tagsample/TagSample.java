package com.jhlee.tagsample;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.jhlee.tagsample.R;
import com.jhlee.tagsample.RRTagsListView;
import com.jhlee.tagsample.RRTagStreamView.RRTagDataProvider;

public class TagSample extends Activity {
	
	public class TagProviderImpl implements RRTagDataProvider {
		public ArrayList<String> mTags = new ArrayList<String>();
		public ArrayList<Integer> mCheckTable = new ArrayList<Integer>();
		
		
		public TagProviderImpl() {
			super();
			
			mTags.add("android");
			mTags.add("google");
			mTags.add("apple");
			mTags.add("microsoft");
			mTags.add("windows mobile");
			mTags.add("palm pre");
			mTags.add("iPhone");
			mTags.add("blackberry");
			mTags.add("rim");
			mTags.add("amazon");
			
			int cnt = mTags.size();
			for(int i=0;i<cnt;++i) {
				mCheckTable.add(0);
			}
		}

		@Override
		public int getCount() {
//			return 0;
			return mTags.size();
		}

		@Override
		public String getTag(int index) {
			return mTags.get(index);
		}

		@Override
		public boolean isChecked(int index) {
			return mCheckTable.get(index) != 0;
		}

		@Override
		public void check(int index) {
			mCheckTable.set(index, 1);
		}

		@Override
		public void uncheck(int index) {
			mCheckTable.set(index, 0);
		}

		@Override
		public boolean addTag(String tag, boolean checked) {
			mTags.add(tag);
			mCheckTable.add(checked ? 1 : 0);
			return true;
		}

		@Override
		public int findTag(String tagName) {
			int cnt = mTags.size();
			for(int pos=cnt-1;pos>=0;--pos) {
				if(0 == tagName.compareToIgnoreCase(mTags.get(pos))) {
					return pos;
				}
			}
			
			return -1;
		}
		
	};
	
	private TagProviderImpl	mTagProvider = new TagProviderImpl();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*
        RRTagTextView textView = (RRTagTextView)findViewById(R.id.tag_text_view);
        textView.setTagText("This is tag view");
        textView.showDeleteMark();
        
        RRTagStreamView stmView = (RRTagStreamView)findViewById(R.id.tag_stream_view);
        stmView.setTagProvider(mTagProvider);
        
        RRTagBox tagBox = (RRTagBox)findViewById(R.id.tag_box);
        tagBox.setTagProvider(mTagProvider);*/
        RRTagsListView gridView = (RRTagsListView)findViewById(R.id.tags_grid_view);
        gridView.initialize(mTagProvider);
        
        RRTagSelectDialog dlg = new RRTagSelectDialog(this);
        dlg.initialize(mTagProvider);
        dlg.show();
    }
}