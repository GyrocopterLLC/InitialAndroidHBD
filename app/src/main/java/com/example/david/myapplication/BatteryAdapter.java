package com.example.david.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BatteryAdapter extends RecyclerView.Adapter<BatteryAdapter.BatteryViewHolder> {
    private static BatteryClickListener clickListener;
    Float[] mVoltages;
    Integer[] mStatuses;

    protected final float[] battRanges = {
            3.4f, // 20%
            3.5f, // 30%
            3.7f, // 50%
            3.8f, // 60%
            4.0f, // 80%
            4.1f, // 90%
    };

    public class BatteryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Each view contains a battery with icon and voltage
        public TextView mTitle;
        public TextView mVoltage;
        public ImageView mIcon;

        public BatteryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.mTitle = itemView.findViewById(R.id.battTitleText);
            this.mVoltage = itemView.findViewById(R.id.battVoltageText);
            this.mIcon = itemView.findViewById(R.id.battIconView);
        }

        public void setBatteryVoltage(float newVoltage) {
            // Check which icon to use
            if(newVoltage < battRanges[0]) {
                mIcon.setImageResource(R.drawable.ic_battery_20_black_24dp);
            } else if(newVoltage < battRanges[1]) {
                mIcon.setImageResource(R.drawable.ic_battery_30_black_24dp);
            } else if(newVoltage < battRanges[2]) {
                mIcon.setImageResource(R.drawable.ic_battery_50_black_24dp);
            } else if(newVoltage < battRanges[3]) {
                mIcon.setImageResource(R.drawable.ic_battery_60_black_24dp);
            } else if(newVoltage < battRanges[4]) {
                mIcon.setImageResource(R.drawable.ic_battery_80_black_24dp);
            } else if(newVoltage < battRanges[5]) {
                mIcon.setImageResource(R.drawable.ic_battery_90_black_24dp);
            } else {
                mIcon.setImageResource(R.drawable.ic_battery_full_black_24dp);
            }
            // And set the voltage text
            mVoltage.setText(String.format("%.2f",newVoltage));
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
    }

    public void setClickListener(BatteryClickListener bcl) {
        BatteryAdapter.clickListener = bcl;
    }

    public interface BatteryClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public BatteryAdapter(Float[] voltages, Integer[] statuses) {
        mVoltages = voltages;
        mStatuses = statuses;

    }

    // Create new views (invoked by layout manager)
    @Override
    public BatteryAdapter.BatteryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_battery, parent,
                false);
        BatteryViewHolder vh = new BatteryViewHolder(v);
        return vh;
    }

    // Replace contents of a view
    @Override
    public void onBindViewHolder(BatteryViewHolder holder, int position) {
        // - get element from dataset at this position
        // - replace contents of view with data
        holder.setBatteryVoltage(mVoltages[position]);
        holder.mTitle.setText(String.format("Battery %02d",position+1));
    }

    @Override
    public int getItemCount() {
        return mVoltages.length;
    }

    public void setBattery(float newVolts, int position) {
        mVoltages[position] = newVolts;
        notifyItemChanged(position);
    }
}
