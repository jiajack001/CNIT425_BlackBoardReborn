package com.cnit425.cnit425_blackboard;

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
import java.util.HashMap;

public class TimeSlotArrayAdapter extends ArrayAdapter {
    private final Context context;

    private final ArrayList<String> availabilityTime;
    private final HashMap<String, Integer> availabilitySlot;
    public int checkedPosition = 0;

    public TimeSlotArrayAdapter(Context context, ArrayList<String> avail, HashMap<String,Integer> map){
        super(context,R.layout.listview_custom_layout, avail);
        this.context = context;
        this.availabilityTime = avail;
        this.availabilitySlot = map;
    }

    @SuppressLint({"DefaultLocale", "ResourceAsColor"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //inflate the rowView of ListView
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.listview_custom_layout,parent,false);

        //find the time ticket String
        String timeFrame = availabilityTime.get(position);

        //Define RadioButton text, and onClick behavior (when onClick, notify the adapter to re-inflate the ListView)
        RadioButton rad = rowView.findViewById(R.id.btnRadio);
        rad.setText(timeFrame);
        rad.setChecked(position == checkedPosition);
        rad.setTag(position);
        rad.setOnClickListener(v -> {
            checkedPosition = (int) v.getTag();
            notifyDataSetChanged();
        });

        //Display the number of spots left for registration
        TextView txt = rowView.findViewById(R.id.txtCountLeft);
        Integer num =  availabilitySlot.get(timeFrame);
        txt.setText(String.format("%d/15",num));
            //if the num left is >= 15, disable the radiobutton
        if(num!= null && num >= 15){
            rad.setEnabled(false);
            if(position == checkedPosition){
                rad.setChecked(false);
            }
            rad.setTextColor(R.color.common_google_signin_btn_text_dark_disabled);
            txt.setTextColor(R.color.common_google_signin_btn_text_dark_disabled);
        }

        return rowView;
    }
}
