package com.remote.webmode;

import com.remote.binder.webview.IWebBinderCallback;
import com.remote.webmode.receiver.WebRemoteReciver;
import com.remote.webmode.util.WebRemoteControl;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析给web调用的接口类，根据web调用传过来的方法名、参数等信息，调用这里解析出来的方法
 * 这种实现是为了不需要改动底层真正给web调用的接口{@link com.remote.webmode.interfaces.BaseRemoteJsInterface}
 * 只需要上层通过H5Bridge.getInstance().register(H5Interface.class)
 * Created by zhangjianliang on 2018/5/22
 */
public class H5Bridge {

    //key：方法名  value：方法对象Method
    private Map<String, Method> mMethodMp = new HashMap<>();

    private static volatile H5Bridge mInstance;

    private H5Bridge() {
    }

    public static H5Bridge getInstance() {
        if (mInstance == null) {
            synchronized (H5Bridge.class) {
                if (mInstance == null) {
                    mInstance = new H5Bridge();
                }
            }
        }
        return mInstance;
    }

    /**
     * 解析提供给h5调用的方法所在的类，把该类的所有方法解析，并以键值对的形式放到 mMethodMp 中
     * 1、该类中的方法不允许重名，否则后面的方法会覆盖前面的方法
     * 2、方法的参数为JSONObject
     *
     * @param clazz 提供给h5调用的方法所在的类
     */
    public void register(Context context, Class clazz) {
        try {
            Intent intent = new Intent(WebRemoteReciver.WEB_REMOTE_ACTION);
            context.sendBroadcast(intent);
            parseMethods(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseMethods(Class injectedCls) throws Exception {
        Method[] methods = injectedCls.getDeclaredMethods();
        Method[] methods2 = injectedCls.getSuperclass().getDeclaredMethods();
        appendMethodsToMap(methods, mMethodMp);
        appendMethodsToMap(methods2, mMethodMp);
    }

    private void appendMethodsToMap(Method[] methods, Map<String, Method> mMethodsMap) {
        if (methods == null || methods.length == 0) {
            return;
        }
        for (Method method : methods) {
            if (method == null) {
                continue;
            }
            String name = method.getName();
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            Class[] parameters = method.getParameterTypes();
            if (parameters == null || parameters.length == 0) {
                return;
            }
            if (parameters[0] == JSONObject.class) {
                mMethodsMap.put(name, method);
            }
        }
    }

    /**
     * 根据网页调用的方法名、参数，通过反射调用注册的客户端方法
     */
    public void callJava(String methodName, String param, IWebBinderCallback callback) {
        if (mMethodMp.containsKey(methodName)) {
            Method method = mMethodMp.get(methodName);
            if (method != null) {
                try {
                    JSONObject paramJson = null;
                    if (!TextUtils.isEmpty(param)) {
                        paramJson = new JSONObject(param);
                    }
                    method.invoke(null, paramJson, callback);
                } catch (Exception e) {
                    Log.d(WebRemoteControl.TAG, "执行异常，请检查传入参数是否有误！");
                }
            } else {
                Log.d(WebRemoteControl.TAG, "Android侧没有定义该方法，请检查接口参数名称是否有误！");
            }
        } else {
            Log.d(WebRemoteControl.TAG, "Android侧没有定义接口[" + methodName + "]，请检查接口参数名称是否有误！");
        }
    }
}