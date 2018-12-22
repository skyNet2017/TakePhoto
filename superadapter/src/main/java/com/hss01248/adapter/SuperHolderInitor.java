package com.hss01248.adapter;

/**
 * Created by huangshuisheng on 2018/3/28.
 */

public class SuperHolderInitor {

    public static IBindView getButterKnife() {
        return iButterKnife;
    }

    private static IBindView iButterKnife;


    /**
     * 传入butterknife的bind代码
     * @param iBindView
     */
    public static void init(IBindView iBindView){
        iButterKnife = iBindView;
    }
}
