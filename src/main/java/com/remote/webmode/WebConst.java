package com.remote.webmode;

/**
 * Created by zhangjianliang on 2018/5/23
 */

public interface WebConst {

    String JS_INTERFACE = "webview";//h5调用natvie方法的引用名
    //回调给web子进程的消息类型
    int MSG_TYPE_JS = 0;//0，默认就是执行js代码，不执行js，回调给子进程做什么
    int MSG_TYPE_CLOSE_H5 = 1;//1，关闭h5页面
}
