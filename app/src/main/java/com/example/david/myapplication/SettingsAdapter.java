package com.example.david.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public interface SettingsTypes {
        public static final int TYPE_8BIT = 0x00;
        public static final int TYPE_16BIT = 0x01;
        public static final int TYPE_32BIT = 0x02;
        public static final int TYPE_FLOAT = 0x03;
    }


    String[] mNames;
    Float[] mValues;
    Integer[] mFormats;

    public interface SettingsClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    private static SettingsClickListener clickListener;

    public class SettingsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Each view contains a name and a value
        public TextView mVarName;
        public EditText mVarValue;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.mVarName = itemView.findViewById(R.id.mcu_var_name);
            this.mVarValue = itemView.findViewById(R.id.mcu_var_value);
        }

        public void setItem(String name, Float value, Integer format) {
            mVarName.setText(name);
            // Value depends on the format...
            if(format == SettingsTypes.TYPE_FLOAT) {
                // floating point number
                mVarValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                mVarValue.setText(String.format("%.3f", value));
            } else {
                // all integer types are the same
                mVarValue.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_SIGNED);
                mVarValue.setText(String.format("%d",value.intValue()));
            }
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

    public void setClickListener(SettingsClickListener scl) {
        SettingsAdapter.clickListener = scl;
    }

    // Constructor
    public SettingsAdapter(String[] names, Float[] values, Integer[] formats) {
        mNames = names;
        mValues = values;
        mFormats = formats;
    }

    // Create new views (invoked by layout manager)
    @Override
    public SettingsAdapter.SettingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_mcuvariable, parent,
                false);
        SettingsViewHolder vh = new SettingsViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        // Set the values in each item
        holder.setItem(mNames[position],mValues[position],mFormats[position]);
    }

    @Override
    public int getItemCount() {
        return mNames.length;
    }

    public void setNewValue(float newVal, int position) {
        mValues[position] = newVal;
        notifyItemChanged(position);
    }

}
