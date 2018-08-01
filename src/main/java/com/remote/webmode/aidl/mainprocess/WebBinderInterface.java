package com.remote.webmode.aidl.mainprocess;

import com.remote.binder.webview.IWebBinder;
import com.remote.binder.webview.IWebBinderCallback;
import com.remote.webmode.H5Bridge;

import android.content.Context;
import android.os.RemoteException;

/**
 * 主进程中，封装了给h5进程调用的接口
 * Created by zhangjianliang on 2018/5/22
 */
public class WebBinderInterface extends IWebBinder.Stub {

    private Context context;

    public WebBinderInterface(Context context) {
        this.context = context;
    }

    @Override   //处理web进程中h5页面穿过来的事件
    public void handleJsFunction(String methodName, String params, IWebBinderCallback callback) throws RemoteException {
        try {
            H5Bridge.getInstance().callJava(methodName, params, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
