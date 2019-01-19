package com.adgad.kboard;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public final TextView textView;

    private ItemClickListener mListener;

    public ItemViewHolder(View itemView, ItemClickListener clickListener) {
        super(itemView);
        textView = (TextView) itemView;
        mListener = clickListener;
        textView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        mListener.onItemClick(this.getAdapterPosition());
    }


    public interface ItemClickListener {
        void onItemClick(int position);
    }
}