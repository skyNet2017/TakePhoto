package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.helpers.LoggingListener;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.smb.SmbToHttp;

import java.io.File;
import java.net.URLDecoder;
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
        String desc = ImageInfoFormater.formatFileSize(album.fileSize)+", "+ album.count+" "+FileTypeUtil.getDesc(album.mediaType);
        if(album.mediaType != BaseMediaInfo.TYPE_IMAGE){
            desc = album.path +"\n"+desc;
        }else {
            desc =  album.name+"\n"+desc;
        }
        if(album.duration >0){
            desc = desc+"\n"+formatTime(album.duration);
        }

        Uri uri1 = Uri.parse(album.path);
        if(uri1 != null){
            String scheme = uri1.getScheme();
            if(TextUtils.isEmpty(scheme)){
                scheme = "file";
            }
            desc = desc+" "+scheme;
        }


        viewHolder.textView.setText(desc);

        String cover = arrayList.get(position).cover;

        if(cover.startsWith("http") || cover.startsWith("smb")){
            if(FileTypeUtil.getTypeByFileName(cover) != BaseMediaInfo.TYPE_IMAGE){
                viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.c_text_bg));
                return convertView;
            }
        }
        Uri uri = null;
        if(cover.startsWith("content")){
            uri = Uri.parse(cover);
        }else if(cover.startsWith("/storage/")){
            uri = Uri.fromFile(new File(cover));
        }else if(cover.startsWith("smb")){
            String url = SmbToHttp.getHttpUrlFromSmb(cover);
            uri = Uri.parse(url);
        }else {
            uri = Uri.parse(cover);
        }

        android.util.Log.i("GLIDE", "start load url:"+ URLDecoder.decode(uri.toString()));
        Glide.with(context)
                .load(uri)
                .thumbnail(0.2f)
                .listener(new LoggingListener<>())
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
