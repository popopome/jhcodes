package com.jhlee.chartsample;

import android.app.Activity;
import android.os.Bundle;

public class ChartSample extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        RRChartBarView barView = (RRChartBarView) findViewById(R.id.bar_view);
        barView.setData("$38.00", 200, 38);
    }
}