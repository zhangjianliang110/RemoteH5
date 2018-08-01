package com.remote.webmode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

/**
 * 用于接收主进程的消息
 * Created by zhangjianliang on 2018/5/22
 */
public class WebRemoteReciver extends BroadcastReceiver {

    public static final String WEB_REMOTE_ACTION = "com.remote.webmode.web_remote_action";
    public static final String REMOTE_MESSAGE_KEY = "remote_message_key";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (!WEB_REMOTE_ACTION.equals(action)) {
            return;
        }
        RemoteMessage msg = intent.getParcelableExtra(REMOTE_MESSAGE_KEY);
        if (msg == null) {
            return;
        }
        switch (msg.mMsgType) {
            case RemoteMessage.MSG_TYPE_KILL://让子进程自杀
                Process.killProcess(Process.myPid());
                break;
        }
    }
}
