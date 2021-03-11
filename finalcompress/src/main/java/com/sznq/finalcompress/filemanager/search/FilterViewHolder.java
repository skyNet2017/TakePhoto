package com.sznq.finalcompress.filemanager.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.view.viewholder.CommonViewHolder;
import com.noober.menu.FloatMenu;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.HolderSearchFilterBinding;
import com.yyydjk.library.DropDownMenu;

import java.util.ArrayList;
import java.util.List;

public class FilterViewHolder extends CommonViewHolder<String, HolderSearchFilterBinding>{


    public FilterViewHolder(@Nullable LayoutInflater inflater, @NonNull LifecycleOwner lifecycleOwner,
                            @Nullable ViewGroup parent, boolean attachToParent) {
        super(inflater, lifecycleOwner, parent, attachToParent);
    }

    @Override
    protected void initDataAndEventInternal(LifecycleOwner lifecycleOwner, String bean) {
        binding.tvDisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiskFilterMenu(v);
            }
        });
        binding.tvIsDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDirFilterMenu(v);
            }
        });
        binding.tvSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);
            }
        });
        binding.tvType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTypeFilterMenu(v);
            }
        });

    }

    private void showTypeFilterMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[10];
        desc[0] = "全部";
        desc[1] ="图片和视频";
        desc[2] ="只有图片";
        desc[3] ="只有视频";
        desc[4] ="只有音频";
        desc[5] ="pdf";
        desc[6] ="doc";
        desc[7] ="ppt";
        desc[8] ="excel";
        desc[9] ="txt";

        desc[mediaType] =  desc[mediaType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                mediaType = position;
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);
    }

    private void showSortMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[10];
        desc[0] = "按文件夹容量从大到小";
        desc[1] ="按文件个数从大到小";
        desc[2] ="按更新时间 新在前";
        desc[3] ="按更新时间顺序 旧在前";
        desc[4] ="按文件夹名 顺序";
        desc[5] ="按文件夹名  倒序";
        desc[6] ="按路径 顺序";
        desc[7] ="按路径  倒序";
        desc[8] ="按时长 顺序";
        desc[9] ="按时长  倒序";

        desc[sortType] =  desc[sortType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                sortType = position;
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);
    }

    boolean isSearchDir;
    int diskType = 0;
    int sortType = 0;
    int mediaType = 0;
    private void showDirFilterMenu(View v) {
        isSearchDir = !isSearchDir;
        doSearch();
    }

    private void doSearch() {
        ToastUtils.showLong("搜索:xxxx");

    }


    private void showDiskFilterMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[5];
        desc[0] = "全部";
        desc[1] ="仅手机存储卡";
        desc[2] ="手机存储卡和http服务器";
        desc[3] ="仅http服务器";
        desc[4] ="具体某台http服务器//todo";
        desc[diskType] =  desc[diskType] +"(now)";
        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                diskType = position;
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);
    }
}
