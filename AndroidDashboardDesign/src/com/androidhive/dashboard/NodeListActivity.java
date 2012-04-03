package com.androidhive.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Node;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class NodeListActivity extends ListActivity implements Observer
{
    private NodeController nc;
    private List<Map<String, String>> nodeData = new ArrayList<Map<String, String>>();
    private List<Node> nodes = new ArrayList<Node>();
    private BaseAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        nc = ((DashboardApplication) getApplication()).nc;
        nc.addNetworkObserver(this);

        String[] from = { "displayStr", "timestamp" };
        int[] to = { android.R.id.text1, android.R.id.text2 };

        listAdapter = new SimpleAdapter(this, nodeData,
                android.R.layout.simple_list_item_2, from, to);
        setListAdapter(listAdapter);
        updateList();
    }

    private void updateList()
    {
        nodeData.clear();
        nodes.clear();

        for (Node n : nc.getKnownNodes().values())
        {
            nodes.add(n);

            String tag = "";
            if (n.nodeNum == nc.getMe().nodeNum)
                tag = "(Me)";
            else if (!nc.getNodesInNetwork().containsKey(n.nodeNum))
                tag = "(Left)";
            
            Map<String, String> rowMap = new HashMap<String, String>();
            rowMap.put("displayStr", "Node " + n.nodeNum + " " + tag);
            
            Date d = new Date(n.lastHeartbeat.timestamp);
            String formattedDate = new SimpleDateFormat("H:mm:ss").format(d);
            rowMap.put("timestamp", "Last heartbeat: " + formattedDate);
            
            nodeData.add(rowMap);
        }

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent i = new Intent(this, NodeDetailsActivity.class);
        i.putExtra(NodeDetailsActivity.NODE_NUM_KEY,
                nodes.get(position).nodeNum);
        startActivity(i);
    }

    // event received from the NetworkController
    public void update(Observable observable, Object obj)
    {
        NetworkEvent netEvent = (NetworkEvent) obj;
        switch (netEvent.event)
        {
        case NodeJoined:
            updateList();
            break;
        case NodeLeft:
            updateList();
            break;
        case RecvdHeartbeat:
            updateList();
            break;
        }
    }
}
