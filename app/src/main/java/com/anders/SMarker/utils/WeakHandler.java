package com.anders.SMarker.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 메모리 릭 방지 핸들러
 * @param <T>
 */
public abstract class WeakHandler<T> extends Handler {

    private final WeakReference<T> reference;

    public WeakHandler(T ref) {
        this.reference = new WeakReference<T>(ref);
    }

    @Override
    public void handleMessage(Message msg) {

        T ref = reference.get();
        if (ref != null) {
        	weakHandleMessage(ref, msg);
        }
    }

    protected abstract void weakHandleMessage(T ref, Message msg);

}
