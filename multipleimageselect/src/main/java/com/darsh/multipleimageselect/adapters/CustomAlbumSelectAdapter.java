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
        String desc = album.name+"\n"+ImageInfoFormater.formatFileSize(album.fileSize)+", "+ album.count+" "+typeDes(album.type);

        viewHolder.textView.setText(desc);
        Glide.with(context)
                .load(arrayList.get(position).cover)
                .placeholder(R.drawable.image_placeholder).centerCrop().into(viewHolder.imageView);


        return convertView;
    }

    private String typeDes(int type) {
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
