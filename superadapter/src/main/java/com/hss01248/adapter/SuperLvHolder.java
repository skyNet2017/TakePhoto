package com.hss01248.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2016/4/15 0015.
 */
public abstract class SuperLvHolder<T,A extends Context> implements  View.OnAttachStateChangeListener,ILifeCycle{
    public View rootView;
    public int type;
    public A activity;

    /*public SuperLvHolder(){

    }*/

    public SuperLvHolder(A context, ViewGroup viewGroup){
        this.activity = context;
        int layoutRes = setLayoutRes();
        if(layoutRes !=0){
            LayoutInflater factory = LayoutInflater.from(context);
            //通过这个方法让layoutres里的布局参数设置有效
            rootView = factory.inflate(layoutRes,viewGroup,false);
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

    private View setRootView(A context) {
        return null;
    }

    public SuperLvHolder setType(int type){
        this.type = type;
        return this;
    }

    protected abstract  @LayoutRes  int setLayoutRes();

    /**
     * 一般情况下，实现这个方法就足够了
     * @param activity
     * @param bean
     */
    public  abstract void assingDatasAndEvents(A activity, T bean);

    /**
     * 如果有需要，才实现这个方法
     * @param activity activity实例,用于一些点击事件
     * @param bean 该条目的数据
     * @param position 该条目所在的位置
     * @param isLast 是否为最后一条,有些情况下需要用到
     * @param isListViewFling listview是不是在惯性滑动,备用
     *  @param datas 整个listview对应的数据
     * @param superAdapter adapter对象引用,可用于触发notifydatesetChanged()方法刷新整个listview,比如更改的单选按钮
     */
    public void assingDatasAndEvents(A activity, T bean, int position ,boolean isLast,
                                     boolean isListViewFling,List datas, SuperLvAdapter superAdapter){
        assingDatasAndEvents(activity,bean);
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
