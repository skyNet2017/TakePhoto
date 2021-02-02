package com.sznq.finalcompress;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.annotation.RequiresApi;
import android.util.Log;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        Log.d("监听job","MyJobService-onStartJob:"+bundle.getString("fileName")+", thread:"+Thread.currentThread().getName()+", id:"+params.getJobId());
        if(bundle != null){
            String fileName = bundle.getString("fileName");
            String dir = bundle.getString("dir");
            MyImageWatcher.doBg(fileName,new File(dir));
        }
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("监听job","MyJobService-onStopJob:id:"+params.getJobId());
        return false;//返回false表示停止后不再重试执行
    }
}