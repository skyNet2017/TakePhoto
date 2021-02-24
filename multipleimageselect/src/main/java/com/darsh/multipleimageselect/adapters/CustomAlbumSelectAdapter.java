package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.graphics.ImageFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.models.Album;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Darshan on 4/14/2015.
 */
public class CustomAlbumSelectAdapter extends CustomGenericAdapter<BaseMediaFolderInfo> {
    public CustomAlbumSelectAdapter(Context context, List<BaseMediaFolderInfo> albums) {
        super(context, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_album_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_album_image);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view_album_name);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BaseMediaFolderInfo album = arrayList.get(position);
        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;
        String desc = ImageInfoFormater.formatFileSize(album.fileSize)+", "+ album.count+" "+typeDes(album.type);
        if(album.type == BaseMediaInfo.TYPE_AUDIO){
            desc = album.pathOrUri+"\n"+desc;
        }else {
            desc =  album.name+"\n"+desc;
        }
        if(album.duration >0){
            desc = desc+"\n"+formatTime(album.duration);
        }


        viewHolder.textView.setText(desc);
        Glide.with(context)
                .load(arrayList.get(position).cover)
                .placeholder(R.drawable.image_placeholder).centerCrop().into(viewHolder.imageView);


        return convertView;
    }

    public String formatTime(long duration) {
        if(duration < 60){
            return duration+"s";
        }
        if(duration< 3600){
            return duration/60+"min "+duration % 60 +"s";
        }
        int hour = (int) (duration / 3600);
        duration = duration % 3600;
        return hour +"h "+ duration/60+"min "+duration % 60 +"s";

    }

    public static String typeDes(int type) {
        if(type ==1){
            return "图";
        }else if(type == 2){
            return "视频";
        }else if(type == 3){
            return "音频";
        }
        return "未知";
    }

    private static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
