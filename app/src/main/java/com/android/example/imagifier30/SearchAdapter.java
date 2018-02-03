package com.android.example.imagifier30;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Meeth on 03-Feb-18.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    Context context;
    ArrayList<Search> searchArrayList;

    public SearchAdapter(Context context, ArrayList<Search> searchArrayList) {
        this.context = context;
        this.searchArrayList = searchArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.search_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvSearch.setText(searchArrayList.get(position).getDesc());
        Glide.with(context).load(searchArrayList.get(position).getImageUrl()).into(holder.imSearch);
    }

    @Override
    public int getItemCount() {
        return searchArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imSearch;
        TextView tvSearch;

        public ViewHolder(View itemView) {
            super(itemView);

            imSearch = (ImageView) itemView.findViewById(R.id.iv_search);
            tvSearch = (TextView) itemView.findViewById(R.id.tv_search);
        }
    }
}
