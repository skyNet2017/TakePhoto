package com.hss01248.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public  abstract   class SuperRvAdapter<A extends Context> extends RecyclerView.Adapter<SuperRvHolder> implements Refreshable,ILifeCycle {


    private List datas;
    private A context;
    boolean isListViewFling;
    public static final int TYPE_NULL = 1;

    private List<SuperRvHolder> headers;
    private List<SuperRvHolder> footers;
    private List headerBeans;
    private List footerBeans;

    public boolean isListViewFling() {
        return isListViewFling;
    }

    public void setListViewFling(boolean listViewFling) {
        isListViewFling = listViewFling;
    }



    public SuperRvAdapter(A context){

        this.datas = new ArrayList();
        this.context = context;
        headers = new ArrayList<>();
        footers = new ArrayList<>();
        headerBeans = new ArrayList<>();
        footerBeans = new ArrayList<>();
        datas.addAll(headerBeans);
        datas.addAll(footerBeans);
    }

    public void addHeader(View  headView){
        SuperRvHolder holder = new SuperRvHolder(headView) {
            @Override
            public void assignDatasAndEvents(Context context, Object data) {

            }
        };
        headers.add(holder);
        MyRcvHeaderClazz clazz = new MyRcvHeaderClazz();
        headerBeans.add(clazz);
        datas.add(headerBeans.size()-1,clazz);
        notifyDataSetChanged();
    }

    public void addFooter(View  footerView){
        SuperRvHolder holder = new SuperRvHolder(footerView) {
            @Override
            public void assignDatasAndEvents(Context context, Object data) {

            }
        };
        footers.add(holder);
        MyRcvFooterClazz clazz = new MyRcvFooterClazz();
        footerBeans.add(clazz);
        datas.add(clazz);
        notifyDataSetChanged();
    }

    /**
     * 工具方法
     * @param layoutRes
     * @return
     */
    public  View inflate(ViewGroup parent,@LayoutRes int layoutRes){
       return LayoutInflater.from(context).inflate(layoutRes,parent,false);
       //上面的api才能应用到layout文件中的固定宽高
        //return View.inflate(context, layoutRes,null);
    }


    @Override
    public SuperRvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("adapter","viewType:"+viewType);
        if(viewType>=0 && viewType<datas.size()){
            if(viewType<headerBeans.size()){
                return headers.get(viewType);
            }else {
                int footPosition = viewType - (datas.size() -footerBeans.size());
                return footers.get(footPosition);
            }
        }
        return generateCustomViewHolder(parent,viewType);
    }

    protected abstract SuperRvHolder generateCustomViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(SuperRvHolder holder, int position) {
        if(getItemViewType(position) == position){
            return;
        }

        holder.assignDatasAndEvents(context,datas.get(position),position,position == getItemCount() -1,isListViewFling,datas,this);
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = datas.get(position);
        if(obj ==null){
            return TYPE_NULL;
        }
        if(obj.getClass() == MyRcvHeaderClazz.class ){
            return position;

        }
        if(obj.getClass() == MyRcvFooterClazz.class ){
            return  position;

        }
        return obj.getClass().hashCode();
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void refresh(List newData) {


        if (newData == null){
            datas.clear();
            notifyDataSetChanged();
            return;
        }
        newData.addAll(0,headerBeans);
        newData.addAll(footerBeans);



        if (datas == null){
            datas = newData;
            notifyDataSetChanged();
        }else {
            datas.clear();
            datas.addAll(newData);
            notifyDataSetChanged();
        }
    }

    @Override
    public void addAll(List newData) {
        if (newData == null){
            return;
        }

        datas.addAll(datas.size()-footerBeans.size(),newData);
    }

    @Override
    public void clear() {
        if (datas != null){
            datas.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void delete(int position) {
        position = position+headerBeans.size();
        if (datas != null && position < getItemCount()){
            datas.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public void add(Object object) {
        if (object != null){
            datas.add(datas.size()-footerBeans.size(),object);
            notifyItemInserted(datas.size() -footerBeans.size());
        }

    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    public List getListData(){
        return datas;
    }

    @Override
    public void onDestory() {

    }

    /*public class  ViewHolder extends RecyclerView.ViewHolder {
        public  ViewGroup rootView;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = (ViewGroup) itemView;
            ButterKnife.bind(this,rootView);

        }

        public void assignDatasAndEvents(Activity context,Object data,int position){
            SuperRclyAdapter.this.assignDatasAndEvents(context,data,position);
        }


    }*/
    public class MyRcvHeaderClazz{

    }
    public class MyRcvFooterClazz{

    }
}
