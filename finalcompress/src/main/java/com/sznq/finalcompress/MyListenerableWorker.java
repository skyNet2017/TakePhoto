package com.sznq.finalcompress;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

public class MyListenerableWorker extends ListenableWorker {

    static IDoWork work;
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public MyListenerableWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        if(work != null){
            //work.exe()
        }
        return null;
    }

    public interface IDoWork{
        Object exe(Map params);
    }
}
