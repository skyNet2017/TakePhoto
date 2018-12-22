package org.devio.simple.compress;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hss01248.notifyutil.NotifyUtil;

import org.devio.simple.R;
import org.devio.simple.ResultActivity;

/**
 * Created by hss on 2018/12/22.
 * 8.0以上接收不到静态广播,需要动态注册
 */

public class NewPhotoAddedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.w("NewPhotoAddedReceiver",intent.getData()+"");//file:///storage/emulated/0/Boohee/1545468993828.jpg
        Intent resultIntent = new Intent(context, CompressActivity.class);
        resultIntent.setData(intent.getData());
         PendingIntent resultPendingIntent = PendingIntent.getActivity(context,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotifyUtil.buildSimple(90, R.mipmap.ic_launcher,"新文件","点击去压缩处",resultPendingIntent).setHeadup().show();





    }
}
