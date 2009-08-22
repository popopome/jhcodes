package com.jhlee.calendarsample;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class CalendarSample extends Activity {
	public static final String TAG = "CALENDARSAMPLE";
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Context ctx = this;
        RRCalendarStreamView view = (RRCalendarStreamView)this.findViewById(R.id.calendarStream);
        view.setClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ctx, R.string.date_is_clicked, Toast.LENGTH_LONG).show();
			}
        });
        
        RRCalendarSelectDialog dlg = new RRCalendarSelectDialog(this);
        dlg.show();
    }
}