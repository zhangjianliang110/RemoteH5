// IWebAidlCallback.aidl
package com.remote.binder.webview;

// aidl调用后异步回调的接口

interface IWebBinderCallback {
    //msgType：回调给web进程的消息类型   message：回调给web进程的消息内容
    void onResult(int msgType, String message);
}
