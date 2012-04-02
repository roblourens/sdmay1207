package com.androidhive.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import com.androidhive.dashboard.DashboardApplication.Notification;

public class NotificationActivity extends ListActivity implements Observer
{
    private DashboardApplication da;
    private BaseAdapter listAdapter;
    private List<Map<String, String>> notificationData = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        da = ((DashboardApplication) getApplication());

        for (Notification n : da.nm.notifications)
            addNotificationToList(n);

        String[] from = { "displayStr", "timestamp" };
        int[] to = { android.R.id.text1, android.R.id.text2 };

        listAdapter = new SimpleAdapter(this, notificationData,
                android.R.layout.simple_list_item_2, from, to);
        setListAdapter(listAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        da.nm.addObserver(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        da.nm.deleteObserver(this);
    }
    
    private void addNotificationToList(Notification n)
    {
        Map<String, String> newNotificationMap = new HashMap<String, String>();
        newNotificationMap.put("displayStr", n.shortDisplayString());

        // format timestamp
        Date d = new Date(n.timestamp);
        String formattedDate = new SimpleDateFormat("H:mm:ss").format(d);
        newNotificationMap.put("timestamp", formattedDate);
        
        notificationData.add(0, newNotificationMap);
    }

    public void update(Observable observable, Object obj)
    {
        Notification n = (Notification) obj;
        addNotificationToList(n);

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
}
