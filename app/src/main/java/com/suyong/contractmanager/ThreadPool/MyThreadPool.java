package com.suyong.contractmanager.ThreadPool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    //创建基本线程池
    static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,10,1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(50));
    public static  ThreadPoolExecutor getInstance(){
        return threadPoolExecutor;
    }
}
