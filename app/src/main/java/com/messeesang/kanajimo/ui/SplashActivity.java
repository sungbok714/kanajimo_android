package com.messeesang.kanajimo.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.Kit;
import com.messeesang.kanajimo.kit.PrefKit;
import com.messeesang.kanajimo.kit.TelKit;

import static com.messeesang.kanajimo.MyApplication.IR_CD_Value;
import static com.messeesang.kanajimo.MyApplication.URL_Value;
import static com.messeesang.kanajimo.MyApplication.getAppContext;
import static com.messeesang.kanajimo.kit.Kit.isNetworkConnected;
import static com.messeesang.kanajimo.kit.TelKit.PATH_HOME;
import static com.messeesang.kanajimo.kit.TelKit.URL_BASE_PRD;

import org.json.JSONObject;

public class SplashActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private static int SPLASH_TIME_OUT = 800;
    private Handler VersionCheckHandler;
    private AlertDialog dialog = null;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.purple_500);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VersionCheckHandler = new Handler(Looper.getMainLooper());

        new Kit.GoogleADIDTask(getAppContext()).executeSync();

        if (isNetworkConnected(SplashActivity.this)) {
            if (PrefKit.getFirstPermission(SplashActivity.this)) {
                showPermissionDialog(R.layout.permission_dialog_layout);
            } else {
                showLink();
                AppVersionChecker();
            }
        } else {
            if (!SplashActivity.this.isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("???????????? ??????????????? ????????? ?????????.");
                builder.setCancelable(false);
                builder.setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                builder.show();
            }
        }

        //# Appsflyer Start
        AppsFlyerLib.getInstance().sendPushNotificationData(this);
        //# Appsflyer End
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (VersionCheckHandler != null) {
            VersionCheckHandler.removeCallbacksAndMessages(null);
        }
    }

    private void goMain() {
        if (isNetworkConnected(SplashActivity.this)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    String link = getIntent().getStringExtra(Extra.KEY_LINK);
                    Kit.log(Kit.LogType.VALUE, "SplashActivity::next::link = " + link);
                    if (Kit.isNotNullNotEmpty(link)) {
                        Kit.log(Kit.LogType.VALUE, "SplashActivity::111111111");
                        // ???????????? ???????????? ????????? ??????...
                        intent.putExtra(Extra.KEY_LINK, link);
                    } else {
                        Kit.log(Kit.LogType.VALUE, "SplashActivity::2222222222");
                        // ??????????????? ????????? ????????? ??????...
                        Uri uri = getIntent().getData();
                        if (uri != null) {
                            link = uri.getQueryParameter("link");
                            String ir_cd = uri.getQueryParameter("ir_cd");
                            String url = uri.getQueryParameter("url");
                            if (Kit.isNotNullNotEmpty(link)) {   // ?????? ???????????? ?????? ???????????? ?????????...
                                intent.putExtra(Extra.KEY_LINK, link);
                            } else if (uri.getScheme().equals("appsflyer")) {
                                if (Kit.isNotNullNotEmpty(ir_cd)) {
                                    IR_CD_Value = ir_cd;
                                }
                                if (Kit.isNotNullNotEmpty(URL_Value)) {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                                } else {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                                }
                            } else if (Kit.isNotNullNotEmpty(ir_cd)) {   // ?????????????????? ??????????????? ?????? ?????? ?????????...????????? ??????????????? ??????
                                Log.e("SplashActivity", "isNotNullNotEmpty ir_cd : " + ir_cd);
                                IR_CD_Value = ir_cd;
                                if (Kit.isNotNullNotEmpty(url)) {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + url + "/?ir_cd=" + IR_CD_Value);
                                } else {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                                }
                            }
                        } else if (Kit.isNotNullNotEmpty(IR_CD_Value)) {  //uri??? Null ?????? Firebase DynamicLink ??? ???????????? ?????? Start
                            if (Kit.isNotNullNotEmpty(URL_Value)) {
                                intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                            } else {
                                intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                            }
                        } else if (Kit.isNotNullNotEmpty(URL_Value)) {
                            intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                        }   //uri??? Null ?????? Firebase DynamicLink ??? ???????????? ?????? End
                    }
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, SPLASH_TIME_OUT);
        } else {
            if (!SplashActivity.this.isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("???????????? ??????????????? ????????? ?????????.");
                builder.setCancelable(false);
                builder.setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                builder.show();
            }
        }
    }

    public void AppVersionChecker() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // ?????? ??????
                AppUpdateManager mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
                // ???????????? ?????? ?????? ???????????? ??????
                Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
                // ???????????? ?????? ???????????? ????????????
                appUpdateInfoTask.addOnSuccessListener(AppUpdateInfo -> {

                    if (AppUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && // ????????? ???????????? ?????? ??? (AppUpdateType.FLEXIBLE) ??????
                            AppUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        Log.e("SplashActivity", "???????????? ?????? : " + AppUpdateInfo.availableVersionCode());
                        showAppUpdateDialog();
                    } else {
                        Log.e("SplashActivity", "???????????? ????????? : " + AppUpdateInfo.availableVersionCode());
                        // ??????????????? ?????? ???????????? ?????? ??????(???????????? ??????) -> ?????? ??????????????? ???????????????
                        new TelKit(SplashActivity.this, SplashActivity.this).request(TelKit.PATH_REQUEST_VERSION_INFO, "");
                    }
                });
                appUpdateInfoTask.addOnFailureListener(new com.google.android.play.core.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        new TelKit(SplashActivity.this, SplashActivity.this).request(TelKit.PATH_REQUEST_VERSION_INFO, "");
                    }
                });
            }
        }).start();
    }

    private void showAppUpdateDialog() {
        if (!SplashActivity.this.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle("????????????");
            builder.setMessage("?????? ????????? ?????? ?????????????????????\n???????????? ???????????????????");

            String positiveText = "????????????";
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                            marketLaunch.setData(Uri.parse("market://details?id=" + getPackageName()));
                            startActivity(marketLaunch);
                            dialog.dismiss();
                            finishAffinity();
                        }
                    });

            String negativeText = "??????";
            builder.setNegativeButton(negativeText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finishAffinity();
                        }
                    });
            dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (VersionCheckHandler != null) {
            VersionCheckHandler.removeCallbacksAndMessages(null);
        }

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onDestroy();
    }

    private void showLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if (pendingDynamicLinkData == null) {
                            Log.e(TAG, "DynamicLinkData : null");
                            return;
                        } else {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink != null) {
                                Log.e("SplashActivity", "deepLink : " + deepLink);
                                if (Kit.isNotNullNotEmpty(deepLink.getQueryParameter("ir_cd"))) {
                                    IR_CD_Value = deepLink.getQueryParameter("ir_cd");
                                    Log.e("SplashActivity", "IR_CD_Value : " + IR_CD_Value);
                                } else {
                                    IR_CD_Value = "";
                                }

                                if (Kit.isNotNullNotEmpty(deepLink.getQueryParameter("url"))) {
                                    URL_Value = deepLink.getQueryParameter("url");
                                    Log.d("SplashActivity", "URL_Value : " + URL_Value);
                                } else {
                                    URL_Value = "";
                                }
                            }
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "DynamicLink Fail");
                    }
                });
    }

    private void showPermissionDialog(int layout) {
        dialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        Button btnCancel = layoutView.findViewById(R.id.ok_btn);

        dialogBuilder.setView(layoutView);
        dialogBuilder.setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefKit.setFirstPermission(SplashActivity.this, false);
                showLink();
                AppVersionChecker();
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e(TAG, "onBackPressed");
        moveTaskToBack(true);
        finishAffinity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mRequestUrl.equals(TelKit.PATH_REQUEST_VERSION_INFO)) {
            if (!result.mResponse.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String code = json.optString("code");
                    if (code.equals("success")) {
                        String app_ver_name = json.optString("app_ver_name");
                        String app_ver_code = json.optString("app_ver_code");
                        Log.e(TAG, "app_ver_name : " + app_ver_name);
                        try {
                            String device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            if (app_ver_name != null && app_ver_name.compareTo(device_version) > 0) {
                                Log.d(TAG, "PATH_REQUEST_VERSION_INFO ???????????? ??????.");
                                showAppUpdateDialog();
                            } else {
                                Log.d(TAG, "PATH_REQUEST_VERSION_INFO ???????????? ?????????.");
                                goMain();

                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            goMain();
                        }
                    } else {
                        goMain();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    goMain();
                }
            } else {
                goMain();
            }
        }
    }
}
