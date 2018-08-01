package com.remote.webmode.aidl;

import com.remote.binder.IBinderManager;
import com.remote.webmode.aidl.mainprocess.MainRemoteService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.concurrent.CountDownLatch;

/**
 * 用于web进程向mainprocess发起连接，获取binder
 * Created by zhangjianliang on 2018/5/22
 */
public class WebBinderClient {

    private IBinderManager mBinderManager;

    private static volatile WebBinderClient mInstance;

    private CountDownLatch mCountDownLatch;

    private ServiceConnectImpl mConnect;

    private WebBinderClient() {
    }

    public static WebBinderClient getInstance() {
        if (mInstance == null) {
            synchronized (WebBinderClient.class) {
                if (mInstance == null) {
                    mInstance = new WebBinderClient();
                }
            }
        }
        return mInstance;
    }

    /**
     * 启动服务、连接主进程服务端
     */
    public synchronized void bindMainService(Context context) {
        mCountDownLatch = new CountDownLatch(1);//共享锁
        Intent service = new Intent(context, MainRemoteService.class);
        if (mConnect == null) {
            mConnect = new ServiceConnectImpl(context);
        }
        context.bindService(service, mConnect, Context.BIND_AUTO_CREATE);
        try {
            mCountDownLatch.await();//阻塞当前线程(webview子进程的子线程)，等待连接完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出h5时解绑服务
     */
    public synchronized void unbindMainService(Context context) {
        if (mConnect != null) {
            context.unbindService(mConnect);
        }
    }

    private class ServiceConnectImpl implements ServiceConnection {

        private Context mContext;

        public ServiceConnectImpl(Context context) {
            mContext = context;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                mBinderManager = IBinderManager.Stub.asInterface(service);
                try {
                    mBinderManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {//子进程的主线程中监听binder的死亡通知
                            mBinderManager.asBinder().unlinkToDeath(this, 0);
                            mBinderManager = null;
                            //此处回调到了子进程的主线程中，如果这里重新连接服务就是在主线程连接服务，如果连接异常有小概率出现anr
                        }
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mCountDownLatch.countDown();//共享锁释放锁，子进程启动服务的线程唤醒继续往下执行
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    /**
     * 根据binderCode获取Binder
     *
     * @param binderCode 一个标识而已 {@link com.silvrr.common.module.h5.aidl.mainprocess.BinderManager}
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderManager != null) {
                binder = mBinderManager.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;//异常情况下，返回null，回调回去后会重新连接服务
    }
}