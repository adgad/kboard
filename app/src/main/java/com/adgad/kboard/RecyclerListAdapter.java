package com.adgad.kboard;

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private final List<String> mItems = new ArrayList<>();
    private final ItemViewHolder.ItemClickListener mListener;
    private final SharedPreferences mPreferences;
    private final Gson gson = new Gson();

    public RecyclerListAdapter(ArrayList items, SharedPreferences sharedPrefs, ItemViewHolder.ItemClickListener listener) {
        mItems.addAll(items);
        mListener = listener;
        mPreferences = sharedPrefs;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ItemViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.textView.setText(mItems.get(position));
    }


    private void updateWords() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KboardIME.Keys.STORAGE_KEY, gson.toJson(mItems));
        editor.apply();
    }

    public void add(String item) {
        mItems.add(item);
        notifyItemInserted(mItems.size());
        updateWords();
    }

    public void set(int position, String item) {
        mItems.set(position, item);
        notifyItemChanged(position);
        updateWords();
    }

    public void addAll(Collection<String> items) {
        int start = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(start, mItems.size());
        updateWords();
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
        updateWords();
    }

    public String get(int position) {
        return mItems.get(position);
    }

    public void clear() {
        int size = mItems.size();
        mItems.clear();
        notifyItemRangeChanged(0, size);
        updateWords();
    }

    public void swap(int fromPosition, int toPosition) {
        String text = mItems.get(fromPosition);
        mItems.remove(fromPosition);
        mItems.add(toPosition, text);
        notifyItemMoved(fromPosition, toPosition);
        updateWords();
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

}