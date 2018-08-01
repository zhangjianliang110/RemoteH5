package com.remote.webmode.view;

import com.remote.webmode.util.NetworkUtils;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 包装这个WebView 并增强它的配置
 */
public class EnhanceWebView extends WebView {

    private static final String TAG = "InstallmentJsObj";

    public EnhanceWebView(Context context) {
        super(context);
        init();
    }

    public EnhanceWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhanceWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        init(this);
    }

    public void init(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");//字符编码UTF-8
        //支持获取手势焦点，输入用户名、密码或其他
        webView.requestFocusFromTouch();
        settings.setSupportZoom(false);//不支持缩放
        //设置自适应屏幕，两者合用
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setUseWideViewPort(true); //将图片调整到适合webView的大小
        settings.setJavaScriptEnabled(true); // 启用javascript脚本
        settings.setNeedInitialFocus(true); //当webView调用requestFocus时为webView设置节点
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过js打开新的窗口
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染等级
        settings.setSupportMultipleWindows(true);
        settings.setEnableSmoothTransition(true);
        webView.setFitsSystemWindows(true);
        //缓存数据 (localStorage)
        //有时候网页需要自己保存一些关键数据,Android WebView 需要自己设置
        if (NetworkUtils.isConnected(webView.getContext())) {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        } else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//没网，则从本地获取，即离线加载
        }
        settings.setAllowFileAccess(true);
        settings.setSaveFormData(true);
        settings.setDomStorageEnabled(true);// 设置H5可以使用localStorage
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);//启动缓存
        String appCachePath = webView.getContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        //html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
        //然后 复写 WebChromeClient的onCreateWindow方法
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // for remote debug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // 界面上放大和缩小按钮控制器
        settings.setBuiltInZoomControls(false);
        //缓存设置20M
        settings.setAppCacheMaxSize(20*1024*1024);
    }
}