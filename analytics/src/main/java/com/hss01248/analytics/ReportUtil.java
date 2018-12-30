package com.hss01248.analytics;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

/**
 * Created by hss on 2018/10/3.
 */

public class ReportUtil {



    private static Application app;

    public static void init(Application application,String GAtrackingId,boolean dryRun,boolean showLog){
        app = application;
        GoogleAnalysticTool.init(GAtrackingId,dryRun,showLog);
        regesterLifecycle(application);
    }

    private static void regesterLifecycle(Application application) {
        application.registerActivityLifecycleCallbacks(new ReportAppLifeCycleCallback());
        //谷歌分析自动注册,无需在这里
    }

    public static Application getApp() {
        return app;
    }

    public static void logHttpFail(String msg, String url, Map params,String uidAccount){
        GoogleAnalysticTool.logHttpFail(msg,url,params,uidAccount);

    }

    public static void logOtherFail(String msg){
        //GoogleAnalysticTool.logHttpFail("otherFail:"+msg);

    }

    public static void reportException(Exception e){
        GoogleAnalysticTool.reportException(e);

    }

    public static void setUid(String uid){

    }


}
