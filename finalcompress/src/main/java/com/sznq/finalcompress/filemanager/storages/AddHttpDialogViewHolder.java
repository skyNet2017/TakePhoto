package com.sznq.finalcompress.filemanager.storages;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.db.StorageBeanDao;
import com.hss01248.media.mymediastore.http.HttpHelper;
import com.hss01248.view.viewholder.CommonViewHolder;
import com.sznq.finalcompress.databinding.DialogAddHttpBinding;
import com.sznq.finalcompress.databinding.ItemStorageBinding;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AddHttpDialogViewHolder extends CommonViewHolder<Dialog, DialogAddHttpBinding> {
    public AddHttpDialogViewHolder(@Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner, @Nullable ViewGroup parent, boolean attachToParent) {
        super(inflater, lifecycleOwner, parent, attachToParent);
    }

    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, Dialog bean) {
        binding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAdd();
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bean.dismiss();
            }
        });
    }

    private void checkAdd() {
        if (TextUtils.isEmpty(binding.etIp.getText().toString().trim())) {
            ToastUtils.showLong("ip必须输入");
            return;
        }
        ToastUtils.showLong("保存");
        StorageBean bean = new StorageBean();
        bean.type = StorageBean.TYPE_HTTP_Everything;
        bean.ip = binding.etIp.getText().toString().trim();
        bean.name = binding.etHostname.getText().toString().trim();
        bean.uname = binding.etName.getText().toString().trim();
        bean.pw = binding.etPw.getText().toString().trim();
        if(!bean.ip.startsWith("http")){
            bean.ip= "http://"+bean.ip;
        }

        long count = DbUtil.getDaoSession().getStorageBeanDao().queryBuilder()
                .where(StorageBeanDao.Properties.Type.eq(StorageBean.TYPE_HTTP_Everything), StorageBeanDao.Properties.Ip.eq(bean.ip)).count();
        if (count > 0) {
            //DbUtil.getDaoSession().getStorageBeanDao().update(bean);
            ToastUtils.showLong("该ip已经在列表中,不能重复添加");
            return;
        }
        checkIpAvaiable(bean);

    }

    private void checkIpAvaiable(StorageBean bean) {

        Observable.just(bean)
                .subscribeOn(Schedulers.io())
                .map(new Function<StorageBean, Boolean>() {
                    @Override
                    public Boolean apply(@io.reactivex.annotations.NonNull StorageBean storageBean) throws Exception {
                        return HttpHelper.checkAvailable(bean.ip, bean.uname, bean.pw);
                    }
                }).doOnNext(new Consumer<Boolean>() {
            @SuppressLint("CheckResult")
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    DbUtil.getDaoSession().getStorageBeanDao().insert(bean);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {

                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {
                        if(aBoolean){
                            ToastUtils.showLong("http数据源添加成功,可在列表里点击进入扫描");
                            initInfo.dismiss();
                        }else {
                            ToastUtils.showLong("该ip无法连接成功");

                        }

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });




    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        initInfo.dismiss();
    }
}
