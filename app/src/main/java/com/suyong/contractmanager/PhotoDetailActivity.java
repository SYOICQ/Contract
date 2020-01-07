package com.suyong.contractmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;


import com.suyong.contractmanager.Interfaces.registerInterface;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.pojo.Contract;
import com.suyong.contractmanager.utils.BitmapUtils;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.DownLoadUtil;
import com.suyong.contractmanager.utils.StorgeUtils;
import com.suyong.contractmanager.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

import uk.co.senab.photoview.PhotoView;

public class PhotoDetailActivity extends AppCompatActivity {

    private PhotoView photoView;
    private Button up_load;
    private Button down_load;
    private String ImageId ="";
    private static final int CHOOSE_PHOTO = 1;

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void messageEventBus(Contract contract){
        photoView.setImageBitmap(contract.getBitmap());
        ImageId = contract.getId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        init();
        EventBus.getDefault().register(this);
    }

    private void init() {
        photoView = findViewById(R.id.photoview);
        up_load = findViewById(R.id.up_load);
        up_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPermission();
            }
        });
        down_load = findViewById(R.id.down_load);
        down_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPermission1();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 查看权限
     */
    private void CheckPermission1() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            //开始下载
            StartDownload();
        }
    }

    private void StartDownload() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StorgeUtils util = new StorgeUtils(PhotoDetailActivity.this);
                DownLoadUtil.downloadFile("http://49.234.92.110:8080/MyWebDemo//DownLoadServlet?filename="+ImageId+".jpg",util.getdownLocalpatch()+ImageId+".jpg",new registerInterface() {
                    @Override
                    public void Sucess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(PhotoDetailActivity.this,"下载成功!");
                            }
                        });
                    }

                    @Override
                    public void failed(final String returnCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(PhotoDetailActivity.this,returnCode);
                            }
                        });
                    }
                });
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    private void CheckPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // 调用系统相册
            openAlbum();
        }
    }
    @Override
    // 申请用户权限
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    // 用户允许授权
                    /* ————调用系统相册————*/
                    openAlbum();
                } else {
                    // 用户拒绝授权
                    ToastUtil.showToast(this, "You denied the permission");
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    // 用户允许授权
                    /* 开始下载*/
                    StartDownload();
                } else {
                    // 用户拒绝授权
                    ToastUtil.showToast(this, "You denied the permission");
                }
                break;
            default:
        }
    }

    /**
     * 打开系统相册
     */
    private void openAlbum() {
        // 使用Intent来跳转
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        // setType是设置类型，只要系统中满足该类型的应用都会被调用，这里需要的是图片
        intent.setType("image/*");
        // 打开满足条件的程序，CHOOSE_PHOTO是一个常量，用于后续进行判断，下面会说
        startActivityForResult(intent, CHOOSE_PHOTO);
    }


    /**
     * 处理返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 上面的CHOOSE_PHOTO就是在这里用于判断
            case CHOOSE_PHOTO:
                // 判断手机系统版本号
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 手机系统在4.4及以上的才能使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 手机系统在4.4以下的使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = null;
        //如果是document类型的uri
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径
            imagePath = uri.getPath();
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        upload(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        // 根据得到的图片路径显示图片
        upload(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 上传图片
     */
    private void upload(final String imagePath){

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    DownLoadUtil.uploadLogFile("http://49.234.92.110:8080/MyWebDemo/UploadHandleServlet?filename=",imagePath,ImageId, new registerInterface() {
                        @Override
                        public void Sucess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showToast(PhotoDetailActivity.this,"上传成功!");
                                    photoView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                                }
                            });
                        }

                        @Override
                        public void failed(String returnCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showToast(PhotoDetailActivity.this,"上传失败!");
                                }
                            });
                        }
                    });
                }
            };
            MyThreadPool.getInstance().submit(runnable);
        }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;//获取原始图片的高度
        int width = options.outWidth;//获取原始图片的宽度
        int inSampleSize = 1;//设置采样率
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
