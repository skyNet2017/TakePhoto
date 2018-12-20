package org.devio.takephoto.wrap;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.WindowManager;
import org.devio.takephoto.R;

/**
 * Created by huangshuisheng on 2018/12/20.
 */

public class TakePhotoUtil {



    public static void startPickOneWitchDialog(final FragmentActivity activity, final TakeOnePhotoListener listener){

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.t_activity_select_pic);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = activity.getResources().getDisplayMetrics().widthPixels;
        dialog.getWindow().setAttributes(params);
        dialog.show();
        dialog.getWindow().findViewById(R.id.btn_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickOne(activity,true,listener);
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        dialog.getWindow().findViewById(R.id.btn_pick_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickOne(activity,false,listener);
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        dialog.getWindow().findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });




    }

    public static void startPickOne(FragmentActivity activity,boolean fromCamera,TakeOnePhotoListener listener) {
        SysTakeOnePhotoTransFragment fragment = getTransFragment(activity.getSupportFragmentManager());
       fragment.setListener(listener)
               .pickFromCamera(fromCamera);
    }

    private static SysTakeOnePhotoTransFragment getTransFragment(FragmentManager fragmentManager) {
        SysTakeOnePhotoTransFragment fragment = findFragment(fragmentManager);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new SysTakeOnePhotoTransFragment();
            fragmentManager.beginTransaction().add(fragment, "TakePhotoUtil").commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    private static SysTakeOnePhotoTransFragment findFragment(FragmentManager fragmentManager) {
        return (SysTakeOnePhotoTransFragment)fragmentManager.findFragmentByTag("TakePhotoUtil");
    }


}
