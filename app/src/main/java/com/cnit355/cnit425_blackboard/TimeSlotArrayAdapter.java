package com.cnit355.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TimeSlotArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final static String[] timeSlot = new String[]{
            "8:00-9:00","9:00-10:00","10:00-11:00","11:00-12:00","12:00-13:00",
            "13:00-14:00","14:00-15:00","15:00-16:00","16:00-17:00"};

    private final ArrayList<Integer> availability;
    private int checkedPosition;

    public TimeSlotArrayAdapter(Context context, ArrayList<Integer> avail){
        super(context,R.layout.listview_custom_layout, avail);
        this.context = context;
        this.availability = avail;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.listview_custom_layout,parent,false);

        RadioButton rad = rowView.findViewById(R.id.btnRadio);
        rad.setText(timeSlot[position]);
        rad.setChecked(position == checkedPosition);
        rad.setTag(position);
        rad.setOnClickListener(v -> {
            checkedPosition = (int) v.getTag();
            notifyDataSetChanged();
        });

        TextView txt = rowView.findViewById(R.id.txtCountLeft);
        int num = (int) availability.get(position);
        txt.setText(String.format("%d/15",num));

        if(num >= 15){
            rad.setEnabled(false);
        }

        return rowView;
    }
}
