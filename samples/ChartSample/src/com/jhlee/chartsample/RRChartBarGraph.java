package com.jhlee.chartsample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jhlee.chartsample.RRChartBarStreamView.RRChartBarDataProvider;

public class RRChartBarGraph extends RelativeLayout {

	private int mGraphHeight = -1;
	private RRChartYAxisView	mYAxisView;
	private RRChartBarStreamView	mBarStreamView;
	
	public RRChartBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildLayout();
	}

	public RRChartBarGraph(Context context) {
		this(context, null);
		
	}
	
	private void buildLayout() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.rr_chart_bar_graph, this, true);
		
		mYAxisView = (RRChartYAxisView) findViewById(R.id.chart_y_axis);
		mBarStreamView = (RRChartBarStreamView)findViewById(R.id.bar_stream_view);
	}
	
	/*
	 * Set chart graph Height
	 */
	private void setGraphHeight(int graphHeight) {
		mGraphHeight = graphHeight;
		requestLayout();
	}

	/*
	 * Measure dimension
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		/*
		 * Gallery does not support left-aligned view.
		 * It has always center-aligned view. 
		 * Hence we multiply its width size two times. 
		 */
		int w = mYAxisView.getWidth() + mBarStreamView.getWidth()*2;
		if(mGraphHeight > 0) {
			setMeasuredDimension(w, mGraphHeight);	
		}/* else {
			setMeasuredDimension(w, getMeasuredHeight());
		}*/
	}

	
	/*
	 * Set axis name
	 */
	public void setXYAxisName(String xAxisName, String yAxisName) {
		mYAxisView.setYAxisName(yAxisName);
	}
	
	public void setBarColors(int barColor, int barEdgeColor) {
		mBarStreamView.setBarColor(barColor, barEdgeColor);
		mYAxisView.setLineColor(barEdgeColor);
	}
	
	/*
	 * Set default bar value name text size.
	 */
	public void setBarValueNameTextSize(int textSize) {
		mBarStreamView.setBarValueNameTextSize(textSize);
	}
	/* 
	 * Title text size
	 */
	public void setTitleTextSize(int textSize) {
		mBarStreamView.setTitleTextSize(textSize);
	}

	public void setChartBarDataProvider(RRChartBarDataProvider dataProvider) {
		mBarStreamView.setChartBarDataProvider(dataProvider);
	}

	/*
	 * Set bar width
	 */
	public void setBarWidth(int barWidth) {
		mBarStreamView.setBarWidth(barWidth);
	}
	
	public void setGraphTitle(String graphTitle) {
		TextView titleView = (TextView) findViewById(R.id.chart_title);
		titleView.setText(graphTitle);
		requestLayout();
	}
	
}
