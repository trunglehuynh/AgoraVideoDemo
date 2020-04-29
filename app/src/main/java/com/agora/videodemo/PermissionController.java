package com.agora.videodemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class PermissionController {

    private final List<String> permissions;
    private final Context applicationContext;
    public final int  permissionCode;

    public PermissionController(List<String> permissions, Context applicationContext, int permissionCode) {
        this.permissions = permissions;
        this.applicationContext = applicationContext;
        this.permissionCode = permissionCode;
    }

    public Boolean isAllPermissionGranted() {

        for (String permission: permissions) {
            if (!isGrantedPermission(permission)) return false;
        }
        return true;
    }
    public void askAllPermissions(Activity activity){
        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[permissions.size()]), permissionCode);
    }

    public void openAppPermission(Activity activity){

        Intent intent = new  Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) ;
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, permissionCode);
    }


    private boolean isGrantedPermission( String permission) {
        return ContextCompat.checkSelfPermission(
                applicationContext,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }
}
