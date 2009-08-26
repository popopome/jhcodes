package com.jhlee.vbudget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jhlee.vbudget.util.RRUtil;

public class CommandBar extends LinearLayout {
	public static final int COMMAND_SHOW_THE_PLAN = 1;
	public static final int COMMAND_DAILY_CAROUSEL = 2;
	public static final int COMMAND_DETAIL_VIEW = 3;
	public static final int COMMAND_DAILY_STATISTICS = 4;
	public static final int COMMAND_CAMERA = 1024;

	public interface OnCommandExecuteListener {
		public void onCommandExecute(int cmd);
	}

	private Button mCameraButton;
	private CommandScrollList mCommandList;
	private OnCommandExecuteListener mCommandExecuteListener;

	public CommandBar(Context context) {
		this(context, null);
	}

	public CommandBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		View view = RRUtil.createViewsFromLayout(context,
				R.layout.rr_command_bar, this);
		mCameraButton = (Button) view.findViewById(R.id.camera_button);
		/* Button is clicked */
		mCameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCommandExecuteListener != null) {
					mCommandExecuteListener.onCommandExecute(COMMAND_CAMERA);
				}
			}
		});
		
		
		mCommandList = (CommandScrollList) view.findViewById(R.id.command_list);

		/*
		 * Command item is clicked
		 */
		mCommandList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long id) {
						int command = 0;
						switch (position) {
						case 1:
							command = COMMAND_SHOW_THE_PLAN;
							break;
						}

						if (mCommandExecuteListener != null)
							mCommandExecuteListener.onCommandExecute(command);
					}
				});
	}

	public void setOnCommandExecuteListener(OnCommandExecuteListener listener) {
		mCommandExecuteListener = listener;
	}
}
