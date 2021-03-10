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
public abstract class BaseBindingViewHolder <VM extends BaseViewModel, InitInfo,Binding extends ViewBinding> implements DefaultLifecycleObserver {

   public View rootView;
    public   Binding binding;

    public VM getViewModel() {
        return viewModel;
    }

    protected VM viewModel;
    protected ViewGroup parent;
    protected InitInfo initInfo;

    public BaseBindingViewHolder(@Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner, @Nullable ViewGroup parent,boolean attachToParent) {
        lifecycleOwner.getLifecycle().addObserver(this);
        if(inflater == null){
            inflater = LayoutInflater.from(BaseViewHolder.getContext(lifecycleOwner,parent));
        }
        this.parent = parent;
        binding = createDataBinding(inflater,parent,attachToParent);
        this.viewModel = new ViewModelProvider((ViewModelStoreOwner) lifecycleOwner,
                new SavedStateViewModelFactory(TUtil.app, (SavedStateRegistryOwner) lifecycleOwner))
                .get(getViewModelClass());

       rootView = binding.getRoot();
        //binding.setLifecycleOwner(lifecycleOwner);

    }

    private   Class<VM> getViewModelClass(){
        return vmClass;
    }

    public BaseBindingViewHolder<VM,InitInfo,Binding> addToParentView(int index) {
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

    Class<VM> vmClass;
    private  Binding createDataBinding(LayoutInflater inflater, ViewGroup parent, boolean attachToParent){
        // // public static ItemDemoBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent)
        //binding = createDataBinding(inflater);
        /*((Class<T>) ((ParameterizedType) (this.getClass()
                .getGenericSuperclass())).getActualTypeArguments()[i])*/
        Type[] types = ((ParameterizedType)(this.getClass().getGenericSuperclass())).getActualTypeArguments();

        try {
            vmClass = (Class<VM>) types[0];
            Class vb = (Class) types[2];
            Method method = null;
            method = vb.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            return (Binding) method.invoke(vb,inflater,parent,attachToParent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public final void initDataAndEvent(LifecycleOwner lifecycleOwner, @Nullable InitInfo bean) {
        initInfo = bean;
        initDataAndEventInternal(lifecycleOwner, bean);

    }

    protected abstract void initDataAndEventInternal(LifecycleOwner lifecycleOwner, InitInfo bean);

}
