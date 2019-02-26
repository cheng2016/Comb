package com.cds.comb.module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cds.comb.R;
import com.cds.comb.data.entity.Light;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Author: chengzj
 * @CreateDate: 2019/1/4 16:51
 * @Version: 3.0.0
 */
public class LightAdapter extends BaseAdapter {
    List<Light> mDataList = new ArrayList<>();
    Context context;

    public LightAdapter(Context context) {
        this.context = context;
    }

    public LightAdapter(Context context, List<Light> list) {
        this.mDataList = list;
        this.context = context;
    }

    public void setDataList(List<Light> listItems) {
        this.mDataList = listItems;
        this.notifyDataSetChanged();
    }

    public List<Light> getDataList() {
        return mDataList;
    }

    @Override
    public int getCount() {
        if (mDataList.size() == 16) {
            return mDataList.size();
        }
        return mDataList.size() + 1;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_light, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == mDataList.size() && position < 16) { //在最后位置增加一个加号图片
            holder.addImg.setVisibility(View.VISIBLE);
            holder.contentLayout.setVisibility(View.GONE);

            holder.addImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Light light = new Light("10","30");
                    mDataList.add(light);
                    setDataList(mDataList);
                }
            });
            return convertView;
        }
        holder.addImg.setVisibility(View.GONE);
        holder.contentLayout.setVisibility(View.VISIBLE);
        holder.indexTv.setText((position + 1) + "");
        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataList.remove(position);
                setDataList(mDataList);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.content)
        RelativeLayout contentLayout;
        @Bind(R.id.index)
        TextView indexTv;
        @Bind(R.id.top_img)
        ImageView topImg;
        @Bind(R.id.bottom_img)
        ImageView bottomImg;
        @Bind(R.id.mw_top_editText)
        EditText mwTopEditText;
        @Bind(R.id.time_top_editText)
        EditText timeTopEditText;
        @Bind(R.id.mw_bottom_editText)
        EditText mwBottomEditText;
        @Bind(R.id.time_bottom_editText)
        EditText timeBottomEditText;
        @Bind(R.id.delete)
        ImageView deleteImg;
        @Bind(R.id.add)
        ImageView addImg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
