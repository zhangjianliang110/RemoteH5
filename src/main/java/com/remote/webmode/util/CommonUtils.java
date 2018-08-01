package com.remote.webmode.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class CommonUtils {

    /**
     * 隐藏输入法键盘
     */
    public static void hideKeyBoard(Activity activity) {
        resolveHideKeyBoard(activity, false);
    }

    private static void resolveHideKeyBoard(Activity activity, boolean clearFocus) {
        if (null == activity) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null == imm || !imm.isActive()) {
            return;
        }
        View currentFocus = activity.getCurrentFocus();
        if (null != currentFocus) {
            imm.hideSoftInputFromWindow(currentFocus.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            if (clearFocus) {
                currentFocus.clearFocus();
            }
        }
    }

    /**
     * 隐藏输入法键盘
     */
    public static void hideKeyBoard(Activity activity, boolean clearFocus) {
        resolveHideKeyBoard(activity, clearFocus);
    }
}