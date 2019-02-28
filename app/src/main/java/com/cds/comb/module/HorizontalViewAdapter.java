package com.cds.comb.module;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cds.comb.R;
import com.cds.comb.data.entity.Light;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chengzj
 * @CreateDate: 2019/2/28 12:06
 * @Version: 3.0.0
 */
public class HorizontalViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Light> mDataList = new ArrayList<>();

    private Context context;

    public HorizontalViewAdapter(Context context) {
        this.context = context;
    }

    public void setDataList(List<Light> listItems) {
        this.mDataList = listItems;
        this.notifyDataSetChanged();
    }

    public List<Light> getDataList() {
        return mDataList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public View topIndicator;

        View bottomIndicator;

        TextView indexTv;

        public ViewHolder(View view) {
            super(view);
            topIndicator = (View) view.findViewById(R.id.top_indicator);
            bottomIndicator = (View) view.findViewById(R.id.bottom_indicator);
            indexTv = (TextView) view.findViewById(R.id.index_tv);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_horizontal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;

        Light bean = mDataList.get(position);
        if (bean.getIr().equals("0")) {
            holder.topIndicator.setBackground(context.getDrawable(R.drawable.rectangle_gray));
        } else {
            holder.topIndicator.setBackground(context.getDrawable(R.drawable.rectangle_yellow));
        }
        if (bean.getRed().equals("0")) {
            holder.bottomIndicator.setBackground(context.getDrawable(R.drawable.rectangle_gray));
        } else {
            holder.bottomIndicator.setBackground(context.getDrawable(R.drawable.rectangle_red));
        }
        holder.indexTv.setText("" + (position+1));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
