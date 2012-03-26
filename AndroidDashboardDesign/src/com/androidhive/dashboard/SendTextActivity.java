package com.androidhive.dashboard;

import sdmay1207.ais.NodeController;
import sdmay1207.ais.network.model.TextMessage;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidhive.dashboard.R;

public class SendTextActivity extends Activity
{
    private NodeController nc;
    private int nodeNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_text);

        nc = ((DashboardApplication) getApplication()).nc;
        nodeNum = getIntent().getIntExtra(NodeDetailsActivity.NODE_NUM_KEY, 0);

        // set title text
        ((TextView) findViewById(R.id.sendTextTitle))
                .setText("Send a text message to node " + nodeNum);

        // set send button listener
        final Context c = this;
        ((Button) findViewById(R.id.sendButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        String text = ((EditText) findViewById(R.id.textToSend))
                                .getText().toString();
                        nc.sendNetworkMessage(new TextMessage(text), nodeNum);
                        
                        
                        Toast.makeText(c, "Text message sent", 3).show();
                    }
                });
    }
}
