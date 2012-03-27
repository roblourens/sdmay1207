package com.androidhive.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.NetworkController.NetworkEvent;
import sdmay1207.ais.network.model.Node;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NodeListActivity extends ListActivity implements Observer
{
    private NodeController nc;
    List<String> nodeStrs = new ArrayList<String>();
    List<Node> nodes = new ArrayList<Node>();
    ArrayAdapter<String> listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        nc = ((DashboardApplication) getApplication()).nc;
        nc.addNetworkObserver(this);

        listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, nodeStrs);
        setListAdapter(listAdapter);
        updateList();
    }

    private void updateList()
    {
        nodeStrs.clear();
        nodes.clear();
        for (Node n : nc.getKnownNodes().values())
        {
            nodes.add(n);
            if(n.nodeNum==nc.getMe().nodeNum)
            	nodeStrs.add("Node "+ n+"(Me)");
            else
            	nodeStrs.add("Node " + n);
        }

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                System.out.println("Notifying list adapter");
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent i = new Intent(this, NodeDetailsActivity.class);
        i.putExtra(NodeDetailsActivity.NODE_NUM_KEY, nodes.get(position).nodeNum);
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
        }
    }
}
