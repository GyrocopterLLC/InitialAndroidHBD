package com.example.david.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import android.view.View.OnClickListener;
import android.widget.Toast;

//public class BluetoothDeviceViewAdapter extends ArrayAdapter<BluetoothDeviceViewModel> implements View.OnClickListener {
public class BluetoothDeviceViewAdapter extends ArrayAdapter<BluetoothDeviceViewModel> {

    private ArrayList<BluetoothDeviceViewModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtMac;
        ImageView imgIcon;
    }

    public BluetoothDeviceViewAdapter(ArrayList<BluetoothDeviceViewModel> data, Context context) {
        super(context, R.layout.listview_tworowitem, data);
        this.dataSet = data;
        this.mContext = context;
    }

    /**
    @Override
    public void onClick(View v) {

        int position = (Integer)v.getTag();
        Object object = getItem(position);
        BluetoothDeviceViewModel btdmodel = (BluetoothDeviceViewModel)object;

        switch(v.getId())
        {
            case R.id.textViewName:
                Toast.makeText(v.getContext(), "Name "+btdmodel.getName(),Toast.LENGTH_LONG).show();
                break;
        }
    }
    **/

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        BluetoothDeviceViewModel dataModel = getItem(position);
        ViewHolder vh;
        final View result;

        if(convertView == null) {
            vh = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_tworowitem, parent, false);
            vh.txtName = (TextView) convertView.findViewById(R.id.textViewName);
            vh.txtMac = (TextView) convertView.findViewById(R.id.textViewMac);
            vh.imgIcon = (ImageView) convertView.findViewById(R.id.btIconView);

            result = convertView;
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        vh.txtName.setText(dataModel.name);
        vh.txtMac.setText(dataModel.mac);
        vh.imgIcon.setImageResource(R.drawable.ic_bluetooth_black_24dp);

        return convertView;
    }
}
