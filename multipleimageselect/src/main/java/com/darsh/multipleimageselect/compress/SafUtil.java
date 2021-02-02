package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.fragment.app.FragmentActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hss01248.activityresult.ActivityResultListener;
import com.hss01248.activityresult.StartActivityUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class SafUtil {

    public static DocumentFile getRoot(Activity activity){
        SharedPreferences sp = activity.getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
        String uriTree = sp.getString("uriTree", "");
        if(TextUtils.isEmpty(uriTree)){
            return null;
        }
        Uri uri = Uri.parse(uriTree);
        final int takeFlags = activity.getIntent().getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
            DocumentFile root = DocumentFile.fromTreeUri(activity.getApplicationContext(), uri);
            Log.d("dd5", uriTree);
            return root;
        }
        return null;
    }

    public static void getRootDir(FragmentActivity activity, ISdRoot callback){
        StorageUtils.context = activity.getApplicationContext();
        SharedPreferences sp = activity.getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
        String uriTree = sp.getString("uriTree", "");
        Log.d("dd",uriTree);
        if (TextUtils.isEmpty(uriTree)) {
            // 重新授权
            requestSaf(activity,callback);
        } else {
            try {
                Uri uri = Uri.parse(uriTree);
                final int takeFlags = activity.getIntent().getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    activity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    DocumentFile root = DocumentFile.fromTreeUri(activity.getApplicationContext(), uri);
                    Log.d("dd2",uriTree);
                    if(root == null){
                        callback.onPermissionDenied(7,"DocumentFile.fromTreeUri return null");
                        return;
                    }
                    callback.onPermissionGet(root);
                }else {
                    callback.onPermissionDenied(9,"android version is below 4.4");
                }

            } catch (SecurityException e) {
                e.printStackTrace();
                // 重新授权
                requestSaf(activity,callback);
            }
        }

    /*    作者：唯鹿
        链接：https://juejin.im/post/6844904058743078919
        来源：掘金
        著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。*/
    }

    private static void requestSaf(final FragmentActivity activity, final ISdRoot callback) {
        // 用户可以选择任意文件夹，将它及其子文件夹的读写权限授予APP。
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        StartActivityUtil.goOutAppForResult((AppCompatActivity) activity, intent, new ActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if(resultCode == Activity.RESULT_OK){
                    Uri uriTree = null;
                    if (data != null) {
                        uriTree = data.getData();
                    }
                    if (uriTree != null) {
                        Log.d("dd3",uriTree.toString());
                        // 创建所选目录的DocumentFile，可以使用它进行文件操作
                        DocumentFile root = DocumentFile.fromTreeUri(activity.getApplicationContext(), uriTree);
                        // 比如使用它创建文件夹


                        Log.d("dd3",root.getUri()+"");
                        if(root == null){
                            callback.onPermissionDenied(7,"DocumentFile.fromTreeUri return null");
                            return;
                        }
                        // 保存获取的目录权限
                        SharedPreferences sp = activity.getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("uriTree", uriTree.toString());
                        editor.apply();
                        callback.onPermissionGet(root);
                    }else {
                        Log.w("dd4","uri == null");
                        callback.onPermissionDenied(resultCode,"data in intent of reaultback is null");
                    }
                }else {
                    callback.onPermissionDenied(resultCode,"onResultError");
                }
            }

            @Override
            public void onActivityNotFound(Throwable e) {

            }
        });
        Toast.makeText(activity,"请选择SD卡根目录并允许访问",Toast.LENGTH_LONG).show();
        /*RxActivityUtil.jump(activity, intent, new RxActivityCallback() {
            @Override
            public void onResultOk(int resultCode, Intent data) {
                Uri uriTree = null;
                if (data != null) {
                    uriTree = data.getData();
                }
                if (uriTree != null) {
                    Log.d("dd3",uriTree.toString());
                    // 创建所选目录的DocumentFile，可以使用它进行文件操作
                    DocumentFile root = DocumentFile.fromTreeUri(activity.getApplicationContext(), uriTree);
                    // 比如使用它创建文件夹


                    Log.d("dd3",root.getUri()+"");
                    if(root == null){
                        callback.onPermissionDenied(7,"DocumentFile.fromTreeUri return null");
                        return;
                    }
                    // 保存获取的目录权限
                    SharedPreferences sp = activity.getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("uriTree", uriTree.toString());
                    editor.apply();
                    callback.onPermissionGet(root);
                }else {
                    Log.w("dd4","uri == null");
                    callback.onPermissionDenied(resultCode,"data in intent of reaultback is null");
                }
            }

            @Override
            public void onResultError(int resultCode, Intent data) {
                callback.onPermissionDenied(resultCode,"onResultError");
            }
        });
        Toast.makeText(activity,"请选择存储根目录并允许访问",Toast.LENGTH_LONG).show();*/
    }


    public interface ISdRoot{
        void onPermissionGet(DocumentFile dir);

        void onPermissionDenied(int resultCode, String msg);
    }


    private static String readFile(DocumentFile file,Context context) {
        try {
            //file.getUri()
            InputStreamReader reader = new InputStreamReader(context.getContentResolver().openInputStream(file.getUri()));
            BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
            StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
            String s = "";
            while ((s =bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s);//将读取的字符串添加换行符后累加存放在缓存中
                System.out.println(s);
            }
            bReader.close();
            String str = sb.toString();
            System.out.println(str );
            return str;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void alterDocument(Uri uri,String content,Context context) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
