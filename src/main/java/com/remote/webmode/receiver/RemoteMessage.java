package com.remote.webmode.receiver;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * web子进程接收主进程广播消息实体
 * Created by zhangjianliang on 2018/5/22
 */

public class RemoteMessage implements Parcelable{
    public static final int MSG_TYPE_KILL = 1;//让子进程自杀

    public int mMsgType;

    public RemoteMessage(){}

    protected RemoteMessage(Parcel in) {
        mMsgType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMsgType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RemoteMessage> CREATOR = new Creator<RemoteMessage>() {
        @Override
        public RemoteMessage createFromParcel(Parcel in) {
            return new RemoteMessage(in);
        }

        @Override
        public RemoteMessage[] newArray(int size) {
            return new RemoteMessage[size];
        }
    };
}
