package com.sprd.autoslt.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.sprd.autoslt.R;

public class ListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private ArrayList<TestItem> mTestItemList = new ArrayList<TestItem>();
    private boolean mIsGetResult;

    public ListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public ListAdapter(Context context, ArrayList<TestItem> queryData) {
        mInflater = LayoutInflater.from(context);
        mTestItemList = queryData;
        mIsGetResult = true;
        notifyDataSetChanged();
    }

    public void addItem(String name, String value, String note) {
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            return;
        }
        TestItem item = new TestItem();
        item.setTestCase(name);
        item.setTestResult(value);
        item.setmTestNote(note);
        mTestItemList.add(item);
        notifyDataSetChanged();
    }

    public void addItem(TestItem item) {
        if(item == null || TextUtils.isEmpty(item.getTestCase()) || TextUtils.isEmpty(item.getTestResult())) {
            return;
        }
        mTestItemList.add(item);
        notifyDataSetChanged();
    }
    
    public void clearData() {
        mTestItemList.clear();
        notifyDataSetChanged();
    }
    
    public String getItemName(int index){
        return mTestItemList.get(index).getTestCase();
    }
    
    public void setItemResult(int index,String res){
        mTestItemList.get(index).setTestResult(res);
        notifyDataSetChanged();
    }
    
    public void setItemNote(int index,String not){
        mTestItemList.get(index).setTestResult(not);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mTestItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTestItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            if(mIsGetResult) {
                convertView = mInflater.inflate(R.layout.get_result_list_item, null);
                holder.textId = (TextView)convertView.findViewById(R.id.text_id);
//                holder.textNote = (TextView)convertView.findViewById(R.id.text_note);
                holder.textStation = (TextView)convertView.findViewById(R.id.text_station);
            } else {
                convertView = mInflater.inflate(R.layout.list_item, null);
            }
            holder.textItem = (TextView)convertView.findViewById(R.id.text_item);
            holder.textValue = (TextView)convertView.findViewById(R.id.text_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        if(mTestItemList.get(position).getTestResult().equalsIgnoreCase("pass")) {
            holder.textValue.setTextColor(Color.GREEN);
        } else if(mTestItemList.get(position).getTestResult().equalsIgnoreCase("nt")) {
            holder.textValue.setTextColor(Color.GRAY);
        }else if(mTestItemList.get(position).getTestResult().equalsIgnoreCase("go")) {
            holder.textValue.setTextColor(Color.BLACK/*Color.YELLOW*/);
        }else {
            holder.textValue.setTextColor(Color.RED);
        }

        if(mIsGetResult) {
            //testID maybe change to not 1..23,use position for display order.
            holder.textId.setText(/*mTestItemList.get(position).getTestID()*/String.valueOf(position + 1));
            //holder.textNote.setText(mTestItemList.get(position).getmTestNote());
            if(!TextUtils.isEmpty(mTestItemList.get(position).getTestStation()) && holder.textStation != null){
                holder.textStation.setText(mTestItemList.get(position).getTestStation());
            }
        }
        holder.textItem.setText(mTestItemList.get(position).getTestCase());
        holder.textValue.setText(mTestItemList.get(position).getTestResult());
        return convertView;
    }

    public static class ViewHolder {
        public TextView textId;
        public TextView textStation;
        public TextView textItem;
        public TextView textValue;
    }
}
