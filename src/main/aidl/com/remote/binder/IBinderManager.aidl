// IBinderManager.aidl
package com.remote.binder;

//子进程请求主进程服务成功后，返回这个管理器Binder，在子进程可以根据binderCode拿到需要的Binder
//这种设计是为了解耦，方便不同业务下把Binder分离
interface IBinderManager {
    IBinder queryBinder(int binderCode); //根据code获取需要的Binder
}
