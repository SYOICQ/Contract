package com.suyong.contractmanager.utils;

import android.content.Context;
import android.os.Environment;
import android.telephony.euicc.DownloadableSubscription;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.suyong.contractmanager.Interfaces.registerInterface;

public class StorgeUtils {
    private String download = "http://49.234.92.110:8080/MyWebDemo/DownLoadServlet?filename=";
    private String storgePath = "";
    private String downloadPath="";
    public StorgeUtils(Context context){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            storgePath = Environment.getExternalStorageDirectory().toString()
                    + "/ContractManager/jpg/";
            File file = new File(storgePath);
            if(!file.exists()){
                file.mkdirs();
            }
        }else {
            storgePath = context.getFilesDir().getAbsolutePath() + "/ContractManager/jpg/";
            File file = new File(storgePath);
            if(!file.exists()){
                file.mkdirs();
            }
        }
        Log.d("StorgeUtils", storgePath);
    }

    public String getdownLocalpatch(){
        return storgePath;
    }

    public void download(registerInterface listener){
        try {
            URL url = new URL(downloadPath);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(5000);
            con.setConnectTimeout(5000);
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                InputStream is = con.getInputStream();//获取输入流
                FileOutputStream fileOutputStream = null;//文件输出流
                if (is != null) {
                    //File outFile = new File(localPath);
                    //outFile.createNewFile();
                    fileOutputStream = new FileOutputStream(downloadPath);//指定文件保存路径，代码看下一步
                    byte[] buf = new byte[1024];
                    int ch;
                    while ((ch = is.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                    }
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                listener.Sucess();
            }
            listener.failed("网络错误!");
        }catch ( Exception e){
            listener.failed(e.getMessage());
        }
    }
}
