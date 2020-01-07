package com.suyong.contractmanager.utils;

import com.suyong.contractmanager.Interfaces.registerInterface;
import com.suyong.contractmanager.pojo.Contract;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownLoadUtil {
    public static void uploadLogFile(String path, String oldFilePath, String id, registerInterface inter){
        try {
            String newPath = path+id+".jpg";
            URL url = new URL(newPath);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            // 允许Input、Output，不使用Cache
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            con.setConnectTimeout(50000);
            con.setReadTimeout(50000);
            // 设置传送的method=POST
            con.setRequestMethod("POST");
            //在一次TCP连接中可以持续发送多份数据而不会断开连接
            con.setRequestProperty("Connection", "Keep-Alive");
            //设置编码
            con.setRequestProperty("Charset", "UTF-8");
            //text/plain能上传纯文本文件的编码格式
            con.setRequestProperty("Content-Type", "image/jpeg");

            // 设置DataOutputStream
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());

            // 取得文件的FileInputStream
            FileInputStream fStream = new FileInputStream(oldFilePath);
            // 设置每次写入1024bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int length = -1;
            // 从文件读取数据至缓冲区
            while ((length = fStream.read(buffer)) != -1) {
                // 将资料写入DataOutputStream中
                ds.write(buffer, 0, length);
            }
            ds.flush();
            fStream.close();
            ds.close();
            if(con.getResponseCode() == 200){
                //logger.info("文件上传成功！上传文件为：" + oldFilePath);
                System.out.print("上传成功");
                inter.Sucess();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //logger.info("文件上传失败！上传文件为：" + oldFilePath);
            //logger.error("报错信息toString：" + e.toString());
            System.out.print("上传失败");
            inter.failed("0");
        }
    }
    /**
     *
     * @param path   url下载地址
     * @param localPath  下载到本地的路径
     */
    public static void downloadFile(String path,String localPath,registerInterface listener){
        try {
            URL url = new URL(path);
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
                    fileOutputStream = new FileOutputStream(localPath);//指定文件保存路径，代码看下一步
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
            listener.failed("下载失败");
        }catch (Exception e){
            listener.failed(e.getMessage());
        }
    }

    /**
     * 获取MIME类型
     * @param filename
     * @return
     */
    public static String getContentType(String filename){
        String type = null;

        return type;
    }

}

