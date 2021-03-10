package com.hss01248.view.viewholder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

/**
 * BYB的ViewModel
 *
 * @author PengZhiming
 * @version 1.0
 * @since 2020/4/17
 */
public class BaseViewModel<Repo extends BaseRepository,MainResponseData> extends ViewModel {

    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> emptyLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<MainResponseData> contentViewData = new MutableLiveData<>();

    public MutableLiveData<Boolean> getShowContentView() {
        return showContentView;
    }

    private final MutableLiveData<Boolean> showContentView = new MutableLiveData<Boolean>();



    protected Repo repository;

    protected SavedStateHandle savedStateHandle;

    public BaseViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
        repository = TUtil.getNewInstance(this,0);
    }

   /* public MutableLiveData<Integer> getNumber(){
        if (!handle.contains(MainActivity.KEY_NUMBER)){
            handle.set(MainActivity.KEY_NUMBER,0);  //判断Handle里面的值是否被初始化，如果没有，就赋值这个key的值为0
        }
        return handle.getLiveData(MainActivity.KEY_NUMBER);
    }*/




    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public MutableLiveData<Boolean> getEmptyLiveData() {
        return emptyLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }


    public  MutableLiveData<MainResponseData> getContentViewData() {
        return (MutableLiveData<MainResponseData>) contentViewData;
    }

    protected void showLoading() {
        loadingLiveData.postValue(true);
    }

    @SuppressWarnings("unchecked")
    protected void showContent() {
        showContentView.postValue(true);
    }

    @SuppressWarnings("unchecked")
    protected  void setContentViewData(MainResponseData data) {
        contentViewData.postValue(data);
    }

    protected void showError(String errorMsg) {
        errorLiveData.postValue(errorMsg);
    }

    protected void showEmpty() {
        emptyLiveData.postValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (repository != null) {
            repository.unSubscribe();
        }
    }
}
