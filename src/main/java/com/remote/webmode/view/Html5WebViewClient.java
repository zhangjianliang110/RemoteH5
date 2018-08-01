package com.remote.webmode.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * WebViewClient
 * Created by zhangjianliang on 2018/5/22
 */
public class Html5WebViewClient extends WebViewClient {

    private IUrlloadCallback mLoadUrlError;

    public Html5WebViewClient(IUrlloadCallback loadUrlError) {
        mLoadUrlError = loadUrlError;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        //延缓加载图片资源，防止webview的onPageFinish()方法执行时耗过长，出现点击界面没反应的情况
        view.getSettings().setBlockNetworkImage(true);
        if (url.startsWith("tel:")) {//调用系统拔号
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            view.getContext().startActivity(intent);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //当webview页面加载完毕后，开始加载图片资源
        view.getSettings().setBlockNetworkImage(false);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.startsWith("http:") || url.startsWith("https:")) {
            view.loadUrl(url);
        }
        try {//处理intent协议
            if (url.startsWith("intent://")) {
                Intent intent;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setComponent(null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        intent.setSelector(null);
                    }
                    PackageManager pm = view.getContext().getPackageManager();
                    List<ResolveInfo> resolves = null;
                    if (pm != null) {
                        resolves = pm.queryIntentActivities(intent, 0);
                    }
                    if (resolves != null && !resolves.isEmpty()) {
                        ((Activity) view.getContext()).startActivityIfNeeded(intent, -1);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            // 处理自定义scheme协议
            if (!url.startsWith("http")) {
                try {// 以下固定写法
                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();// 防止没有安装的情况
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.shouldOverrideUrlLoading(view, url);
    }


    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (error.getPrimaryError() == SslError.SSL_DATE_INVALID
                || error.getPrimaryError() == SslError.SSL_EXPIRED
                || error.getPrimaryError() == SslError.SSL_INVALID
                || error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
            handler.proceed();//接受所有网站的证书
        } else {
            handler.cancel();
        }
        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (mLoadUrlError != null) {
            mLoadUrlError.onLoadError(view, errorCode, description, failingUrl);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        // 拦截替换网络请求数据,  从API 21开始引入
        WebResourceResponse response;
        Context app = view.getContext();
        response = super.shouldInterceptRequest(view, request);
        if (request.getUrl().toString().contains("jquery-2.2.1.min.js")) {
            try {
                response = new WebResourceResponse("application/x-javascript", "UTF-8",
                        app.getAssets().open("js/jquery-2.2.1.min.js"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (request.getUrl().toString().contains("opensans.ttf")) {
            try {
                response = new WebResourceResponse("application/octet-stream", "UTF-8",
                        app.getAssets().open("font/opensans.ttf"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (request.getUrl().toString().contains("swiper.jquery.js")) {
            try {
                response = new WebResourceResponse("application/x-javascript", "UTF-8",
                        app.getAssets().open("js/swiper.jquery.js"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response;
        Context app = view.getContext();
        response = super.shouldInterceptRequest(view, url);
        if (url.contains("jquery-2.2.1.min.js")) {
            try {
                response = new WebResourceResponse("application/x-javascript", "UTF-8",
                        app.getAssets().open("js/jquery-2.2.1.min.js"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (url.contains("opensans.ttf")) {
            try {
                response = new WebResourceResponse("application/octet-stream", "UTF-8",
                        app.getAssets().open("font/opensans.ttf"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (url.contains("swiper.jquery.js")) {
            try {
                response = new WebResourceResponse("application/x-javascript", "UTF-8",
                        app.getAssets().open("js/swiper.jquery.js"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public interface IUrlloadCallback {
        void onLoadError(WebView view, int errorCode, String description, String failingUrl);
    }
}