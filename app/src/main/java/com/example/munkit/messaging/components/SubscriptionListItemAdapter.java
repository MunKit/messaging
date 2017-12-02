package com.example.munkit.messaging.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.munkit.messaging.R;
import com.example.munkit.messaging.model.Subscription;

import java.util.ArrayList;

public class SubscriptionListItemAdapter extends ArrayAdapter<Subscription>{

    private final Context context;
    private final ArrayList<Subscription> topics;
    private final ArrayList<OnUnsubscribeListner> unsubscribeListners = new ArrayList<OnUnsubscribeListner>();
    //private final Map<String, String> topics;

    public SubscriptionListItemAdapter(Context context, ArrayList<Subscription> topics){
        super(context, R.layout.vehicle_list_item, topics);
        this.context = context;
        this.topics = topics;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.vehicle_list_item, parent, false);
        TextView topicTextView = (TextView) rowView.findViewById(R.id.vehicle_num_label);
        ImageView topicDeleteButton = (ImageView) rowView.findViewById(R.id.topic_delete_image);
        topicTextView.setText(topics.get(position).getTopic());

        topicDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (OnUnsubscribeListner listner : unsubscribeListners) {
                    listner.onUnsubscribe(topics.get(position));
                }
                topics.remove(position);
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    public void addOnUnsubscribeListner(OnUnsubscribeListner listner){
        unsubscribeListners.add(listner);
    }

    public interface OnUnsubscribeListner{
        void onUnsubscribe(Subscription subscription);
    }



}
