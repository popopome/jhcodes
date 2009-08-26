package com.jhlee.vbudget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

public class CommandScrollList extends Gallery {

	private float mTextSize;
	
	public CommandScrollList(Context context) {
		this(context, null);
	}
	public CommandScrollList(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CommandScrollList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		mTextSize = (float) (dm.scaledDensity * 20.3);
		
		setAdapter(new CommandIconAdapter());
	}
	
	
	
	private class CommandIconAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			TextView  commandText = (TextView)convertView;
			if(null == commandText) {
				 commandText= new TextView(CommandScrollList.this.getContext());
			}

			commandText.setTextSize(mTextSize);
			
			
			String text = "";
			switch(position) {
			case 0:	text = "Home"; break;
			case 1: text = "Plan budgets"; break;
			case 2: text = "Carousel View"; break;
			case 3: text = "Detail View"; break;
			case 4: text = "Daily Statistics"; break;	
			}
			commandText.setText(text);
			
			return commandText;
		}
		
	};
	
}
