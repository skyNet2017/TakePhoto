package com.sznq.finalcompress.filemanager.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.pagestate.PageStateManager;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.ActivitySearchBinding;
import com.sznq.finalcompress.filemanager.adapter.FileItemAdapter;
import com.sznq.finalcompress.filemanager.adapter.FileItemImgAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    public static void doSearch(Activity activity,String dir){
        Intent intent = new Intent(activity,SearchActivity.class);
        intent.putExtra("dir",dir);
        activity.startActivity(intent);
    }
    ActivitySearchBinding binding;
    FilterViewHolder filterViewHolder;
    PageStateManager stateManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_search);
        binding = ActivitySearchBinding.inflate(getLayoutInflater(), findViewById(android.R.id.content), true);
        filterViewHolder = new FilterViewHolder(getLayoutInflater(),this, binding.llRoot,false);
        filterViewHolder.addToParentView(1);
        filterViewHolder.initDataAndEventInternal(this,"");
        stateManager = PageStateManager.initWhenUse(binding.recycler,null);
        filterViewHolder.setActivity(this);
        stateManager.showContent();


        initRecycleview();

        binding.titlebar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });
    }

    List<BaseInfo> mediaInfos = new ArrayList<>();
    BaseQuickAdapter adapter;
    int displayType = 1;
    private void initRecycleview() {
        this.displayType = displayType%2;
        if(displayType == 0){
            binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter = new MediaItemAdapter(R.layout.item_file);
        }else if(displayType ==1){
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
            adapter = new MediaItemImgAdapter(R.layout.item_file_img);
        }else {
            binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter = new MediaItemAdapter(R.layout.item_file);
        }
        binding.recycler.setAdapter(adapter);
        adapter.setNewData(mediaInfos);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                /*IFile file =  files.get(position);
                if(file.isDirectory()){
                    listFiles(file);
                }else {
                    ToastUtils.showLong("打开文件:"+file.getPath());
                    openFile(file,files);

                }*/
            }
        });
    }

    String currentSearchKey;
    void doSearch(){
        String word = binding.titlebar.getSearchKey();

        searchDB(word,filterViewHolder.isSearchDir,filterViewHolder.diskType,filterViewHolder.mediaType,
                filterViewHolder.sortType,filterViewHolder.hiddenType);
    }
    int[] pageInfo = new int[]{0,0};
    private void searchDB(String word, boolean isSearchDir, int diskType, int mediaType, int sortType,int hiddenType) {
        stateManager.showLoading();
        Observable.just(1)
                .subscribeOn(Schedulers.io())
                .map(new Function<Integer, List<? extends BaseInfo>>() {
                    @Override
                    public List<? extends BaseInfo> apply(@NonNull Integer integer) throws Exception {
                        return SearchDbUtil.searchItem(word,diskType,mediaType,sortType,isSearchDir,hiddenType,pageInfo);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<? extends BaseInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<? extends BaseInfo> infos) {
                        if(infos.isEmpty()){
                            stateManager.showEmpty();
                        }else {
                            stateManager.showContent();
                        }
                        mediaInfos.clear();
                        mediaInfos.addAll(infos);
                        adapter.setNewData(mediaInfos);

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        stateManager.showError(e.getMessage());
                        e.printStackTrace();

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
