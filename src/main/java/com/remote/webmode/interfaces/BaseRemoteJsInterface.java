package com.remote.webmode.interfaces;

import android.os.Handler;
import android.webkit.JavascriptInterface;

/**
 * 真正被webview.addJavascriptInterface(xxx)添加的
 * Created by zhangjianliang on 2018/5/22
 */
public final class BaseRemoteJsInterface {

    private final Handler mHandler = new Handler();

    private JsFunctionCallback mCallback;

    @JavascriptInterface
    public void jsFunc(final String methodName, final String param) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.execute(methodName, param);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void jsFunc(final String methodName) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCallback != null) {
                        mCallback.execute(methodName, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setCallback(JsFunctionCallback callback) {
        this.mCallback = callback;
    }

    public interface JsFunctionCallback {

        void execute(String methodName, String params);
    }
}
