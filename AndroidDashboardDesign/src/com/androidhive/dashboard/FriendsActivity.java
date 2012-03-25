package com.androidhive.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ListView;
import androidhive.dashboard.R;

public class FriendsActivity extends Activity {
	 /** Called when the activity is first created. */
	ListView lstView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	  super.onCreate(savedInstanceState);
	        setContentView(R.layout.friends_layout);
	        Button btn = (Button) findViewById(R.id.btn);


	        btn.setOnTouchListener(new OnTouchListener() {


				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					// TODO Auto-generated method stub
					setContentView(R.layout.news_feed_layout);
					return false;
				}

	        });
	        
	        //Add code to get node heartbeat here or you can make a function to do it
    }
}
