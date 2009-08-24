package com.jhlee.chartsample;

import com.jhlee.chartsample.RRChartBarStreamView.RRChartBarDataProvider;

import android.app.Activity;
import android.os.Bundle;

public class ChartSample extends Activity {
	private class SampleChartDataProvider implements RRChartBarDataProvider {
		private int[] mValues = new int[]{10, 20, 30, 40, 50, 40, 30, 20, 10 };
		private int mMaxValues = 200;
		@Override
		public int getBarMaxValue() {
			return mMaxValues;
		}

		@Override
		public String getBarTitle(int position) {
			return "$" + Integer.toString(mValues[position]);
		}

		
		@Override
		public String getBarValueName(int position) {
			return "08-03-2009";
		}

		@Override
		public int getBarValue(int position) {
			return mValues[position];
		}

		@Override
		public int getCount() {
			return mValues.length;
		}
		
	}
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
        RRChartBarView barView = (RRChartBarView) findViewById(R.id.bar_view);
        barView.setData("$38.00", 200, 38);*/
        
        /*RRChartBarStreamView streamView = (RRChartBarStreamView)findViewById(R.id.bar_stream_view);
        streamView.setBarWidth(80);
        streamView.setBarValueNameTextSize(9);
        streamView.setTitleTextSize(20);
        streamView.setChartBarDataProvider(new SampleChartDataProvider());
        
        RRChartYAxisView yAxisView = (RRChartYAxisView)findViewById(R.id.chart_y_axis);
        yAxisView.setYAxisName("Money");*/
        
        RRChartBarGraph graph = (RRChartBarGraph)findViewById(R.id.bar_graph);
        graph.setBarWidth(80);
        graph.setBarValueNameTextSize(9);
        graph.setTitleTextSize(20);
        graph.setChartBarDataProvider(new SampleChartDataProvider());
        graph.setXYAxisName("Date", "Money");
        graph.setGraphTitle("Day by day view\nGraph for day expenses");
    }
}