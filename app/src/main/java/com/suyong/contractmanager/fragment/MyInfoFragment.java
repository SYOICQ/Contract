package com.suyong.contractmanager.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.suyong.contractmanager.MyApplication;
import com.suyong.contractmanager.R;
import com.suyong.contractmanager.ThreadPool.MyThreadPool;
import com.suyong.contractmanager.UserSignature;
import com.suyong.contractmanager.utils.BitmapUtils;
import com.suyong.contractmanager.utils.DBUtils;
import com.suyong.contractmanager.utils.ProgressDialogUtil;
import com.suyong.contractmanager.utils.ToastUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static android.app.Activity.RESULT_OK;

public class MyInfoFragment extends Fragment implements View.OnClickListener{

    private static final int CHOOSE_PHOTO = 1;
    private int option;
    private ImageView user_sex_arrow;
    private ImageView user_region_arrow;
    private ImageView user_personal_signature_arrow;
    private ImageView user_icon;
    private TextView user_nickname;
    private TextView user_sex;
    private TextView user_region;
    private TextView edit_passwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.from(getContext()).inflate(R.layout.myinfofragment_layout,container,false);
        initView(rootView);
        return rootView;
    }
    
    private void initView(View rootView) {
     user_sex_arrow = rootView.findViewById(R.id.user_sex_arrow);
     user_region_arrow= rootView.findViewById(R.id.user_region_arrow);
     user_personal_signature_arrow = rootView.findViewById(R.id.user_personal_signature_arrow);
     user_icon = rootView.findViewById(R.id.user_touxiang);
     user_nickname = rootView.findViewById(R.id.user_nickname);
     user_sex = rootView.findViewById(R.id.user_sex);
     user_region = rootView.findViewById(R.id.user_region);
     edit_passwd = rootView.findViewById(R.id.edit_passwd);
     user_icon.setOnClickListener(this);
    user_sex_arrow.setOnClickListener(this);
    user_region_arrow.setOnClickListener(this);
    user_personal_signature_arrow.setOnClickListener(this);
    user_nickname.setOnClickListener(this);
    edit_passwd.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MyInfoFragment", MyApplication.getCurrentIUser().toString());
        RestoreView();
        updatePhoto();
    }

    private void RestoreView() {
       // final ProgressDialog dialog = ProgressDialogUtil.newInstatnce(getContext(),"提示","更新用户信息...");
        //dialog.show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    String sql = "select password,nickname,sex,region,signature from admin where username =?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, MyApplication.getCurrentIUser().getName());
                    set = ps.executeQuery();
                    set.first();
                     final String nickname =set.getString("nickname");
                     int sex1 = set.getInt("sex");
                     final String sex = sex1 ==0?"女":"男";
                     final String region = set.getString("region");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            user_region.setText(region.isEmpty()?"保密":region);
                            user_nickname.setText(nickname.isEmpty()?"神秘人":nickname);
                            user_sex.setText(sex);
                           // dialog.dismiss();
                        }
                    });
                }catch (Exception e){
                   // dialog.dismiss();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"更新用户信息失败！");
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.user_touxiang:
                changeIcon();
                break;
            case R.id.user_nickname:
                updateNickName();
                break;
            case R.id.user_sex_arrow:
                updateSex();
                break;
            case R.id.user_region_arrow:
                updateRegion();
                break;
            case R.id.user_personal_signature_arrow:
                startActivity(new Intent(getActivity(), UserSignature.class));
                break;
            case R.id.edit_passwd:
                editPassword();
                break;
             default:
                 break;
        }
    }

    /**
     * 更换头像
     */
    private void changeIcon() {
        CheckPermission();
    }

    /**
     * 查看权限
     */
    private void CheckPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // 调用系统相册
            openAlbum();
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
     * 修改密码
     */
    private void editPassword() {
        View view = getLayoutInflater().inflate(R.layout.edit_passwd,null);
        final EditText newPassword = view.findViewById(R.id.newPassword);
        new AlertDialog.Builder(getContext())
                .setTitle("修改密码:")
                .setView(view)
                .setIcon(R.drawable.edit_password)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    ps = con.prepareStatement("update admin set password =? where username =?");
                                    ps.setString(1,newPassword.getText().toString());
                                    ps.setString(2,MyApplication.getCurrentIUser().getName());
                                    ps.executeUpdate();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"修改密码成功！");
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"修改密码失败！");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,null,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                }).setNegativeButton("取消",null)
                .show();
    }

    /**
     * 更新地区
     */
    private void updateRegion() {
         View view = getLayoutInflater().inflate(R.layout.edit_location,null);
        final EditText newLocation = view.findViewById(R.id.newlocation);
        new AlertDialog.Builder(getContext())
                .setTitle("地区：")
                .setView(view)
                .setIcon(R.drawable.location)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    ps = con.prepareStatement("update admin set region =? where username =?");
                                    ps.setString(1,newLocation.getText().toString());
                                    ps.setString(2,MyApplication.getCurrentIUser().getName());
                                    ps.executeUpdate();
                                    RestoreView();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新成功！");
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新失败！");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,null,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                }).setNegativeButton("取消",null)
                .show();
    }

    /**
     * 更新性别
     */
    private void updateSex() {
        final View view = getLayoutInflater().inflate(R.layout.select_sex,null);
        RadioGroup sexGroup = view.findViewById(R.id.sexGroup);

        sexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                RadioButton choice = (RadioButton) view.findViewById(id);
                option = choice.getText().toString().equals("男")?1:0;
            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle("性别：")
                .setView(view)
                .setIcon(R.drawable.sex)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    ps = con.prepareStatement("update admin set sex =? where username =?");
                                    ps.setInt(1,option);
                                    ps.setString(2,MyApplication.getCurrentIUser().getName());
                                    ps.executeUpdate();
                                    RestoreView();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新成功！");
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新失败！");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,null,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                }).setNegativeButton("取消",null)
                .show();
    }

    /**
     * 更新昵称
     */
    private void updateNickName() {
        View view = getLayoutInflater().inflate(R.layout.edit_nickname,null);
        final EditText newName = view.findViewById(R.id.newname);
        new AlertDialog.Builder(getContext())
                .setTitle("请输入！")
                .setView(view)
                .setIcon(R.drawable.nickname)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Connection con = DBUtils.getConnection();
                                PreparedStatement ps = null;
                                try{
                                    ps = con.prepareStatement("update admin set nickname =? where username =?");
                                    ps.setString(1,newName.getText().toString());
                                    ps.setString(2,MyApplication.getCurrentIUser().getName());
                                    ps.executeUpdate();
                                    RestoreView();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新成功！");
                                        }
                                    });
                                }catch(Exception e){
                                    e.printStackTrace();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showToast(MyApplication.getContext(),"更新失败！");
                                        }
                                    });
                                }finally{
                                    DBUtils.close(con,null,ps);
                                }
                            }
                        };
                        MyThreadPool.getInstance().submit(runnable);
                    }
                }).setNegativeButton("取消",null)
                .show();
    }

    /**
     * 更新头像
     */
    private void updatePhoto(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Connection con = DBUtils.getConnection();
                PreparedStatement ps = null;
                ResultSet set = null;
                try{
                    String sql = "select icon from admin where username =?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, MyApplication.getCurrentIUser().getName());
                    set = ps.executeQuery();
                    set.first();
                    if(!TextUtils.isEmpty(set.getString("icon"))){
                        final Bitmap icon = BitmapUtils.convertStringToIcon(set.getString("icon"));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                user_icon.setImageBitmap(icon);
                            }
                        });
                    }

                }catch(Exception e){
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(MyApplication.getContext(),"头像更新失败！");
                        }
                    });
                }finally{
                    DBUtils.close(con,set,ps);
                }
            }
        };
        MyThreadPool.getInstance().submit(runnable);
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
                    ToastUtil.showToast(getActivity(), "You denied the permission");
                }
                break;
            default:
        }
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
        if(DocumentsContract.isDocumentUri(getActivity(),uri)){
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
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        // 根据得到的图片路径显示图片
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContext().getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 显示图片
     */
    private void displayImage(String imagePath){
        if(imagePath!=null){
            BitmapFactory.Options options = new BitmapFactory.Options();// 解析位图的附加条件
            options.inJustDecodeBounds = true;    // 不去解析位图，只获取位图头文件信息
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath,options);
            int sampleSize = calculateInSampleSize(options,user_icon.getWidth(),user_icon.getHeight()); // 设置默认缩放比例

            options.inSampleSize = sampleSize;       // 设置图片缩放比例
            options.inJustDecodeBounds = false;     // 真正解析位图
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            user_icon.setImageBitmap(bitmap); // 设置图片

            //上传到数据库
            final String str = BitmapUtils.convertIconToString(bitmap);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Connection con = DBUtils.getConnection();
                    PreparedStatement ps = null;
                    try{
                        ps = con.prepareStatement("update admin set icon =? where username =?");
                        ps.setString(1,str);
                        ps.setString(2,MyApplication.getCurrentIUser().getName());
                        ps.executeUpdate();
                        RestoreView();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(MyApplication.getContext(),"更新成功！");
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(MyApplication.getContext(),"更新失败！");
                            }
                        });
                    }finally{
                        DBUtils.close(con,null,ps);
                    }
                }
            };
            MyThreadPool.getInstance().submit(runnable);
        }else{
            ToastUtil.showToast(getActivity(),"failed to get image");
        }
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
