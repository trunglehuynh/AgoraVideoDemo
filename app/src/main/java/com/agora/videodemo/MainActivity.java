package com.agora.videodemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.rtc.IRtcEngineEventHandler;

/**
 *
 * this demo bases on Quickstart Tutorial: Android 1-to-1 Video Call App, UI buttons got from the tutorial.
 * https://www.youtube.com/watch?v=gIuThUluO0U&t=564s
 *
 *  futureworks:
 *
 *  support multiple users at same time
 *  support both portrait and landscape mode
 *  support multiple screen sizes
 *  use fragments to reuse components on phone/tablet, improve navigation if need
 *  improve UI
 *  use dependency injection such as: dagger2
 *  use Rxjava2 if need ( I need more time to learn more Agora SDK)
 *  use firebase/ Crashlytic to log bugs, crashes, and performance
 *
 *  write unit tests and UI tests
 *  more manual tests
 *  consider use MVVM or MVP base on requirements
 *
 * */


public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";

    private static final List<String> PERMISSIONS = Arrays.asList(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private PermissionController permissionController;
    private MainActivityController controller;

    @BindView(R.id.remoteVideoContainer)
    FrameLayout remoteVideoContainer;

    @BindView(R.id.localVideoContainer)
    FrameLayout localVideoContainer;

    @BindView(R.id.btn_mute)
    ImageButton muteBtn;

    @BindView(R.id.btn_call)
    ImageButton callBtn;

    @BindView(R.id.btn_switch_camera)
    ImageButton switchCameraBtn;

    @BindView(R.id.permissionBtn)
    Button permissionBtn;

    private boolean isCalling = false;
    private boolean isMute = false;

    private IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.i(TAG, "onJoinChannelSuccess: "+uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, "onUserOffline: "+uid+" "+reason);

            runOnUiThread(() ->{
                remoteVideoContainer.removeAllViews();
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(() -> {
                // only support 1 - 1 video
                if(remoteVideoContainer.getChildCount() != 0)return;
                controller.addRemoteVideo(remoteVideoContainer,getBaseContext(),uid);
            });
         }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            Log.i(TAG, "onLeaveChannel: "+stats);
            runOnUiThread(() ->{
                localVideoContainer.removeAllViews();
                remoteVideoContainer.removeAllViews();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initButtons();

        permissionController = new PermissionController(PERMISSIONS,getApplicationContext(),1234);

        BasicRtcProvider basicRtcProvider = new BasicRtcProvider(getApplicationContext());

        controller = new MainActivityController(basicRtcProvider,iRtcEngineEventHandler);

         permissionController.askAllPermissions(this);
         checkPermission(permissionController.permissionCode);
        updateCallingState(isCalling);
    }

    private void initButtons(){
        permissionBtn.setOnClickListener( v->{
           permissionController.openAppPermission(this);
        });

        callBtn.setOnClickListener( v ->{
            isCalling = !isCalling;
            if (isCalling){
                controller.startCall(localVideoContainer,getBaseContext());
                // restart mute for every call
                isMute =true;
                toggleMute();
            }
            else
                controller.leaveChannel();

            updateCallingState(isCalling);
        });
        switchCameraBtn.setOnClickListener( v ->{
            controller.switchCamera();
        });
        muteBtn.setOnClickListener( v ->{
            toggleMute();
        });
    }
    /*toggle mute button*/
    private void toggleMute(){
        isMute = !isMute;
        controller.muteLocalAudioStream(isMute);
        int res = isMute ? R.drawable.btn_mute : R.drawable.btn_unmute;
        muteBtn.setImageResource(res);
    }
    /* display button base on permission granted or not*/
    private void checkPermissions(){
        boolean isPermissionGranted = permissionController.isAllPermissionGranted();
        permissionBtn.setVisibility(isPermissionGranted? View.GONE:View.VISIBLE);
        callBtn.setVisibility(isPermissionGranted? View.VISIBLE:View.GONE);

    }
    /* show/hide buttons base on calling stat*/
    private void updateCallingState(boolean isCalling){
        callBtn.setImageResource(isCalling? R.drawable.btn_endcall:R.drawable.btn_startcall);
        muteBtn.setVisibility(isCalling? View.VISIBLE:View.GONE);
        switchCameraBtn.setVisibility(isCalling? View.VISIBLE:View.GONE);
    }

    private void initRtcEngine(){
        if (!permissionController.isAllPermissionGranted()){
            Toast.makeText(this,"App needs permissions to run",Toast.LENGTH_LONG).show();
            return;
        }

        try {
            controller.initEngine();
        }catch (RuntimeException e){
            Log.e(TAG, "initRtcEngine: "+e.getMessage());
            Toast.makeText(this,"App cannot init Video engine",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission(requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkPermission(requestCode);
    }

    private void checkPermission(int requestCode){
        if (requestCode == permissionController.permissionCode){
            checkPermissions();
            initRtcEngine();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.onDestroy();
    }
}
