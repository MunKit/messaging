package com.example.munkit.messaging.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.Gravity;

import com.example.munkit.messaging.R;
import com.example.munkit.messaging.model.ReceivedMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class MessageListItemAdapter extends ArrayAdapter<ReceivedMessage>{

    private final Context context;
    private final ArrayList<ReceivedMessage> messages;
    private boolean align;

    public MessageListItemAdapter(Context context, ArrayList<ReceivedMessage> messages , boolean align){
        super(context, R.layout.message_list_item, messages);
        this.context = context;
        this.messages = messages;
        this.align = align;

    }
    public void setalign(boolean side)
    {
        this.align = side;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.message_list_item, parent, false);
        TextView topicTextView = (TextView) rowView.findViewById(R.id.message_topic_text);
        TextView messageTextView = (TextView) rowView.findViewById(R.id.message_text);
        TextView dateTextView = (TextView) rowView.findViewById(R.id.message_date_text);
        try {
            String[] msg = new String(messages.get(position).getMessage().getPayload()).split("@");
            messageTextView.setText(msg[1]);
            //topicTextView.setText(context.getString(R.string.topic_fmt, messages.get(position).getTopic()));
            topicTextView.setText(context.getString(R.string.topic_fmt, msg[0]));
            DateFormat dateTimeFormatter = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            String shortDateStamp = dateTimeFormatter.format(messages.get(position).getTimestamp());
            dateTextView.setText(context.getString(R.string.message_time_fmt, shortDateStamp));
            if (!msg[0].equals("Me")) {
                messageTextView.setGravity(Gravity.LEFT);
                System.out.println("set to left gravity ");
            }
            else {
                messageTextView.setGravity(Gravity.RIGHT);
                messageTextView.setText(msg[2]);
                topicTextView.setText(context.getString(R.string.topicme_fmt, messages.get(position).getTopic()));
                System.out.println("set to right gravity");
            }
            return rowView;
        }
        catch(Exception e)
        {
            System.out.println("Exception occurred");
        }
        return rowView;
    }
}
