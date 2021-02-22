package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Darshan on 4/24/2015.
 */
public abstract class CustomGenericAdapter<T> extends BaseAdapter {
    protected List<T> arrayList = new ArrayList<>();
    protected Context context;
    protected LayoutInflater layoutInflater;

    protected int size;

    public CustomGenericAdapter(Context context, List<T> arrayList) {
        this.arrayList  = arrayList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    public T getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setLayoutParams(int size) {
        this.size = size;
    }

    public void releaseResources() {
        arrayList = null;
        context = null;
    }


}
