package com.codisats.blower;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import static android.view.LayoutInflater.from;

public class ListViewAdapter extends ArrayAdapter<Message> {
    public ListViewAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(position == 0){
            return LayoutInflater.from(this.getContext()).inflate(R.layout.new_message, parent, false);
        }

        convertView = LayoutInflater.from(this.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

        Message message = getItem(position);

        TextView title = (TextView) convertView.findViewById(android.R.id.text1);

        title.setText(message.getTitle());

        return convertView;
    }
}
