package com.yc.ycrertofitutils.interfaces;

public interface OnCallBackListener {

    <Q> void onSuccess(Q body, String tag);

    void onFailed(String e, String tag);
}
