package com.remote.webmode.aidl.mainprocess;

import com.remote.binder.IBinderManager;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Binder管理器
 * Created by zhangjianliang on 2018/5/22
 */
public class BinderManager extends IBinderManager.Stub {

    public static final int BINDER_WEB_AIDL = 1;//h5进程请求主进程

    private Context context;

    public BinderManager(Context context) {
        this.context = context;
    }

    @Override
    public IBinder queryBinder(int binderCode) throws RemoteException {
        IBinder binder = null;
        switch (binderCode) {
            case BINDER_WEB_AIDL: {
                binder = new WebBinderInterface(context);
                break;
            }
            default:
                break;
        }
        return binder;
    }
}