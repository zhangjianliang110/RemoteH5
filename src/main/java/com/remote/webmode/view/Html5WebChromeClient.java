package com.remote.webmode.view;

import android.os.Message;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


/**
 * WebChromeClient
 * Created by my daling on 2017/2/16.
 */
public class Html5WebChromeClient extends WebChromeClient {
    private static final String TAG = "Html5WebChromeClient";

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    //=========HTML5定位==========================================================
    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);//注意个函数，第二个参数就是是否同意定位权限，第三个是是否希望内核记住
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    //=========多窗口的问题==========================================================
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(view);
        resultMsg.sendToTarget();
        return true;
    }
}