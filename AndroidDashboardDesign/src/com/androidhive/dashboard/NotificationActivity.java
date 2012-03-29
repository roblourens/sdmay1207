package com.androidhive.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.androidhive.dashboard.DashboardApplication.Notification;

public class NotificationActivity extends ListActivity implements Observer
{
    private DashboardApplication da;
    private ArrayAdapter<String> listAdapter;
    private List<String> notificationStrs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        da = ((DashboardApplication) getApplication());

        for (Notification n : da.nm.notifications)
            notificationStrs.add(n.shortDisplayString());

        listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, notificationStrs);
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

    public void update(Observable observable, Object obj)
    {
        Notification n = (Notification) obj;
        notificationStrs.add(0, n.shortDisplayString());

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
}
