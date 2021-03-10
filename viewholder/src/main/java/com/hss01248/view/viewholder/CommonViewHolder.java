package com.hss01248.view.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistryOwner;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * by hss
 * data:2020/7/8
 * desc:
 */
public abstract class CommonViewHolder< InitInfo,Binding extends ViewBinding> implements DefaultLifecycleObserver {

   public View rootView;
    public   Binding binding;


    protected ViewGroup parent;
    protected InitInfo initInfo;
    protected LifecycleOwner lifecycleOwner;

    public CommonViewHolder(@Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner, @Nullable ViewGroup parent, boolean attachToParent) {
        lifecycleOwner.getLifecycle().addObserver(this);
        this.lifecycleOwner = lifecycleOwner;
        if(inflater == null){
            inflater = LayoutInflater.from(BaseViewHolder.getContext(lifecycleOwner,parent));
        }
        this.parent = parent;
        binding = createDataBinding(inflater,parent,attachToParent);

       rootView = binding.getRoot();
        //binding.setLifecycleOwner(lifecycleOwner);

    }



    public CommonViewHolder<InitInfo,Binding> addToParentView(int index) {
        if (index < 0) {
            index = 0;
        }
        try {
            parent.addView(rootView, index);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        return this;
    }


    private  Binding createDataBinding(LayoutInflater inflater, ViewGroup parent, boolean attachToParent){
        // // public static ItemDemoBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent)
        //binding = createDataBinding(inflater);
        /*((Class<T>) ((ParameterizedType) (this.getClass()
                .getGenericSuperclass())).getActualTypeArguments()[i])*/
        Type[] types = ((ParameterizedType)(this.getClass().getGenericSuperclass())).getActualTypeArguments();

        try {
            Class vb = (Class) types[1];
            Method method = null;
            method = vb.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            return (Binding) method.invoke(vb,inflater,parent,attachToParent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public final void initDataAndEvent(@Nullable InitInfo bean) {
        initInfo = bean;
        initDataAndEventInternal(lifecycleOwner, bean);

    }

    protected abstract void initDataAndEventInternal(LifecycleOwner lifecycleOwner, InitInfo bean);

}
