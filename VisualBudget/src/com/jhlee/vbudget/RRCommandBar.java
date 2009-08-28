package com.jhlee.vbudget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jhlee.vbudget.util.RRUtil;

public class RRCommandBar extends LinearLayout {
	private static final String	TAG = "RRCommandBar";
	
	public interface OnCommandExecuteListener {
		public void onCommandExecute(int cmdId, String cmdLabel);
	}

	private Gallery mCommandList;
	private OnCommandExecuteListener mCommandExecuteListener;
	private ArrayList<RRCommandSpec>	mCommandSpecs = new ArrayList<RRCommandSpec>();
	private float mTextSize;


	public RRCommandBar(Context context) {
		this(context, null);
	}

	public RRCommandBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		/*
		 * Set up text size
		 */
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		mTextSize = (float) (dm.scaledDensity * 30.3);

		View view = RRUtil.createViewsFromLayout(context,
				R.layout.rr_command_bar, this);
		
		mCommandList = (Gallery) view.findViewById(R.id.command_list);
		mCommandList.setSpacing(2);
		

		/*
		 * Command item is clicked
		 */
		mCommandList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long id) {
						RRCommandSpec cmdSpec = mCommandSpecs.get(position);
						if (mCommandExecuteListener != null)
							mCommandExecuteListener.onCommandExecute(cmdSpec.mCmdId, cmdSpec.mCmdLabel);
					}
				});
	}

	public void addCommandSpec(int cmdId, String cmdLabel) {
		RRCommandSpec spec = new RRCommandSpec(cmdId, cmdLabel);
		mCommandSpecs.add(spec);
	}
	
	public void setOnCommandExecuteListener(OnCommandExecuteListener listener) {
		mCommandExecuteListener = listener;
	}
	
	/*
	 * Let command bar know command specs are updated.
	 * Changed data should be applied into views.
	 * The function leads genearting new views from command specs.
	 */
	public void updatedCommandSpecs() {
		mCommandList.setAdapter(new RRCommandListAdapter());
	}
	
	/*
	 * Set active command
	 * Make the view which has given command id as selected one.
	 */
	public void setActiveCommand(int cmdId) {
		/* Find position by cmd Id */
		ArrayList<RRCommandSpec> s = mCommandSpecs;
		int cnt = s.size();
		int pos = 0;
		for(; pos <cnt; ++pos) {
			if(s.get(pos).mCmdId == cmdId) {
				break;
			}
		}
		if(pos == cnt) {
			Log.e(TAG, "Not found command:cmdId=" + cmdId);
			return;
		}
		
		mCommandList.setSelection(pos, true);
	}
	
	/**
	 * Command spec
	 *
	 */
	private class RRCommandSpec {
		public String	mCmdLabel;
		public int		mCmdId;
		public RRCommandSpec(int id, String label) {
			mCmdId = id;
			mCmdLabel = label;
		}
	};
	
	

	/**
	 * Command list adapter
	 */
	public class RRCommandListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCommandSpecs.size();
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
				 commandText= new TextView(RRCommandBar.this.getContext());
			}

			commandText.setTextSize(mTextSize);
			
			RRCommandSpec cmd = mCommandSpecs.get(position);
			commandText.setText(cmd.mCmdLabel);
			return commandText;
		}
	};
}
