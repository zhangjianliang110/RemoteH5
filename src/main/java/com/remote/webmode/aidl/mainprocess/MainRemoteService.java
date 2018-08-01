package com.remote.webmode.aidl.mainprocess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 主进程的服务端，子进程连接该服务，返回Binder，用于在子进程调用主进程的api
 * Created by zhangjianliang on 2018/5/22
 */
public class MainRemoteService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new BinderManager(this);
    }
}
