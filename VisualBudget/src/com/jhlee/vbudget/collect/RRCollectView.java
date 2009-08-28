package com.jhlee.vbudget.collect;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.util.RRUtil;


public class RRCollectView extends ScrollView {

	private View mFrame;
	
	/*
	 * CTOR
	 */
	public RRCollectView(Context context) {
		this(context, null);
	}
	public RRCollectView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public RRCollectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	
		/* Create view from layout */
		mFrame = (View)RRUtil.createViewsFromLayout(context, R.layout.collect_view, this);
		
		Button cameraBtn = (Button)mFrame.findViewById(R.id.collect_view_camera);
		cameraBtn.setOnClickListener(new OnClickListener() {

			/* Camera button is clicked */
			@Override
			public void onClick(View v) {
				/* Launch capture activity */
				Context ctx = RRCollectView.this.getContext();
				Intent i = new Intent(ctx, RRTakeReceiptActivity.class);
				ctx.startActivity(i);
			}
		});
	}
}
