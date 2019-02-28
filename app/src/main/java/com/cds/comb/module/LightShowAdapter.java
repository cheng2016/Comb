package com.cds.comb.module;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cds.comb.R;
import com.cds.comb.data.entity.Light;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chengzj
 * @CreateDate: 2019/2/28 11:02
 * @Version: 3.0.0
 */
public class LightShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int TYPE_ADD = 0;//添加图片
    private static int TYPE_COMMON = 1;//普通图片展示
    private Context context;
    private LayoutInflater mLayoutInflater;
    //data
    private int mMaxAlbum;//最大选择图片的数量

    private List<Light> mDataList = new ArrayList<>();//图片url集合


    public void setDataList(List<Light> listItems) {
        this.mDataList = listItems;
        this.notifyDataSetChanged();
        if(listener!=null){
            listener.onDataChange();
        }
    }

    public List<Light> getDataList() {
        return mDataList;
    }

    public LightShowAdapter(Context context, int maxAlbum) {
        this.context = context;
        this.mMaxAlbum = maxAlbum;
        this.mLayoutInflater = LayoutInflater.from(context);
    }


    public LightShowAdapter(Context context, List<Light> mStringList, int maxAlbum) {
        this.context = context;
        this.mDataList = mStringList;
        this.mMaxAlbum = maxAlbum;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            return new ItemViewHolderAdd(mLayoutInflater.inflate(R.layout.item_selected_add, parent, false));
        } else {
            return new ItemViewHolderCommon(mLayoutInflater.inflate(R.layout.item_selected_common, parent, false));
        }
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        View itemView = null;
        if (viewHolder instanceof ItemViewHolderAdd) {
            ItemViewHolderAdd itemViewHolderAdd = (ItemViewHolderAdd) viewHolder;
            if (position >= mMaxAlbum) {
                itemViewHolderAdd.itemView.setVisibility(View.GONE);
            } else {
//                itemViewHolderAdd.tvNum.setText(position + "/" + mMaxAlbum);
                itemViewHolderAdd.itemView.setVisibility(View.VISIBLE);

                itemViewHolderAdd.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Light light = new Light("10", "30", "10", "30");
                        mDataList.add(light);
                        setDataList(mDataList);
                    }
                });

                itemView = ((ItemViewHolderAdd) viewHolder).itemView;
            }
        } else if (viewHolder instanceof ItemViewHolderCommon) {
            ItemViewHolderCommon holder = (ItemViewHolderCommon) viewHolder;


            Light bean = mDataList.get(position);
            holder.indexTv.setText((position + 1) + "");
            holder.irMwEditText.setText(bean.getIr());
            holder.irTimeEditText.setText(bean.getIrTime());
            if(bean.getIr().equals("0")){
                holder.irImg.setImageResource(R.mipmap.btn_ir2);
                holder.irMwEditText.setTextColor(context.getResources().getColor(R.color.gray));
                holder.irTimeEditText.setTextColor(context.getResources().getColor(R.color.gray));
            }else {
                holder.irImg.setImageResource(R.mipmap.btn_ir1);
                holder.irMwEditText.setTextColor(context.getResources().getColor(R.color.holo_red_dark));
                holder.irTimeEditText.setTextColor(context.getResources().getColor(R.color.holo_red_dark));
            }
            holder.redMwEditText.setText(bean.getRed());
            holder.redTimeEditText.setText(bean.getRedTime());

            holder.deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataList.remove(position);
                    setDataList(mDataList);
                }
            });

            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onContentClick(v, position, mDataList.get(position));
                    }
                }
            });

//            Glide.with(context).load(url).apply(RequestOptions.centerCropTransform()).transition(withCrossFade()).into(((ItemViewHolderCommon) holder).ivCommon);
            itemView = ((ItemViewHolderCommon) viewHolder).itemView;
        }
/*        if (mOnItemClickListener != null && null != itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(viewHolder.itemView, position);
                }
            });
        }*/
    }

    @Override
    public int getItemViewType(int position) {
        return position == mDataList.size() ? TYPE_ADD : TYPE_COMMON;
    }
    @Override
    public int getItemCount() {
        return mDataList.size() + 1;//加一代表最后一个添加图片按钮
    }
    public static class ItemViewHolderAdd extends RecyclerView.ViewHolder {
        private ImageView addImg;
        public ItemViewHolderAdd(View itemView) {
            super(itemView);
            addImg = itemView.findViewById(R.id.add);
        }
    }

    public static class ItemViewHolderCommon extends RecyclerView.ViewHolder {
        TextView indexTv;
        ImageView irImg;
        ImageView redImg;
        AppCompatTextView irMwEditText;
        AppCompatTextView irTimeEditText;
        AppCompatTextView redMwEditText;
        AppCompatTextView redTimeEditText;
        ImageView deleteImg;

        RelativeLayout contentLayout;

        public ItemViewHolderCommon(View itemView) {
            super(itemView);
            indexTv = (TextView) itemView.findViewById(R.id.index);
            irImg = (ImageView) itemView.findViewById(R.id.ir_img);
            redImg = (ImageView) itemView.findViewById(R.id.red_img);
            irMwEditText = (AppCompatTextView) itemView.findViewById(R.id.ir_mw_editText);
            irTimeEditText = (AppCompatTextView) itemView.findViewById(R.id.ir_time_editText);
            redMwEditText = (AppCompatTextView) itemView.findViewById(R.id.red_mw_editText);
            redTimeEditText = (AppCompatTextView) itemView.findViewById(R.id.red_time_editText);
            deleteImg = (ImageView) itemView.findViewById(R.id.delete);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content);
        }
    }

/*    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }*/

    OnContentClickListener listener;

    public void setListener(OnContentClickListener listener) {
        this.listener = listener;
    }

    public interface OnContentClickListener {
        void onContentClick(View view, int index, Light light);

        void onDataChange();
    }
}
