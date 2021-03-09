package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.helpers.LoggingListener;
import com.darsh.multipleimageselect.models.Image;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.smb.SmbToHttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends CustomGenericAdapter<BaseMediaInfo> {
    public CustomImageSelectAdapter(Context context, List<BaseMediaInfo> images) {
        super(context, images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_image_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_image_select);
            viewHolder.view = convertView.findViewById(R.id.view_alpha);
            viewHolder.tvInfo = convertView.findViewById(R.id.tv_info);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.view.getLayoutParams().width = size;
        viewHolder.view.getLayoutParams().height = size;

        if (false) {//arrayList.get(position).isSelected
            viewHolder.view.setAlpha(0.5f);
            ((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_done_white));

        } else {
            viewHolder.view.setAlpha(0.0f);
            ((FrameLayout) convertView).setForeground(null);
        }

        BaseMediaInfo image = arrayList.get(position);
        viewHolder.image = image;
        Log.w(SafUtil.TAG,"dir22: images show:   "+image.pathOrUri);
        if(image.pathOrUri.startsWith("content:/") && !image.pathOrUri.startsWith("content://")){
            image.pathOrUri = image.pathOrUri.replace("content:/","content://");
        }
        //.FileNotFoundException: content:/com.android.externalstorage.documents/tree/0123-4567
        Uri uri = null;
        if(image.pathOrUri.startsWith("content")){
            uri = Uri.parse(image.pathOrUri);
        }else if(image.pathOrUri.startsWith("/storage/")){
            uri = Uri.fromFile(new File(image.pathOrUri));
        }else if(image.pathOrUri.startsWith("smb")){
            String url = SmbToHttp.getHttpUrlFromSmb(image.pathOrUri);
            uri = Uri.parse(url);
        }else {
            uri = Uri.parse(image.pathOrUri);
        }
        if(image.pathOrUri.startsWith("http") || image.pathOrUri.startsWith("smb")){
            if(image.type != BaseMediaInfo.TYPE_IMAGE){
                viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.c_text_bg));
                viewHolder.tvInfo.setText(image.pathOrUri+"\n"+ ImageInfoFormater.formatFileSize(image.fileSize)+" 点赞:"+image.praiseCount);
                return convertView;
            }
        }
        android.util.Log.i("GLIDE", "start load url:"+URLDecoder.decode(uri.toString()));
        Glide.with(context)
                .load(uri)
                .thumbnail(0.2f)
                //.diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new LoggingListener<>())
                .placeholder(R.drawable.image_placeholder).into(viewHolder.imageView);
        ViewHolder viewHolder1 = viewHolder;
        //viewHolder.tvInfo.setText("");
        if(viewHolder.image.pathOrUri.startsWith("smb") || viewHolder.image.pathOrUri.startsWith("http") ){
            viewHolder.tvInfo.setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1)+"\n"
                    + ImageInfoFormater.formatFileSize(image.fileSize)
                    +" "+ImageInfoFormater.formatTime(image.updatedTime)+" 点赞:"+image.praiseCount);
            return convertView;
        }

        Observable.just(viewHolder1)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<ViewHolder>() {
                    @Override
                    public void accept(ViewHolder viewHolder) throws Exception {
                        //viewHolder.desc = formatImagInfo(viewHolder.image,false,context);
                        //viewHolder.desc = ImageInfoFormater.formatImagInfo(viewHolder.image.pathOrUri,false);
                        if(viewHolder.image.type == 1){
                            viewHolder.desc = ImageInfoFormater.formatImagInfo(viewHolder.image.pathOrUri,false);
                        }else {
                            boolean getInfoFail = false;
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            try {
                                if(viewHolder.image.pathOrUri.startsWith("content")){
                                    retriever.setDataSource(context,Uri.parse(viewHolder.image.pathOrUri));
                                }else if(viewHolder.image.pathOrUri.startsWith("/storage/")){
                                    retriever.setDataSource(viewHolder.image.pathOrUri);
                                }else if(viewHolder.image.pathOrUri.contains("smb")){
                                    /*retriever.setDataSource(SmbToHttp.getHttpUrlFromSmb(image.pathOrUri),new HashMap<>());
                                   Bitmap bitmap = retriever.getFrameAtTime();
                                   if(bitmap!=null){
                                       parent.post(new Runnable() {
                                           @Override
                                           public void run() {
                                               viewHolder.imageView.setImageBitmap(bitmap);
                                           }
                                       });
                                   }*/
                                }

                            }catch (Throwable throwable){
                                getInfoFail = true;
                                throwable.printStackTrace();

                            }



                            int duration = toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;//视频的长度 s
                            String desc = "";
                            if(viewHolder.image.type == BaseMediaInfo.TYPE_VIDEO){
                                int width = toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); //宽
                                int height = toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); //高
                                String ro = "";
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    int rotation = toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));//视频的方向角度
                                    if(rotation > 0){
                                        ro = " "+rotation+"°";
                                    }
                                }
                                desc = width+"x"+height+ro+"\n"+ImageInfoFormater.formatFileSize(ImageInfoFormater.getFileLen(viewHolder.image.pathOrUri))+"   "+formatTime(duration);
                            }else {
                                String path2 = URLDecoder.decode(viewHolder.image.pathOrUri);
                                if (path2.contains(":")) {
                                    path2 = path2.substring(path2.lastIndexOf(":") + 1);
                                }
                                if(path2.contains("/")){
                                    path2 = path2.substring(path2.lastIndexOf("/")+1);
                                }
                                desc = ImageInfoFormater.formatFileSize(ImageInfoFormater.getFileLen(viewHolder.image.pathOrUri))+"   "+formatTime(duration)+"\n"+path2;
                            }
                            if(getInfoFail){
                                viewHolder.desc = URLDecoder.decode(viewHolder.image.pathOrUri);
                                viewHolder.desc = viewHolder.desc.substring(viewHolder.desc.lastIndexOf("/")+1);
                            }else {
                                viewHolder.desc = desc;
                            }

                        }

                    }
                })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ViewHolder>() {
                    @Override
                    public void accept(ViewHolder viewHolder3) throws Exception {
                        if(viewHolder3.image.equals(viewHolder.image)){
                            /*if(viewHolder.image.quality > PhotoCompressHelper.DEFAULT_QUALITY ){
                                viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color_not_compressed));
                            }else {
                                viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color));
                            }*/
                            viewHolder.tvInfo.setText(viewHolder.desc+" 点赞:"+image.praiseCount);
                        }else {
                            //viewHolder.tvInfo.setText("");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                    }
                });

       /* String desc = formatImagInfo(image,false,viewHolder.imageView.getContext());

        if(image.quality > PhotoCompressHelper.DEFAULT_QUALITY ){
            viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color_not_compressed));
        }else {
            viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color));
        }
        viewHolder.tvInfo.setText(desc);*/
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

    public static int toInt(Object o, int defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        int value;
        try {
            String s = o.toString().trim();
            if (s.contains(".")) {
                value = Integer.valueOf(s.substring(0, s.lastIndexOf(".")));
            } else {
                value = Integer.valueOf(s);
            }
        } catch (Exception e) {
            value = defaultValue;
        }

        return value;
    }

    public static int toInt(Object o) {
        return toInt(o, 0);
    }

    private static class ViewHolder {
        public ImageView imageView;
        public View view;
        public TextView tvInfo;
        public BaseMediaInfo image;
        public String  desc;
    }
}
