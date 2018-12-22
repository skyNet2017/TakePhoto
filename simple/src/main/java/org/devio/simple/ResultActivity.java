package org.devio.simple;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.hss01248.adapter.SuperPagerAdapter;
import com.hss01248.adapter.SuperPagerHolder;
import com.light.body.Light;
import com.light.body.LightConfig;
import org.devio.takephoto.model.TImage;

import java.util.ArrayList;
import java.util.List;


/**
 * - 支持通过相机拍照获取图片
 * - 支持从相册选择图片
 * - 支持从文件选择图片
 * - 支持多图选择
 * - 支持批量图片裁切
 * - 支持批量图片压缩
 * - 支持对图片进行压缩
 * - 支持对图片进行裁剪
 * - 支持对裁剪及压缩参数自定义
 * - 提供自带裁剪工具(可选)
 * - 支持智能选取及裁剪异常处理
 * - 支持因拍照Activity被回收后的自动恢复
 * Author: crazycodeboy
 * Date: 2016/9/21 0007 20:10
 * Version:4.0.0
 * 技术博文：http://www.devio.org
 * GitHub:https://github.com/crazycodeboy
 * Email:crazycodeboy@gmail.com
 */
public class ResultActivity extends Activity {
    public static ArrayList<TImage> images;
    ViewPager viewPager;
    SuperPagerAdapter<Activity> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_layout);
        ArrayList<TImage> images = (ArrayList<TImage>) getIntent().getSerializableExtra("images");
        if(images != null){
            ResultActivity.images = images;
        }

        /*LightConfig lightConfig = new LightConfig();
        lightConfig.setDefaultQuality(50);
        lightConfig.setMaxWidth(2000);
        lightConfig.setMaxHeight(2000);
        Light.getInstance().setConfig(lightConfig);*/


        showImg();
    }

    private void showImg() {
        viewPager = findViewById(R.id.vp);
        adapter = new SuperPagerAdapter<Activity>(this) {
            @Override
            protected SuperPagerHolder generateNewHolder(Activity context, ViewGroup viewGroup, int i) {
                return new BigPagerHolder(context,viewGroup);
            }

            @Override
            public List getListData() {
                return images;
            }
        };
        viewPager.setAdapter(adapter);
        adapter.addAll(images);
       /* LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llImages);
        for (int i = 0, j = images.size(); i < j - 1; i += 2) {
            View view = LayoutInflater.from(this).inflate(R.layout.image_show, null);
            ImageView imageView1 = (ImageView) view.findViewById(R.id.imgShow1);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.imgShow2);
            Glide.with(this).load(new File(images.get(i).getCompressPath())).into(imageView1);
            Glide.with(this).load(new File(images.get(i + 1).getCompressPath())).into(imageView2);
            linearLayout.addView(view);
        }
        if (images.size() % 2 == 1) {
            View view = LayoutInflater.from(this).inflate(R.layout.image_show, null);
            ImageView imageView1 = (ImageView) view.findViewById(R.id.imgShow1);
            Glide.with(this).load(new File(images.get(images.size() - 1).getCompressPath())).into(imageView1);
            linearLayout.addView(view);
        }*/

    }
}
