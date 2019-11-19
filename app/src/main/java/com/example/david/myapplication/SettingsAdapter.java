package com.example.david.myapplication;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final Handler mHandler;

    public interface SettingsTypes {
        public static final int TYPE_8BIT = 0x00;
        public static final int TYPE_16BIT = 0x01;
        public static final int TYPE_32BIT = 0x02;
        public static final int TYPE_FLOAT = 0x03;
    }


    String[] mNames;
    Float[] mValues;
    Integer[] mFormats;
    Boolean[] mChanged;

    public interface SettingsClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    private static SettingsClickListener clickListener;

    public class SettingsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Each view contains a name and a value
        public TextView mVarName;
        public TextView mVarValue;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mVarName = itemView.findViewById(R.id.mcu_var_name);
            mVarValue = itemView.findViewById(R.id.mcu_var_value);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setItem(String name, Float value, Integer format, boolean changed) {
            mVarName.setText(name);
            // Value appearance depends on the format...
            if(format == SettingsTypes.TYPE_FLOAT) {
                // floating point number
//                mVarValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                mVarValue.setText(String.format("%.3f", value));
            } else {
                // all integer types are the same
//                mVarValue.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_SIGNED);
                mVarValue.setText(String.format("%d",value.intValue()));
            }
            if(changed) {
                mVarValue.setTextAppearance(R.style.EditTextChanged);
            } else {
                mVarValue.setTextAppearance(R.style.EditTextUnchanged);
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
    public SettingsAdapter(String[] names, Float[] values, Integer[] formats, Handler handler) {
        mNames = names.clone();
        mValues = values.clone();
        mFormats = formats.clone();
        mHandler = handler;
        mChanged = new Boolean[mNames.length];
        for(int i = 0; i < mChanged.length; i++) {
            mChanged[i] = false;
        }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, final int position) {
        // Set the values in each item
        holder.setItem(mNames[position],mValues[position],mFormats[position],mChanged[position]);
    }

    @Override
    public int getItemCount() {
        return mNames.length;
    }

    public void notifyItemChangedLater(final int position) {
        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            }
        );
    }

    public void setNewValue(float newVal, int position, boolean changed) {
        mValues[position] = newVal;
        mChanged[position] = changed;
        notifyItemChangedLater(position);
    }

}
