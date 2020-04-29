package com.agora.videodemo;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;


public class MainActivityController {

    private String TAG = "MainActivityController";

    private RtcEngine rtcEngine;
    private final RtcProvider rtcProvider;
    private final IRtcEngineEventHandler iRtcEngineEventHandler;

    /* injecting  RtcProvider and IRtcEngineEventHandler to improve testability
    *   and share the RtcEngine between components
    *  */
    public MainActivityController(RtcProvider rtcProvider, IRtcEngineEventHandler iRtcEngineEventHandler) {

        this.rtcProvider = rtcProvider;
        this.iRtcEngineEventHandler = iRtcEngineEventHandler;
    }


    void initEngine() throws RuntimeException {

        rtcEngine = rtcProvider.getRtcEngine(iRtcEngineEventHandler);

        if (rtcEngine == null) throw new RuntimeException("App cannot init rtcEngine");

        rtcEngine.enableVideo();

        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_1280x720,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    void startCall(FrameLayout container, Context context) {
        addLocalVideo(container, context);
        joinChannel();
    }

    private void addLocalVideo(FrameLayout container, Context context) {

        SurfaceView localView = RtcEngine.CreateRendererView(context);
        localView.setZOrderMediaOverlay(true);
        container.addView(localView);
        rtcEngine.setupLocalVideo(new VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    void addRemoteVideo(FrameLayout container, Context context, int uid) {

        SurfaceView removeView = RtcEngine.CreateRendererView(context);
        container.addView(removeView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(removeView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void joinChannel() {
        rtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 0);
    }

    void leaveChannel() {
        if (rtcEngine != null)
            rtcEngine.leaveChannel();
    }

    void switchCamera(){
        rtcEngine.switchCamera();
    }
    void muteLocalAudioStream(boolean isMute){
        rtcEngine.muteLocalAudioStream(isMute);
    }

    /*release engine*/
    void onDestroy() {
        leaveChannel();
        RtcEngine.destroy();
    }

}
