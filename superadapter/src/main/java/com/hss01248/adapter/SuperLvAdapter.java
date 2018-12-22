package com.hss01248.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 单一的item
 * Created by Administrator on 2016/4/15 0015.
 */
public abstract class SuperLvAdapter<A extends Context> extends BaseAdapter implements Refreshable,ILifeCycle {
    List datas;
    HashMap<Class,Integer> itemCountTypeMap;
    A context;
    boolean isListViewFling;
    public static final int TYPE_NULL = 1;

    public boolean isListViewFling() {
        return isListViewFling;
    }

    public void setListViewFling(boolean listViewFling) {
        isListViewFling = listViewFling;
    }


    public SuperLvAdapter(A context){
        this.datas = new ArrayList();
        this.context = context;
        itemCountTypeMap = new HashMap<>();
    }
    @Override
    public int getCount() {
        if (datas == null )
        return 0;
        return datas.size();
    }

    protected int getViewTypeByClass(Class clazz){
        return itemCountTypeMap.get(clazz);
    }

    @Override
    public int getViewTypeCount() {
        return getDatasTypeCount();
    }

    private int getDatasTypeCount() {
        itemCountTypeMap.clear();
        int i = 0;
        for (Object obj : datas) {
            Class clazz = null;
            if(obj ==null){
                clazz = MyTypeNull.class;
            }else {
                clazz = obj.getClass();
            }
            if(!itemCountTypeMap.containsKey(clazz)){
                itemCountTypeMap.put(clazz,i);
                i++;
            }
        }
        if(i>1){
            return i;
        }
        return super.getViewTypeCount();
    }

    /**
     * view和数据model/bean一一对应
     * 与javabean的类型挂钩,有几个javabean类型,就有几个item类型
     * 而且,返回的数据要小于getViewTypeCount
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return itemCountTypeMap.get(getClazz(position));
    }

    private Class getClazz(int position) {
        Object obj = datas.get(position);
        Class clazz = null;
        if(obj ==null){
            clazz =  MyTypeNull.class;
        }else {
            clazz = obj.getClass();
        }
        return clazz;
    }

    @Override
    public Object getItem(int position) {
        if (datas == null)
        return null;
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (datas == null){
            return 0;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SuperLvHolder holder = null;
        Class clazz = getClazz(position);
        int type = itemCountTypeMap.get(clazz);
        View retrunView = convertView;
        if (convertView == null){
            holder = generateNewHolder(context,type,clazz,parent);
            holder.setType(type);
            retrunView = holder.rootView;
            retrunView.setTag(holder);
        }else {
            holder = (SuperLvHolder) retrunView.getTag();
            if(!(holder.type == type)){
                holder = generateNewHolder(context,type,clazz,parent);
                holder.setType(type);
                retrunView = holder.rootView;
                retrunView.setTag(holder);
            }
        }
        holder.assingDatasAndEvents(context,datas.get(position),position,position == getCount() -1,isListViewFling,datas,this);
        return retrunView;
    }


    protected abstract SuperLvHolder generateNewHolder(A context, int itemViewType,Class beanClass,ViewGroup parent);

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public void refresh(List newData){
        if (newData == null){
            datas.clear();
            notifyDataSetChanged();
            return;
        }
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
    public void addAll(List newData){
        if (newData == null){
            return;
        }
        if (datas == null){
            datas = newData;
            notifyDataSetChanged();
        }else {
            datas.addAll(newData);
            notifyDataSetChanged();
        }
    }
    @Override
    public void clear(){
        if (datas != null){
            datas.clear();
            notifyDataSetChanged();
        }
    }
    @Override
    public void delete(int position){
        if (datas != null && position < getCount()){
            datas.remove(position);
            notifyDataSetChanged();
        }
    }
    @Override
    public List getListData(){
        return datas;
    }

    @Override
    public void add(Object object) {
        if (object ==null){
            return;
        }

        try {
            datas.add(object);
            notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    @Override
    public void onDestory() {

    }
}
