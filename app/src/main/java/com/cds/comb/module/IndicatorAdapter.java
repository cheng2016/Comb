package com.cds.comb.module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cds.comb.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Author: chengzj
 * @CreateDate: 2019/1/7 15:08
 * @Version: 3.0.0
 */
public class IndicatorAdapter extends BaseAdapter {
    List mDataList = new ArrayList<>();
    Context context;

    public IndicatorAdapter(Context context) {
        this.context = context;
    }

    public IndicatorAdapter(Context context, List list) {
        this.mDataList = list;
        this.context = context;
    }

    public void setDataList(List listItems) {
        this.mDataList = listItems;
        this.notifyDataSetChanged();
    }

    public List getDataList() {
        return mDataList;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_indicator, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.top_indicator)
        View topIndicator;
        @Bind(R.id.bottom_indicator)
        View bottomIndicator;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}