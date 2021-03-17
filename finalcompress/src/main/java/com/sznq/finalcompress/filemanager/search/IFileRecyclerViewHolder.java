package com.sznq.finalcompress.filemanager.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.view.viewholder.CommonViewHolder;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.HolderRecyclerviewBinding;
import com.sznq.finalcompress.filemanager.FolderViewActivity;

import java.util.ArrayList;
import java.util.List;

public class IFileRecyclerViewHolder extends CommonViewHolder<String, HolderRecyclerviewBinding> {
    public IFileRecyclerViewHolder(@Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner,
                                   @Nullable ViewGroup parent, boolean attachToParent) {
        super(inflater, lifecycleOwner, parent, attachToParent);
        initRecycleview();
        binding.fastscroll.setRecyclerView(binding.recycler);
        binding.fastscroll.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, String bean) {

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
            binding.recycler.setLayoutManager(new GridLayoutManager(rootView.getContext(),3));
            adapter = new MediaItemImgAdapter(R.layout.item_file_img);
        }else if(displayType ==1){
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
            adapter = new MediaItemImgAdapter(R.layout.item_file_img);
        }else {
            binding.recycler.setLayoutManager(new LinearLayoutManager(rootView.getContext(),LinearLayoutManager.VERTICAL,false));
            adapter = new MediaItemAdapter(R.layout.item_file);
        }
        binding.recycler.setAdapter(adapter);
        adapter.setNewData(mediaInfos);
        initAdapter();
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
                    //FolderViewActivity.goTo(SearchActivity.this,info.getPath(),folderInfo.getMediaType(),"","");
                }else {
                   // openFile((BaseMediaInfo) info,mediaInfos);
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
               // showMenu(view,position);
                return true;
            }
        });
    }
}
