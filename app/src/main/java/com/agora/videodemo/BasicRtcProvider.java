package com.agora.videodemo;

import android.content.Context;
import android.util.Log;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class BasicRtcProvider implements RtcProvider {
    private String TAG = "BasicRtcProvider";
    private final Context applicationContext;

    public BasicRtcProvider(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public RtcEngine getRtcEngine(IRtcEngineEventHandler mRtcEventHandler) {
        try {
           return RtcEngine.create(applicationContext, applicationContext.getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
