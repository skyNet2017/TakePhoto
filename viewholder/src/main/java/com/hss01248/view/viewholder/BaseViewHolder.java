package com.hss01248.view.viewholder;



import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.savedstate.SavedStateRegistryOwner;




/**
 * by hss
 * data:2020-04-10
 * desc: 界面模块化
 * K 初始化时用数据刷新界面的数据类型,比如从bundle等处传过来的
 *
 * 如果带loading,error等状态,那么xml使用statuview或其子类即可
 *
 * 通过lifecycle感知生命周期
 */
@Deprecated
public abstract class BaseViewHolder<VM extends BaseViewModel, InitInfo>
        implements DefaultLifecycleObserver {

    public View getRootView() {
        return rootView;
    }

    protected View rootView;

    protected LifecycleOwner lifecycleOwner;

    protected ViewGroup parent;

    public VM getViewModel() {
        return viewModel;
    }

    protected VM viewModel;

    protected InitInfo initInfo;



    protected Context context;

    /**
     * 在activity或者view中使用
     */
    public BaseViewHolder(@NonNull LifecycleOwner lifecycleOwner, ViewGroup parent) {
        this(null,lifecycleOwner,parent);
    }

    /**
     * 在fragment中使用
     */
    public BaseViewHolder( @Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner, @Nullable ViewGroup parent) {
        this.lifecycleOwner = lifecycleOwner;
        lifecycleOwner.getLifecycle().addObserver(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        int layoutRes = getLayoutRes();
        this.parent = parent;
        context = getContext(lifecycleOwner, parent);
        if (layoutRes != 0) {
            if(inflater == null){
                inflater = LayoutInflater.from(context);
            }
            rootView = inflater.inflate(layoutRes, parent, false);
        } else {
            rootView = getRootView(context);
            if (rootView == null) {
                throw new RuntimeException("getRootView is null !");
            }
        }
        initView0();
        this.viewModel = new ViewModelProvider((ViewModelStoreOwner) lifecycleOwner,
                new SavedStateViewModelFactory(TUtil.app, (SavedStateRegistryOwner) lifecycleOwner))
                .get(getViewModelClass());

    }

    public BaseViewHolder<VM,InitInfo> addToParentView(int index) {
        if (index < 0) {
            index = 0;
        }
        parent.addView(rootView, index);
        return this;
    }

    /**
     * 初始化数据和事件.给外部调用
     */
    public final void initDataAndEvent(LifecycleOwner lifecycleOwner, @Nullable InitInfo bean) {
        initInfo = bean;
        initDataAndEventInternal(lifecycleOwner, bean);
        showUIAndRequestData(bean);
    }







    protected abstract int getLayoutRes();

    /**
     * 使用findViewByMe插件生成代码,方法为initView(), 在子类里调用即可
     */
    protected abstract void initView0();

    /**
     * 根据传入的数据初始化holder
     * @param lifecycleOwner
     * @param bean
     */
    protected abstract void initDataAndEventInternal(LifecycleOwner lifecycleOwner, InitInfo bean);

    protected abstract Class<VM> getViewModelClass();

    protected abstract void showUIAndRequestData(InitInfo initInfo);

    protected View findViewById(int id){
        return rootView.findViewById(id);
    }




    static Context getContext(@NonNull LifecycleOwner lifecycleOwner, @Nullable ViewGroup parent) {
        Context context = null;
        if (parent != null) {
            context = parent.getContext();
        } else {
            if (lifecycleOwner instanceof Fragment) {
                context = ((Fragment) lifecycleOwner).getContext();
            } else if (lifecycleOwner instanceof FragmentActivity) {
                context = (Context) lifecycleOwner;
            }
        }
        return context;
    }


    protected View getRootView(Context context) {
        return null;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground() {
        // 应用进入后台
        Log.w("test","LifecycleChecker onAppBackground ON_STOP");

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForeground() {
        // 应用进入前台
        Log.w("test","LifecycleChecker onAppForeground ON_START");
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }
}
