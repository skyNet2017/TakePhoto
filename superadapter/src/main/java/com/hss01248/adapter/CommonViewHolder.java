package com.hss01248.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by huangshuisheng on 2017/12/18.
 */

public abstract class CommonViewHolder<T,A extends Context> implements  View.OnAttachStateChangeListener,ILifeCycle{

    public View rootView;
    public A activity;
    protected ViewGroup parent;

    public CommonViewHolder(A context,ViewGroup parent){
        this.activity = context;
        int layoutRes = setLayoutRes();
        this.parent = parent;
        if(layoutRes !=0){
            LayoutInflater factory = LayoutInflater.from(context);
            //通过这个方法让layoutres里的布局参数设置有效
            rootView = factory.inflate(layoutRes,parent,false);
        }else {
            rootView = setRootView(context);
            if(rootView ==null){
                throw new RuntimeException("setRootView is null !");
            }
        }
        rootView.addOnAttachStateChangeListener(this);
        if(SuperHolderInitor.getButterKnife() !=null){
            SuperHolderInitor.getButterKnife().bind(this,rootView);
        }
        findViewsById(rootView);
    }

    protected  void findViewsById(View rootView){}

    protected ViewGroup setRootView(Context context) {
        return null;
    }

    protected abstract int setLayoutRes();

    public  abstract void assingDatasAndEvents(A activity, @Nullable T bean);
    public   void assingDatasAndEvents(A activity, @Nullable T bean, int position){
        assingDatasAndEvents(activity,bean);
    }

    public   void assingDatasAndEvents(A activity, @Nullable T bean, int position, Object extra,
                                       boolean isLast, List datas, SuperViewGroupSingleAdapter adapter){
        assingDatasAndEvents(activity,bean,position);
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
