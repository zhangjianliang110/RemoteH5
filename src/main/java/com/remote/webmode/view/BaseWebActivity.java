package com.remote.webmode.view;

import com.remote.webmode.R;
import com.remote.webmode.aidl.WebBinderClient;
import com.remote.webmode.interfaces.IServiceConnectCallback;
import com.remote.webmode.util.CommonUtils;
import com.remote.webmode.util.WebRemoteControl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * 用于加载H5的页面基类，实现独立进程，及与主进程通信
 * Created by zhangjianliang on 2018/5/22
 */
public abstract class BaseWebActivity extends Activity {

    protected static final String TAG = BaseWebActivity.class.getSimpleName();

    public static final String LOAD_URL_KEY = "load_url_key";
    public static final String TITLE_KEY = "title_key";

    protected ProgressBar mProgressBar;

    protected LinearLayout mLlContainer;

    protected WebView mWebView;

    protected long mOldTime;

    private String mUrl;
    private String mTitle;

    public @LayoutRes int getLayoutId() {
        return R.layout.activity_base_web;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
    }

    protected void initView() {
        mProgressBar = findViewById(R.id.progressbar);
        mLlContainer = findViewById(R.id.llBaseWebContainer);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        resolveIntent(intent);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView = new EnhanceWebView(this);
        mWebView.setLayoutParams(params);
        mLlContainer.addView(mWebView);
        mWebView.setWebChromeClient(getWebChromeClient());
        mWebView.setWebViewClient(getWebViewClient());
        syncWebViewCookie();
        String agent = mWebView.getSettings().getUserAgentString();
        String ageng1 = replaceAgent(agent);
        mWebView.getSettings().setUserAgentString(ageng1);
//        mWebView.loadUrl(getWebPresenter().getUrl());
        //兼容BUG处理代码Start：web在此会被冻结，无法滑动等...
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            disableHardwareAcc();
        }
        //兼容BUG处理代码End...
        //开始连接主进程
        final WebRemoteControl remoteControl = new WebRemoteControl(this);
        remoteControl.setServiceConnectListener(new IServiceConnectCallback() {
            @Override
            public void onServiceConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(mUrl);
                    }
                });
            }

            @Override
            public void onConnectFailed() {//连接服务失败，重新连接
                remoteControl.bindService(BaseWebActivity.this);
            }
        });
        remoteControl.setWebView(mWebView);
        //完成连接主进程
    }

    public void resolveIntent(Intent intent) {
        mUrl = intent.getStringExtra(LOAD_URL_KEY);
        mTitle = intent.getStringExtra(TITLE_KEY);
    }

    /**
     * 处理特殊useragent
     */
    public String replaceAgent(String agent) {
        agent = agent.replaceAll("; wv", "");
        agent = agent.replaceAll("Version/4.0 ", "");
        return agent;
    }

    public abstract WebChromeClient getWebChromeClient();
    public abstract WebViewClient getWebViewClient();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void disableHardwareAcc() {
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void onReload() {
        mWebView.reload();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers(); //小心这个！！！暂停整个 mWebView 所有布局、解析、JS。
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.setTag(null);
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
        WebBinderClient.getInstance().unbindMainService(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            performKeyBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public abstract void syncWebViewCookie();

    private void performKeyBack() {
        if (System.currentTimeMillis() - mOldTime < 1500) {
            mWebView.clearHistory();
            mWebView.loadUrl(mUrl);
            CommonUtils.hideKeyBoard(this);
            super.onBackPressed();
        } else if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
        mOldTime = System.currentTimeMillis();
    }
}