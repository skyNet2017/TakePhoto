package com.hss01248.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/12/9.
 */

public abstract class SuperPagerHolder<T,A extends Context> implements  View.OnAttachStateChangeListener,ILifeCycle{

    public ViewGroup rootView;
    public A activity;
     int position;

    public SuperPagerHolder(A context,ViewGroup parent){
        int layoutRes = setLayoutRes();
        this.activity = context;
        if(layoutRes !=0){
            LayoutInflater factory = LayoutInflater.from(context);
            //通过这个方法让layoutres里的布局参数设置有效
            rootView = (ViewGroup) factory.inflate(layoutRes,parent,false);
        }else {
            rootView = setRootView(context,parent);
        }
        rootView.addOnAttachStateChangeListener(this);
        if(SuperHolderInitor.getButterKnife() !=null){
            SuperHolderInitor.getButterKnife().bind(this,rootView);
        }
        findViewsById(rootView);
    }

    protected  void findViewsById(View rootView){}

    protected ViewGroup setRootView(Context context,ViewGroup parent) {
        return null;
    }

    protected abstract int setLayoutRes();


    public  abstract void assingDatasAndEvents(A activity, @Nullable T bean, int position);

    public   void assingDatasAndEventsWithPosition(A activity, @Nullable T bean, int position){
        this.position = position;
        this.assingDatasAndEvents(activity,bean,position);
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {

    }

    @Override
    public void onDestory() {

    }
}
