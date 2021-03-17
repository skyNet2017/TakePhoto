package com.sznq.finalcompress.filemanager.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.darsh.multipleimageselect.FileOpenUtil;
import com.darsh.multipleimageselect.compress.CompressResultCompareActivity;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.pagestate.PageStateManager;
import com.noober.menu.FloatMenu;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.ActivitySearchBinding;
import com.sznq.finalcompress.filemanager.FolderViewActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
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
        stateManager = PageStateManager.initWhenUse(binding.fastContainer,null);
        filterViewHolder.setActivity(this);


        initRecycleview();
        binding.fastscroll.setRecyclerView(binding.recycler);
        binding.fastscroll.setVisibility(View.VISIBLE);
       // fastScroller.setRecyclerView(binding.recycler);
      //  fastScroller.setVisibility(VISIBLE);
        binding.titlebar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });

        binding.titlebar.getCenterSearchEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    doSearch();
                    return true;
                }
                return false;

            }
        });
        binding.titlebar.getCenterSearchRightImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.titlebar.getCenterSearchEditText().setText("");
            }
        });


        doSearch();
        initAdapter();


        binding.tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = pageInfo[0]+1;
                if(idx <= pageInfo[1]){
                    pageInfo[0]++;
                    doSearch(true);
                }else {
                    ToastUtils.showLong("已经是最后一页");
                }

            }
        });

        binding.tvPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int idx = pageInfo[0] - 1;
                if (idx >=0){
                    pageInfo[0] = idx;
                    doSearch(true);
                }
            }
        });
        binding.sbPager.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pageInfo[0] = seekBar.getProgress();
                doSearch(true);
            }
        });
    }

    private void initAdapter() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BaseInfo info = mediaInfos.get(position);
                //IFile file =  files.get(position);
                if(info instanceof BaseMediaFolderInfo){
                    BaseMediaFolderInfo folderInfo = (BaseMediaFolderInfo) info;
                    //listFiles(file);
                    FolderViewActivity.goTo(SearchActivity.this,info.getPath(),folderInfo.getMediaType(),"","");
                }else {
                    openFile((BaseMediaInfo) info,mediaInfos);
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                showMenu(view,position);
                return true;
            }
        });
    }

    private void showMenu(View v, int position0) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        String hide = mediaInfos.get(position0).getHidden() == 0 ? "隐藏此文件夹":"取消此文件夹的隐藏";
        String[] desc = new String[2];
        desc[0] = "显示exif/metadata信息";
        desc[1] = hide;
        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position ==0){
                    CompressResultCompareActivity.showExif(SearchActivity.this,mediaInfos.get(position0).getPath());
                }else {
                    hideFolder(mediaInfos.get(position0).getPath(),mediaInfos.get(position0));

                }

            }
        });
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        Point point = new Point();
        point.x = location[0];
        point.y =location[1];
        floatMenu.show(point);
    }

    private void hideFolder(String path, BaseInfo baseInfo) {
        HiddenUtil.switchHidePath(path,baseInfo);
    }

    List<BaseInfo> mediaInfos = new ArrayList<>();
    BaseQuickAdapter adapter;


    private void initRecycleview() {
        changeAdapter(FilterViewHolder.disPlayMode);


    }

    /**
     desc[0] ="表格";
     desc[1] ="瀑布流";
     desc[2] ="列表";
     */
    public void changeAdapter(int displayType) {
        if(displayType == 0){
            binding.recycler.setLayoutManager(new GridLayoutManager(this,3));
            adapter = new MediaItemImgAdapter(R.layout.item_file_img);
        }else if(displayType ==1){
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
            adapter = new MediaItemImgAdapter(R.layout.item_file_img);
        }else {
            binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter = new MediaItemAdapter(R.layout.item_file);
        }
        binding.recycler.setAdapter(adapter);
        adapter.setNewData(mediaInfos);
        initAdapter();
    }

    private void openFile(BaseMediaInfo file, List<BaseInfo> files) {
        String path = file.getPath();
        int type = FileTypeUtil.getTypeByFileName(path);
        List<String> paths = new ArrayList<>();
        int position = 0;
        int realP = 0;
        if(type == BaseMediaInfo.TYPE_IMAGE || type == BaseMediaInfo.TYPE_VIDEO){
            for (BaseInfo iFile : files) {
                if(iFile instanceof BaseMediaFolderInfo){
                    continue;
                }
                if(FileTypeUtil.getTypeByFileName(iFile.getPath()) == type){
                    if(iFile.equals(file)){
                        realP = position;
                    }
                    paths.add(iFile.getPath());
                    position++;
                }
            }
            FileOpenUtil.open(SearchActivity.this,file.getPath(),paths,realP);
        }else {
            FileOpenUtil.open(SearchActivity.this,file.getPath());
        }

    }

    String currentSearchKey;
    void doSearch(boolean isChangePage){
        KeyboardUtils.hideSoftInput(SearchActivity.this);
        if(!isChangePage){
            pageInfo = new int[]{0,0};
        }
        String word = binding.titlebar.getSearchKey();

        searchDB(word,FilterViewHolder.isSearchDir,FilterViewHolder.diskType,FilterViewHolder.mediaType,
                FilterViewHolder.sortType,FilterViewHolder.hiddenType,FilterViewHolder.sizeType);
    }

    void doSearch(){
        doSearch(false);
    }
    int[] pageInfo = new int[]{0,0};
    private void searchDB(String word, boolean isSearchDir, int diskType, int mediaType, int sortType,int hiddenType,int sizeType) {
        stateManager.showLoading();
        Observable.just(1)
                .subscribeOn(Schedulers.io())
                .map(new Function<Integer, List<? extends BaseInfo>>() {
                    @Override
                    public List<? extends BaseInfo> apply(@NonNull Integer integer) throws Exception {
                        return SearchDbUtil.searchItem(word,diskType,mediaType,sortType,isSearchDir,hiddenType,pageInfo,sizeType);
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
                            binding.recycler.scrollToPosition(0);
                        }
                        mediaInfos.clear();
                        mediaInfos.addAll(infos);
                        adapter.setNewData(mediaInfos);


                        if(pageInfo[1] > 0){
                            Log.w(SafUtil.TAG, " 需要分页:" );
                            binding.llPager.setVisibility(View.VISIBLE);
                            binding.tvPbInfo.setVisibility(View.VISIBLE);
                            binding.sbPager.setProgress(pageInfo[0]);
                            binding.sbPager.setMax(pageInfo[1]);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String text = pageInfo[0]+"/"+pageInfo[1];
                                binding.sbPager.setTooltipText(text);
                            }
                           binding.tvPbInfo.setText("page:"+(pageInfo[0]+1)+"/"+pageInfo[1]+",count:"+mediaInfos.size());
                        }else {
                            binding.llPager.setVisibility(View.GONE);
                            binding.tvPbInfo.setVisibility(View.GONE);
                            //titleBar.getLeftTextView().setText("count:"+images.size());
                        }


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
