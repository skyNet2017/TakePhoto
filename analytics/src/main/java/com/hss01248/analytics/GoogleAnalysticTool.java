package com.hss01248.analytics;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by hss on 2018/10/3.
 */

public class GoogleAnalysticTool {

    //google analystic
    private static Tracker tracker;
    private static String trackingId;
    private static boolean dryRun, showLog;

    public static void init(String trackingId,boolean dryRun,boolean showLog){
        GoogleAnalysticTool.trackingId = trackingId;
        GoogleAnalysticTool.dryRun = dryRun;
        GoogleAnalysticTool.showLog = showLog;
        getTracker();
    }

    public static Tracker getTracker() {
        if(tracker == null){
            synchronized (GoogleAnalysticTool.class){
                if(tracker == null){
                    GoogleAnalytics analytics = GoogleAnalytics.getInstance(ReportUtil.getApp());
                    analytics.setDryRun(dryRun);
                    analytics.getLogger()
                            .setLogLevel(showLog ? Logger.LogLevel.VERBOSE : Logger.LogLevel.ERROR);
                    tracker =analytics.newTracker(trackingId);
                    tracker.enableAutoActivityTracking(true);
                    tracker.enableExceptionReporting(true);
                }
            }

        }
        return tracker;
    }

    public static void logHttpFail(String msg, String url, Map params,String uidAccount){
        String str = "";
        JSONObject object = new JSONObject();
        try {
            object.put("type","http");
            object.put("msg",msg);
            object.put("url",url);
            object.put("params",params ==null ? "":params.toString());
            object.put("uidAccount",uidAccount);
            str = object.toString(2);
        }catch (Exception e){
            e.printStackTrace();
            str = msg+"-"+url+"-"+params+"-"+uidAccount;
        }
        getTracker().send(new HitBuilders.ExceptionBuilder().setDescription(str).setFatal(false).build());
    }

    public static void reportException(Exception e){
        getTracker().send(new HitBuilders.ExceptionBuilder()
                .setDescription(new StandardExceptionParser(ReportUtil.getApp(), null)              // Context and optional collection of package names
                                .getDescription(Thread.currentThread().getName(), e))
                .setFatal(false)
                .build());
    }

    public static void setUid(String uid){
        getTracker().set("uid", uid);

    }


}
