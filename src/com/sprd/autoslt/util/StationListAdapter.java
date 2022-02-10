package com.sprd.autoslt.util;

import java.util.ArrayList;

import com.sprd.autoslt.R;
import com.sprd.autoslt.util.ListAdapter.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<StationItem> mStationLists = new ArrayList<StationItem>();

	public StationListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public StationListAdapter(Context context,
			ArrayList<StationItem> queryStationData) {
		mInflater = LayoutInflater.from(context);
		mStationLists = queryStationData;
		notifyDataSetChanged();
	}

	public void addItem(String name, String value) {
		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
			return;
		}
		StationItem item = new StationItem();
		item.setStationName(name);
		item.setStationResult(value);
		mStationLists.add(item);
		notifyDataSetChanged();
	}

	public void addItem(StationItem item) {
		if (item == null || TextUtils.isEmpty(item.getStationName())
				|| TextUtils.isEmpty(item.getStationResult())) {
			return;
		}
		mStationLists.add(item);
		notifyDataSetChanged();
	}

	public void clearData() {
		mStationLists.clear();
		notifyDataSetChanged();
	}

	public String getItemName(int index) {
		return mStationLists.get(index).getStationName();
	}

	public void setItemResult(int index, String res) {
		mStationLists.get(index).setStationResult(res);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mStationLists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mStationLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.list_item, null);
			holder.textItem = (TextView) convertView.findViewById(R.id.text_item);
			holder.textValue = (TextView) convertView.findViewById(R.id.text_value);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (mStationLists.get(position).getStationResult()
				.equalsIgnoreCase("pass")) {
			holder.textValue.setTextColor(Color.GREEN);
		} else if (mStationLists.get(position).getStationResult()
				.equalsIgnoreCase("nt")) {
			holder.textValue.setTextColor(Color.GRAY);
		} else if (mStationLists.get(position).getStationResult()
				.equalsIgnoreCase("go")) {
			holder.textValue.setTextColor(Color.BLACK);
		} else {
			holder.textValue.setTextColor(Color.RED);
		}

		holder.textItem.setText(mStationLists.get(position).getStationName());
		holder.textValue
				.setText(mStationLists.get(position).getStationResult());
		return convertView;

	}
	
    public static class ViewHolder {
        public TextView textItem;
        public TextView textValue;
    }

}
