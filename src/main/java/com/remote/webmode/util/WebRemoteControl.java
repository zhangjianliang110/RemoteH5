package com.remote.webmode.util;

import com.remote.binder.webview.IWebBinder;
import com.remote.binder.webview.IWebBinderCallback;
import com.remote.webmode.WebConst;
import com.remote.webmode.aidl.WebBinderClient;
import com.remote.webmode.aidl.mainprocess.BinderManager;
import com.remote.webmode.interfaces.BaseRemoteJsInterface;
import com.remote.webmode.interfaces.IServiceConnectCallback;

import android.app.Activity;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.webkit.WebView;

/**
 * web模块独立进程的操作入口类
 * Created by zhangjianliang on 2018/5/22
 */
public class WebRemoteControl implements BaseRemoteJsInterface.JsFunctionCallback {

    public static final String TAG = "webremote";

    protected BaseRemoteJsInterface mJsInterface;

    private IServiceConnectCallback mCallback;

    private WebView mWebView;

    private Activity mActivity;

    protected IWebBinder mWebBinder;

    public WebRemoteControl(Activity activity) {
        mActivity = activity;
        mJsInterface = new BaseRemoteJsInterface();
        mJsInterface.setCallback(this);
    }

    /**
     * 添加web端调用接口，连接主进程服务
     */
    public void setWebView(WebView webView) {
        mWebView = webView;
        mWebView.addJavascriptInterface(mJsInterface, WebConst.JS_INTERFACE);
        bindService(mActivity);
    }

    /**
     * 启动服务，与主进程连接
     */
    public void bindService(final Activity activity) {
        ThreadPoolFactory.instance().fixExecutor(new Runnable() {
            @Override
            public void run() {
                WebBinderClient webClient = WebBinderClient.getInstance();
                webClient.bindMainService(activity);//子线程启动服务，启动会该线程会休眠，等待连接成功后才唤醒
                IBinder iBinder = webClient.queryBinder(BinderManager.BINDER_WEB_AIDL);
                if (iBinder != null) {
                    mWebBinder = IWebBinder.Stub.asInterface(iBinder);//服务端(主进程)返回的binder
                    if (mCallback != null) {
                        mCallback.onServiceConnected();
                    }
                } else {
                    if (mCallback != null) {
                        mCallback.onConnectFailed();
                    }
                }
            }
        });
    }

    @Override
    public void execute(String methodName, String params) {//h5调用native方法时回调
        if (mWebBinder == null) {
            return;
        }
        handleJsFunc(methodName, params);
    }

    /**
     * 处理h5调用native的操作
     */
    protected void handleJsFunc(String action, String params) {
        try {//handleJsFunction()是在主进程中，回调通过aidl回到了子进程中
            mWebBinder.handleJsFunction(action, params, new IWebBinderCallback.Stub() {
                @Override
                public void onResult(int msgType, String message) throws RemoteException {
                    resolveResult(msgType, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resolveResult(int msgType, String message) {
        switch (msgType) {
            case WebConst.MSG_TYPE_JS://执行js代码
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(message, null);
                } else {
                    mWebView.loadUrl(message);
                }
                break;
            case WebConst.MSG_TYPE_CLOSE_H5://关闭web进程
                if (mActivity != null && !mActivity.isFinishing()) {
                    mActivity.finish();
                }
                break;
            // TODO: 2018/5/23 回调消息处理
        }
    }

    public void setServiceConnectListener(IServiceConnectCallback callback) {
        mCallback = callback;
    }
}
