package com.jhlee.textblur;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class TextBlurSample extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        R2TopicListView v = (R2TopicListView)findViewById(R.id.topicList);
        v.addTopic(0, "Take a Receipt", Color.WHITE, Color.BLACK, -1);
        v.addTopic(1, "View Receipts", Color.WHITE, Color.BLACK, -1);
        
        v.setItemClickListener(new R2TopicListView.OnItemClickListener() {

			@Override
			public void onItemClicked(View view, long itemIndex) {
				Toast.makeText(view.getContext(), "Wow...item is clicked", Toast.LENGTH_LONG).show();
			}
        	
        });
    }
}