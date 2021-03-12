package com.sznq.finalcompress.filemanager.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
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
        binding.tvHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHiddenMenu(v);
            }
        });
        binding.tvSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSizeFilterMenu(v);
            }
        });

    }

   static int sizeType;
    private void showSizeFilterMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";

        String[] desc = new String[11];
        desc[0] = ">50kB";
        desc[1] ="全部";
        desc[2] =">1KB";
        desc[3] =">500KB";
        desc[4] =">1MB";
        desc[5] =">10MB";
        desc[6] =">100MB";
        desc[7] =">1GB";
        desc[8] ="50kB-10M";
        desc[9] ="50kB-100M";
        desc[10] ="50kB-1GB";
        desc[sizeType] =  desc[sizeType] +"(now)";
        floatMenu.items(desc);

        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                sizeType = position;
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);
    }

    private void showHiddenMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[3];
        desc[0] = "全部";
        desc[1] ="仅搜索公开的内容";
        desc[2] ="仅搜索隐藏的内容";
        desc[hiddenType] =  desc[hiddenType] +"(now)";
        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                hiddenType = position;
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);
    }

    private void showTypeFilterMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[11];
        desc[0] ="图片和视频";
        desc[1] ="只有图片";
        desc[2] ="只有视频";
        desc[3] ="只有音频";
        desc[4] ="全部文档";
        desc[5] ="pdf";
        desc[6] ="doc";
        desc[7] ="ppt";
        desc[8] ="excel";
        desc[9] ="txt";
        desc[10] ="全部office";


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
        String[] desc = new String[13];
        desc[0] ="按更新时间 新在前";
        desc[1] ="按更新时间顺序 旧在前";
        desc[2] = "文件大小从大到小";
        desc[3] ="文件大小从小到大";
        desc[4] ="按文件名 顺序";
        desc[5] ="按文件名  倒序";

        desc[6] ="按画面尺寸 高分辨率在前";
        desc[7] ="按画面尺寸 低分辨率在前";
        desc[8] ="按文件路径 顺序";
        desc[9] ="按文件路径  倒序";

        if(mediaType == BaseMediaInfo.TYPE_VIDEO || mediaType == BaseMediaInfo.TYPE_AUDIO){
            desc[10] = "按时长 长在前";
            desc[11] ="按时长 短在前";
        }
        desc[12] ="按点赞数 顺序";

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

    static  boolean isSearchDir;
    static  int diskType = 0;
    static int sortType = 0;
    static int mediaType = 0;
    static int hiddenType = 0;
    private void showDirFilterMenu(View v) {
        final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[2];
        desc[0] = "文件夹";
        desc[1] ="文件";
        if(isSearchDir){
            desc[0] =  desc[0] +"(now)";
        }else {
            desc[1] =  desc[1] +"(now)";
        }

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position ==0){
                    isSearchDir = true;
                }else {
                    isSearchDir = false;
                }
                doSearch();
            }
        });
        floatMenu.showAsDropDown(v);

    }

    SearchActivity activity;
    public void setActivity(SearchActivity activity){
        this.activity = activity;
    }

    private void doSearch() {
        ToastUtils.showLong("搜索:xxxx");
        activity.doSearch();

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
