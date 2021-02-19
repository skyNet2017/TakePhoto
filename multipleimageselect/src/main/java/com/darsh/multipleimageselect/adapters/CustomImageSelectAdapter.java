package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.models.Image;
import com.hss01248.imginfo.ImageInfoFormater;

import java.io.File;
import java.util.ArrayList;

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
public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {
    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
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

        if (arrayList.get(position).isSelected) {
            viewHolder.view.setAlpha(0.5f);
            ((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_done_white));

        } else {
            viewHolder.view.setAlpha(0.0f);
            ((FrameLayout) convertView).setForeground(null);
        }

        Image image = arrayList.get(position);
        viewHolder.image = image;
        Glide.with(context)
                .load(image.path)
                .placeholder(R.drawable.image_placeholder).into(viewHolder.imageView);
        ViewHolder viewHolder1 = viewHolder;
        //viewHolder.tvInfo.setText("");

        Observable.just(viewHolder1)
                .doOnNext(new Consumer<ViewHolder>() {
                    @Override
                    public void accept(ViewHolder viewHolder) throws Exception {
                        viewHolder.desc = formatImagInfo(viewHolder.image,false,context);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ViewHolder>() {
                    @Override
                    public void accept(ViewHolder viewHolder3) throws Exception {
                        if(viewHolder3.image.equals(viewHolder.image)){
                            if(viewHolder.image.quality > PhotoCompressHelper.DEFAULT_QUALITY ){
                                viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color_not_compressed));
                            }else {
                                viewHolder.tvInfo.setTextColor(viewHolder.imageView.getResources().getColor(R.color.img_tv_color));
                            }
                            viewHolder.tvInfo.setText(viewHolder.desc);
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

    public static String formatImagInfo(Image image, boolean showFullPath,Context context) {
        String path = image.path;
        File file = new File(path);
        String size = ImageInfoFormater.formatFileSize(file.length());
        int[] wh = ImageInfoFormater.getImageWidthHeight(path);
        int quality = image.quality;
        //if(quality < 0){
            quality = ImageInfoFormater.getQuality(path);
            image.quality = quality;
       // }
        String needCompress = quality > PhotoCompressHelper.DEFAULT_QUALITY ? context.getString(com.hss01248.imginfo.R.string.c_not_compressed) : context.getString(com.hss01248.imginfo.R.string.t_compressed);
        String str = wh[0] + "x" + wh[1] + ", " + size + context.getString(com.hss01248.imginfo.R.string.c_quality_info) + quality + needCompress;
        if (showFullPath) {
            return str + "\n" + path;

        } else {
            return str;
        }
    }

    private static class ViewHolder {
        public ImageView imageView;
        public View view;
        public TextView tvInfo;
        public Image image;
        public String  desc;
    }
}
