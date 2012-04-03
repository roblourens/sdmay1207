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
    private boolean directNeighborsOnly;

    public static final String DIRECT_NEIGHBORS_ONLY_KEY = "neighorsOnly";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        directNeighborsOnly = getIntent().getBooleanExtra(
                DIRECT_NEIGHBORS_ONLY_KEY, false);

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
        // I thought we could modify the data backing the adapter from any
        // thread as long as we notified the adapter from the UI thread but I
        // got an exception that says otherwise, must be a weird race condition,
        // so let's try this for a while and keep it in mind for the other parts
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                nodeData.clear();
                nodes.clear();

                if (directNeighborsOnly)
                {
                    for (Node n : nc.getNeighborNodes().values())
                        addNodeToList(n, "Node " + n.nodeNum);
                } else
                {
                    for (Node n : nc.getKnownNodes().values())
                    {
                        String tag = "";
                        if (n.nodeNum == nc.getMe().nodeNum)
                            tag = "(Me)";
                        else if (!nc.getNodesInNetwork().containsKey(n.nodeNum))
                            tag = "(Left)";

                        addNodeToList(n, "Node " + n.nodeNum + " " + tag);
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            public void addNodeToList(Node n, String displayStr)
            {
                nodes.add(n);
                Map<String, String> rowMap = new HashMap<String, String>();
                rowMap.put("displayStr", displayStr);

                String lastHbStr = "";
                if (n.lastHeartbeat != null)
                {
                    Date d = new Date(n.lastHeartbeat.timestamp);
                    String formattedDate = new SimpleDateFormat("H:mm:ss")
                            .format(d);
                    lastHbStr = "Last heartbeat: " + formattedDate;
                } else
                    lastHbStr = "No heartbeats";

                rowMap.put("timestamp", lastHbStr);

                nodeData.add(rowMap);
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
    // could add a callback for when a node joins/leaves as a neighbor, but
    // RecvdHeartbeat should be good enough - the node will be displayed
    // correctly if needed each time updateList is called
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
