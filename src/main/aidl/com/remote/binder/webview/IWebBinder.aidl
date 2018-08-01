// IWebAidlInterface.aidl
package com.remote.binder.webview;

// Declare any non-default types here with import statements

import com.remote.binder.webview.IWebBinderCallback;

interface IWebBinder {
     /**
      * methodName: 方法名   jsonParams: 方法参数    callback跨进程回调函数
      */
      void handleJsFunction(String actionName, String jsonParams, IWebBinderCallback callback);
}
