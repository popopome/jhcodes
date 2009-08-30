package com.jhlee.vbudget.statistics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jhlee.vbudget.R;
import com.jhlee.vbudget.statistics.RRChartBarStreamView.RRChartBarDataProvider;
import com.jhlee.vbudget.util.RRUtil;

public class RRChartBarGraph extends RelativeLayout {

	private RRChartYAxisView	mYAxisView;
	private RRChartBarStreamView	mBarStreamView;
	private TextView mEmptyView;
	private TextView mTitleView;
	private LinearLayout mGraphWrapper;
	
	private RRChartBarDataProvider	mDataProvider;
	
	public RRChartBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildLayout();
	}

	public RRChartBarGraph(Context context) {
		this(context, null);
		
	}
	
	private void buildLayout() {
		RRUtil.createViewsFromLayout(this.getContext(), R.layout.rr_chart_bar_graph, this);
		
		mYAxisView = (RRChartYAxisView) findViewById(R.id.chart_y_axis);
		mBarStreamView = (RRChartBarStreamView)findViewById(R.id.bar_stream_view);
		mEmptyView = (TextView)findViewById(R.id.empty_view);
		mTitleView = (TextView)findViewById(R.id.chart_title);
		mGraphWrapper = (LinearLayout)findViewById(R.id.graph_wrapper);
	}
	
	/*
	 * Set chart graph Height
	 */
	public void setBarMaxHeight(long graphHeight) {
		mBarStreamView.setBarHeight(graphHeight);
		requestLayout();
	}
	
	public void refreshData() {
		/* Refresh data provider */
		mDataProvider.refreshData();
		setBarMaxHeight(mDataProvider.getBarMaxValue());
		
		/* Refresh stream internal data */
		mBarStreamView.refreshData();
	}

	/*
	 * Measure dimension
	 */
	/*@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mGraphHeight > 0) {
			setMeasuredDimension(getMeasuredWidth(), mGraphHeight);	
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(mGraphHeight > 0) {
			setMeasuredDimension(getMeasuredWidth(), mGraphHeight);	
		}
	}*/

	
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
		mDataProvider = dataProvider;
		mBarStreamView.setChartBarDataProvider(dataProvider);
		
		/*
		 * Check if there is no data,
		 * we only show empty data
		 */
		if(dataProvider.getCount() == 0) {
			mGraphWrapper.setVisibility(View.GONE);
			mTitleView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mGraphWrapper.setVisibility(View.VISIBLE);
			mTitleView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
		}
		
		requestLayout();
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
	
	public void setEmptyText(String emptyText) {
		mEmptyView.setText(emptyText);
		requestLayout();
	}
	
}
