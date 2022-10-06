package com.messeesang.kanajimo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.kit.Kit;

import static com.messeesang.kanajimo.MyApplication.IR_CD_Value;
import static com.messeesang.kanajimo.MyApplication.SIDE_MENU_OPEN_CHECK;
import static com.messeesang.kanajimo.kit.TelKit.PATH_HOME;
import static com.messeesang.kanajimo.kit.TelKit.PATH_MY_PAGE;
import static com.messeesang.kanajimo.kit.TelKit.PATH_SEARCH;
import static com.messeesang.kanajimo.kit.TelKit.URL_BASE_PRD;
import static com.messeesang.kanajimo.ui.MainActivity.backPressCloseHandler;

public class MainWebViewFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    public static WebViewEx mWebViewEx = null;
    public CookieManager cookieManager;
    private ProgressBar progress_bar = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private String sPage_URL = "";
    private int Key_Event_Count = 0;

    public static MainWebViewFragment getInstance() {
        MainWebViewFragment fragment = new MainWebViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.fragment_main_webview, null);

        if (getArguments() != null) {
            sPage_URL = getArguments().getString("URL");
        }

        cookieManager = CookieManager.getInstance();
        mWebViewEx = v.findViewById(R.id.WebViewEx_main);
        progress_bar = v.findViewById(R.id.progress_bar);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout);

        mWebViewEx.setProgressBar(progress_bar);
        mWebViewEx.setSwipeRefreshLayout(swipeRefreshLayout);
        Log.e(TAG, "sPage_URL : " + sPage_URL);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            mWebViewEx.setContainer(((MainActivity) activity).mLayoutRoot);
        }
        mWebViewEx.setFragment(this);

        mWebViewEx.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                //This is the filter
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Key_Event_Count++;
                } else {
                    Key_Event_Count = 0;
                }

                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN || Key_Event_Count > 1) {
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.e(TAG, "mWebViewEx KEYCODE_BACK getUrl : " + mWebViewEx.getUrl());
                    if (SIDE_MENU_OPEN_CHECK) {
                        mWebViewEx.loadUrl("javascript:closeSideMenu();");  //사이드메뉴 닫기
                        return true;
                    }
                    if (mWebViewEx.canGoBack()) {
                        if (mWebViewEx.getUrl().equals(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value)
                                || mWebViewEx.getUrl().equals(URL_BASE_PRD + PATH_HOME)) {
                            backPressCloseHandler.onBackPressed();
                        } else if (mWebViewEx.getUrl().equals(URL_BASE_PRD + PATH_SEARCH)
                                || mWebViewEx.getUrl().equals(URL_BASE_PRD + PATH_MY_PAGE)
                                || mWebViewEx.getUrl().equals(URL_BASE_PRD +"/auth/login?ret_url=%2Fmypage")) {
                            loadUrl(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                        } else {
                            mWebViewEx.goBack();
                        }
                    } else {
                        if (mWebViewEx.getUrl() == null || !mWebViewEx.getUrl().equals(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value)) {
                            loadUrl(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                        } else {
                            backPressCloseHandler.onBackPressed();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        loadUrl(sPage_URL);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Activity activity = getActivity();

        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.checkAndOpenLink(mainActivity.getIntent());
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                String token_id = "";
                if (task.isSuccessful()) {
                    // Get new FCM registration token
                    token_id = task.getResult();
                    Kit.log(Kit.LogType.VALUE, "onSuccess::token_id = " + token_id);

                } else {
                    Log.w("MainWebViewFragment", "Fetching FCM registration token failed", task.getException());
                }

                if (mWebViewEx != null) {
                    mWebViewEx.setFCMToken(token_id);
                }
            }
        });

        //# Appsflyer Start
        AppsFlyerLib.getInstance().sendPushNotificationData(activity);
        //# Appsflyer End
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        // TODO Auto-generated method stub
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mWebViewEx != null) {
            mWebViewEx.onPause();
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mWebViewEx != null) {
            mWebViewEx.onResume();

            if(mWebViewEx.getUrl() == null || mWebViewEx.getUrl().equals("")) {
                loadUrl(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
            }
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void loadUrl(String url) {
        if (mWebViewEx == null) {
            return;
        }
        mWebViewEx.post(new Runnable() {
            @Override
            public void run() {
                mWebViewEx.loadUrl(url);
                //Log.e(TAG, "getUserAgentString();" + mWebViewEx.getSettings().getUserAgentString());
            }
        });
    }
}