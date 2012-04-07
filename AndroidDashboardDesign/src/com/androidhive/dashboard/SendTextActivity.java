package com.androidhive.dashboard;

import java.util.Observable;
import java.util.Observer;

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

import com.androidhive.dashboard.DashboardApplication.NetworkEventNotification;
import com.androidhive.dashboard.DashboardApplication.Notification;

public class SendTextActivity extends Activity implements Observer
{
    private NodeController nc;
    private int nodeNum = 0;

    private DashboardApplication da;
    TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_text);
        da= ((DashboardApplication) getApplication());
        nc = ((DashboardApplication) getApplication()).nc;
        nodeNum = getIntent().getIntExtra(NodeDetailsActivity.NODE_NUM_KEY, 0);

        // set title text
        ((TextView) findViewById(R.id.sendTextTitle))
                .setText("Send a text message to node " + nodeNum);

        // set send button listener
        final Context c = this;
        tx =(TextView)findViewById(R.id.textSent);
        
        //Add previous text message to texbox
        for(int i=da.nm.notifications.size()-1;i>-1;i--)
        {
        	NetworkEventNotification notification= (NetworkEventNotification) da.nm.notifications.get(i);
        	if(notification.isTextMessage())
        	{
        		String msg = notification.netEvent.data.toString();
    			tx.setText("Node"+msg+"\n"+tx.getText());
        	}
        		
        }
        
        
        ((Button) findViewById(R.id.sendButton))
                .setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        String text = ((EditText) findViewById(R.id.textToSend))
                                .getText().toString();
                        String prevtext = ((TextView) findViewById(R.id.textSent))
                                .getText().toString();
                        if(prevtext==null)
                        	prevtext=" ";
                        
                        tx.setText("ME:"+text+"\n"+prevtext);
                                            
                        nc.sendNetworkMessage(new TextMessage(text), nodeNum);
                        
                        Toast.makeText(c, "Text message sent", 3).show();
                    }
                });
    }

	public void update(Observable arg0, Object arg1) {
		if(((Notification)arg1).isTextMessage())
		{
			NetworkEventNotification notification = (NetworkEventNotification) arg1;
			// TextMessage tm = (TextMessage) notification.netEvent.data;
			String msg = notification.netEvent.data.toString();
			
			tx.setText(msg+"\n"+tx.getText());
		}
	}
}
