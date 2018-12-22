package com.hss01248.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Administrator on 2017/12/9.
 */

public abstract class SuperPagerAdapter<A extends Context> extends PagerAdapter implements Refreshable,ILifeCycle{

    List datas = new ArrayList();
    A context;
    ViewGroup container;

    public SuperPagerAdapter(A context){
        this.context = context;
    }


    @Override
    public int getCount() {
        //Log.e("dd","SuperPagerAdapter.count:"+datas.size());
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        this.container = container;
        SuperPagerHolder holder = generateNewHolder(context,container,position);
        holder.assingDatasAndEventsWithPosition(context,datas.get(position),position);
        holder.rootView.setTag(holder);
        container.addView(holder.rootView);
        return holder.rootView;
    }

    protected abstract SuperPagerHolder generateNewHolder(A context, ViewGroup container, int position);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public void refresh(List newData) {
        datas.clear();
        datas.addAll(newData);
        notifyDataSetChanged();
        //viewpager本身不支持刷新,需要自己处理:
        forceRefresh();
    }

    private void forceRefresh() {
        if(container == null){
            return;
        }

        /*int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = container.getChildAt(i);
            Object tag = child.getTag();
            if(tag instanceof SuperPagerHolder){
                SuperPagerHolder holder = (SuperPagerHolder) tag;
                holder.assingDatasAndEvents(context,datas.get());
            }
        }*/
    }

    @Override
    public void addAll(List newData) {
        datas.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        datas.clear();
        notifyDataSetChanged();
    }

    @Override
    public void delete(int position) {
        datas.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void add(Object object) {
        datas.add(object);
        notifyDataSetChanged();
    }

    @Override
    public void onDestory() {

    }
}
