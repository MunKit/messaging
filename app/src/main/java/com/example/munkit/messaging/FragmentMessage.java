package com.example.munkit.messaging;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.munkit.messaging.components.MessageListItemAdapter;
import com.example.munkit.messaging.internal.Connections;
import com.example.munkit.messaging.internal.IReceivedMessageListener;
import com.example.munkit.messaging.model.ReceivedMessage;
import com.example.munkit.messaging.model.Subscription;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Map;


public class FragmentMessage extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private MessageListItemAdapter messageListAdapter;
    private ArrayList<ReceivedMessage> messages;
    private ListView messageHistoryListView;
    private Connection connection;
    private View rootView;
    private ArrayList<Subscription> subscriptions;
    public FragmentMessage() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<String, Connection> connections = Connections.getInstance(this.getActivity())
                .getConnections();
        //Connection connection = connections.get(this.getArguments().getString(ActivityConstants.CONNECTION_KEY));
        connection = connections.get(this.getArguments().getString(ActivityConstants.CONNECTION_KEY));
        System.out.println("History Fragment: " + connection.getId());
        setHasOptionsMenu(true);
        messages = connection.getMessages();
        subscriptions = connection.getSubscriptions();
        //disable send button when no car subcribed
        //Button clearButton = (Button) findViewById(R.id.Send_button);
        /*if (subscriptions.size()==0)
            clearButton.setEnabled(false);
        else
            clearButton.setEnabled(true);*/
        connection.addReceivedMessageListner(new IReceivedMessageListener() {
            @Override
            public void onMessageReceived(ReceivedMessage message) {
                System.out.println("GOT A MESSAGE in history " + new String(message.getMessage().getPayload()));
                System.out.println("M: " + messages.size());
                //messageHistoryListView.smoothScrollToPosition(messages.size()-1);
                messageListAdapter.notifyDataSetChanged();
                messageHistoryListView.smoothScrollToPosition(messageHistoryListView.getMaxScrollAmount());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_message, container, false);

        messageListAdapter = new MessageListItemAdapter(getActivity(), messages, true);
        messageHistoryListView = (ListView) rootView.findViewById(R.id.history_list_view);
        messageHistoryListView.setAdapter(messageListAdapter);

        Button clearButton = (Button) rootView.findViewById(R.id.Send_button);
        if (subscriptions.size()==0)
            clearButton.setEnabled(false);
        else
            clearButton.setEnabled(true);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mytopic = subscriptions.get(0).getTopic();
                EditText topicText = (EditText) rootView.findViewById(R.id.pub_vehicle);
                EditText messageText = (EditText) rootView.findViewById(R.id.pub_message);
                String ptopic = topicText.getText().toString();
                String pmsg = mytopic+"@"+messageText.getText().toString();//add subscriplist
                connection.addMessages(ptopic, pmsg);
                try {
                    connection.getClient().publish(ptopic, pmsg.getBytes(), 0, false);
                }
                catch (MqttException e)
                {
                    e.printStackTrace();
                }
                //Connection.messageHistory.add(newmsg);
                messageListAdapter.setalign(false);
                messageListAdapter.notifyDataSetChanged();
                messageHistoryListView.smoothScrollToPosition(messageHistoryListView.getMaxScrollAmount());
                //messageListAdapter.setalign(true);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

}
