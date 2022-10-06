package com.messeesang.kanajimo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.NestedScrollingChild;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerProperties;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.Kit;
import com.messeesang.kanajimo.kit.PrefKit;
import com.messeesang.kanajimo.kit.RealPathKit;
import com.messeesang.kanajimo.kit.ShareKit;
import com.messeesang.kanajimo.payment.PayActivity;
import com.messeesang.kanajimo.payment.ZuPayActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;
import static com.messeesang.kanajimo.MyApplication.IR_CD_Value;
import static com.messeesang.kanajimo.MyApplication.SIDE_MENU_OPEN_CHECK;
import static com.messeesang.kanajimo.MyApplication.URL_Value;
import static com.messeesang.kanajimo.MyApplication.getAppContext;

import static com.messeesang.kanajimo.kit.TelKit.PATH_HOME;
import static com.messeesang.kanajimo.kit.TelKit.PATH_MEMBER_FINDID;
import static com.messeesang.kanajimo.kit.TelKit.PATH_MEMBER_FINDPW;
import static com.messeesang.kanajimo.kit.TelKit.PATH_MEMBER_JOIN;
import static com.messeesang.kanajimo.kit.TelKit.URL_BASE_PRD;

import static com.messeesang.kanajimo.ui.MainWebViewFragment.mWebViewEx;

public class WebViewEx extends WebView implements NestedScrollingChild {
    private String TAG = getClass().getSimpleName();
    private Context mContext;
    private MyWebChromeClient mWebChromeClient;
    private MyWebViewClient mWebViewClient;
    private ProgressBar mProgressBar = null;
    private ViewGroup mLayoutProgress = null;
    private WebViewInterface mWebViewInterface = new WebViewInterface();
    private ViewGroup mContainer = null;
    private ImageButton mBtnBack = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private PopupWindow mNetworkCautionPopup = null;

    private String mFCMToken = "";
    private String mCameraPhotoPath = "";
    private ValueCallback<Uri[]> mFilePathCallback;

    public static final String SCHEME_KANACAT = "kanacat";

    private Fragment mFragment = null;

    private boolean mIsFirstLoad = true;

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        mContext = context;

        WebSettings s = getSettings();
        mWebChromeClient = new MyWebChromeClient();
        mWebViewClient = new MyWebViewClient();
        try {
            setWebChromeClient(mWebChromeClient);
            setWebViewClient(mWebViewClient);
            s.setJavaScriptEnabled(true);                       // javascript 허용
            s.setCacheMode(WebSettings.LOAD_NO_CACHE);
            s.setJavaScriptCanOpenWindowsAutomatically(true);   // window.open 허용

            String userAgent = s.getUserAgentString();
            s.setUserAgentString(userAgent + "/AppVer" + Kit.getPackageVersionName(mContext) + "/kanacat");     // 앱 버전 정보 추가

            s.setBuiltInZoomControls(true);
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
            s.setSupportZoom(true);
            s.setDomStorageEnabled(true);
            s.setTextZoom(110);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(this, true);
            }

            requestFocus(View.FOCUS_DOWN);    // 일부기기에서 소프트키보드 않뜨는 문제때문에...

            addJavascriptInterface(mWebViewInterface, SCHEME_KANACAT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebViewEx(Context context) {
        super(context);

        init(context);
    }

    public WebViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public WebViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void setProgressLayout(ViewGroup layoutProgress) {
        mLayoutProgress = layoutProgress;
    }

    public void setContainer(ViewGroup container) {
        mContainer = container;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void setBackButton(ImageButton btnBack) {
        mBtnBack = btnBack;
        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canGoBack()) {
                    goBack();
                }
            }
        });
    }

    public void setFCMToken(String token) {
        mFCMToken = token;
    }

    public String getFCMToken() {
        return mFCMToken;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        mSwipeRefreshLayout = swipeRefreshLayout;
        final float defaultDistance = 64 * getResources().getDisplayMetrics().density;
        mSwipeRefreshLayout.setDistanceToTriggerSync((int) (defaultDistance * 2.0));     // 당기는 거리 2배로...
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mWebViewEx.getUrl() != null && mWebViewEx.getUrl().contains(PATH_MEMBER_JOIN)) {
                    if (mSwipeRefreshLayout != null) {
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    return;
                }
                reload();
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {

        protected boolean processUrlLoading(WebView webView, Uri uri) {
            String url = uri.toString();
            String fragmentName = "";
            if (webView instanceof WebViewEx) {
                Fragment fragment = ((WebViewEx) webView).mFragment;
                if (fragment != null) {
                    fragmentName = fragment.getClass().getSimpleName();
                }
            }
            //Kit.log(Kit.LogType.VALUE, String.format("[%s]processUrlLoading::url = %s", fragmentName, url));
            String scheme = uri.getScheme();
            String host = "";
            String path = "";
            //String query = "";
            if (uri.getHost() != null) {
                host = uri.getHost().toLowerCase();
            }
            if (uri.getPath() != null) {
                path = uri.getPath().toLowerCase();

            }
            //query = uri.getQuery();
            //Kit.log(Kit.LogType.TEST, "processUrlLoading::scheme = " + scheme);        // https
            //Kit.log(Kit.LogType.TEST, "processUrlLoading::host = " + host);            // dev.cobemall.com
            //Kit.log(Kit.LogType.TEST, "processUrlLoading::path = " + path);            // /auth/logout
            //Kit.log(Kit.LogType.TEST, "processUrlLoading::query = " + query);          // null
            webView.loadUrl("javascript:closeSideMenu();");
            if (url.startsWith("af-event://")) {
                String[] urlParts = url.split("\\?");
                if (urlParts.length > 1) {
                    String query = urlParts[1];
                    String eventName = null;
                    HashMap<String, Object> eventValue = new HashMap<>();
                    Log.e(TAG, "shouldOverrideUrlLoading Appsflyer query : " + query);

                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        String key = pair[0];
                        if (pair.length > 1) {
                            Log.e(TAG, "shouldOverrideUrlLoading Appsflyer key : " + key);
                            if ("eventName".equals(key)) {
                                eventName = pair[1];
                                Log.e(TAG, "shouldOverrideUrlLoading Appsflyer eventName : " + eventName);
                            } else if ("eventValue".equals(key)) {
                                JSONObject event;
                                JSONArray keys;
                                try {
                                    event = new JSONObject(pair[1]);
                                    keys = event.names();
                                    for (int i = 0; i < keys.length(); i++) {
                                        eventValue.put(keys.getString(i), event.getString(keys.getString(i)));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    AppsFlyerLib.getInstance().logEvent(getAppContext(), eventName, eventValue);
                }
                return true;
            }

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                if (isAllowedHost(host)) {
                    if (Kit.isNotNullNotEmpty(path)) {
                        if (path.endsWith("/logout")) {
                            PrefKit.setMemberIdx(mContext, "");
                        }
                    }
                    return false;
                } else {
                    Kit.execBrowserEx(mContext, url);
                    return true;
                }
            } else if (SCHEME_KANACAT.equals(scheme)) {

                if ("newpage".equalsIgnoreCase(host)) {     // 새 웹뷰 화면
                    String urlParam = uri.getQueryParameter("url");
                    String methodParam = uri.getQueryParameter("method");

                    Intent intent = new Intent(mContext, WebActivity.class);
                    intent.putExtra(Extra.KEY_URL, urlParam);
                    intent.putExtra(Extra.KEY_METHOD, methodParam);
                    mContext.startActivity(intent);
                } else if ("command".equalsIgnoreCase(host)) {     // 네이티브 동작
                    String cmd = uri.getQueryParameter("cmd");
                    if (cmd.equalsIgnoreCase("login")) {
                        String device_id = "";
                        final String device_flag = "1";
                        String adv_id = "";
                        adv_id = PrefKit.getUserAdId(mContext);
                        String script = String.format("javascript:login_app('%s', '%s', '%s', '%s');", device_id, device_flag, mFCMToken, adv_id);
//                        Kit.log(Kit.LogType.TEST, "script = " + script);
                        loadUrl(script);
                    } else if (cmd.equalsIgnoreCase("join")) {
                        String device_id = "";
                        final String device_flag = "1";
                        String is_agree_push = PrefKit.getAgreePushNoti(mContext) ? "Y" : "N";      // 푸시 알림 동의 여부 (회원 가입시 체크 되어 있음)
//                        Kit.log(Kit.LogType.TEST, "is_agree_push = " + is_agree_push);
                        String adv_id = "";
                        adv_id = PrefKit.getUserAdId(mContext);
                        String script = String.format("javascript:join_app('%s', '%s', '%s', '%s', '%s');", device_id, device_flag, mFCMToken, is_agree_push, adv_id);
//                        Kit.log(Kit.LogType.TEST, "script = " + script);
                        loadUrl(script);
                    } else if (cmd.equalsIgnoreCase("facebook_login")) {
                        if (mContext instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) mContext;
                            mainActivity.mConnect = false;
                            mainActivity.facebookLogin();
                        }
                    } else if (cmd.equalsIgnoreCase("kakao_login")) {
                        if (mContext instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) mContext;
                            mainActivity.mConnect = false;
                            mainActivity.kakaoLogin();
                        }
                    } else if (cmd.equalsIgnoreCase("naver_login")) {
                        if (mContext instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) mContext;
                            mainActivity.mConnect = false;
                            Intent intent = new Intent(mContext, NaverLoginActivity.class);
                            mainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_NAVER_LOGIN);
                        }
                    } else if (cmd.equalsIgnoreCase("sns_connect")) {
                        if (mContext instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) mContext;
                            String type = uri.getQueryParameter("type");
                            mainActivity.mConnect = true;
                            if (type.equalsIgnoreCase("kakao")) {
                                mainActivity.kakaoLogin();
                            } else if (type.equalsIgnoreCase("naver")) {
                                Intent intent = new Intent(mContext, NaverLoginActivity.class);
                                mainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_NAVER_LOGIN);
                            } else if (type.equalsIgnoreCase("facebook")) {
                                mainActivity.facebookLogin();
                            }
                        }
                    } else if (cmd.equalsIgnoreCase("sns_disconnect")) {
                        if (mContext instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) mContext;
                            String type = uri.getQueryParameter("type");
                            mainActivity.mConnect = true;
                            if (type.equalsIgnoreCase("kakao")) {
                                mainActivity.kakaoLogout();
                                reload();
                            } else if (type.equalsIgnoreCase("naver")) {
                                mainActivity.naverLogout(mContext);
                                reload();
                            } else if (type.equalsIgnoreCase("facebook")) {
                                mainActivity.facebookLogout();
                                reload();
                            }
                        }
                    } else if (cmd.equalsIgnoreCase("popup_push_agree")) {
                        Log.e(TAG, "popup_push_agree");
//                        Kit.log(Kit.LogType.TEST, "popup_push_agree::fragmentName = " + fragmentName);
//                        Kit.log(Kit.LogType.TEST, "popup_push_agree::PrefKit.getExecCountForPushAgree(mContext) = " + PrefKit.getExecCountForPushAgree(mContext));
                        if (PrefKit.getExecCountForPushAgree(mContext) > 1) {   // 처음 실행(메인페이지 진입)이 아닐 경우
                            long time = PrefKit.getDoNotShowWeekTime(mContext);
                            long now = System.currentTimeMillis();
                            long diff = now - time;
//                            Kit.log(LogType.TEST, "popup_push_agree::diff = " + diff);
                            if (diff > 7 * 24 * 60 * 60 * 1000) {       // 7일 초과 경과시
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
//                                String today = sdf.format(new Date(now));
//                                String openedDay = sdf.format(new Date(PrefKit.getOpenPushAgreeTime(mContext)));
//                                Kit.log(LogType.TEST, "popup_push_agree::today = " + today);
//                                Kit.log(LogType.TEST, "popup_push_agree::openedDay = " + openedDay);
//                                if (!today.equals(openedDay)) {         // 같은 날에는 한 번만 노출
                                if (mContext instanceof MainActivity) {
                                    MainActivity mainActivity = (MainActivity) mContext;
                                    Intent itent = new Intent(mContext, AgreePushNotificationActivity.class);
                                    itent.putExtra(Extra.KEY_SAVE_AGREEMENT, false);
                                    mainActivity.startActivityForResult(itent, MainActivity.REQUEST_CODE_AGREE_PUSH_NOTIFICATION);
                                }
//                                }
                            }
                        }
                    }
                } else if ("pay".equalsIgnoreCase(host)) {     // 결제 화면
//                    Kit.log(Kit.LogType.TEST, "mContext instanceof AppCompatActivity = " + (mContext instanceof AppCompatActivity));
                    if (mContext instanceof MainActivity) {
                        String urlParam = uri.getQueryParameter("url");
                        MainActivity mainActivity = (MainActivity) mContext;
                        Intent intent = new Intent(mainActivity, PayActivity.class);
                        intent.putExtra(Extra.KEY_TITLE, "입장권 결제");
                        intent.putExtra(Extra.KEY_URL, urlParam);
                        mainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_PAYMENT);
                    }
                } else if ("zu_pay".equalsIgnoreCase(host)) {     // 가낳지모충전 결제 화면
//                    Kit.log(Kit.LogType.TEST, "mContext instanceof AppCompatActivity = " + (mContext instanceof AppCompatActivity));
                    if (mContext instanceof MainActivity) {
                        String urlParam = uri.getQueryParameter("url");
                        MainActivity mainActivity = (MainActivity) mContext;
                        Intent intent = new Intent(mainActivity, ZuPayActivity.class);
                        intent.putExtra(Extra.KEY_TITLE, "가낳지모 충전");
                        intent.putExtra(Extra.KEY_URL, urlParam);
                        mainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_PAYMENT);
                    }
                } else if ("save_mem_idx".equalsIgnoreCase(host)) {     // 기기정보 전송
                    String mem_idx = uri.getQueryParameter("mem_idx");
                    String user_id = uri.getQueryParameter("user_id");
                    Log.e("WebViewEx", "save_mem_idx mem_idx: " + mem_idx);
                    Log.e("WebViewEx", "save_mem_idx user_id: " + user_id);

                    String urlParam = uri.getQueryParameter("url");
                    if (Kit.isNotNullNotEmpty(mem_idx)) {
                        PrefKit.setMemberIdx(mContext, mem_idx);
                        AppsFlyerLib.getInstance().setCustomerUserId(mem_idx);
                        AppsFlyerLib.getInstance().setCustomerIdAndLogSession(mem_idx, getAppContext());
                        Log.e("WebViewEx", "save_mem_idx AppsFlyerProperties.APP_USER_ID: " + AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID));

                        // 로그인시 기존 토큰 삭제.
                        FirebaseMessaging.getInstance().deleteToken();
                    }
                    if (Kit.isNotNullNotEmpty(urlParam) && Kit.isXSSPreventSafeUrl(urlParam)) {
                        loadUrl(urlParam);
                    } else {
                        loadUrl(URL_BASE_PRD + PATH_HOME);
                    }

                } else if ("share".equalsIgnoreCase(host)) {     // 공유
                    if ("/sms".equalsIgnoreCase(path)) {
                        String contents = uri.getQueryParameter("contents");
                        ShareKit.shareSMS(mContext, contents);
                    } else if ("/kakao_link".equalsIgnoreCase(path)) {
                        String title = uri.getQueryParameter("title");
                        String image = uri.getQueryParameter("image");
                        String link = uri.getQueryParameter("link");
                        ShareKit.shareKakao(mContext, title, image, link, "link=" + link, "구매하기", "https://kanajimo.com", "가낳지모 가기");
                    }
                } else if ("go_store".equalsIgnoreCase(host)) {     // 스토어로 이동
                    Kit.openPlayStore(mContext);
                }

                return true;
            } else if ("about".equalsIgnoreCase(scheme)) {
                Kit.execBrowserEx(mContext, url);
                return true;
            } else if ("mailto".equalsIgnoreCase(scheme)) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                mContext.startActivity(intent);

                return true;
            } else if ("tel".equalsIgnoreCase(scheme)) {
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                mContext.startActivity(intent);

                return true;

            } else if ("intent".equalsIgnoreCase(scheme)) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = mContext.getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        try {
                            mContext.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(mContext, "해당 패키지를 실행 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        mContext.startActivity(marketIntent);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        // Android 7.0 (Nougat) API level 24 이상에서 호출됨
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            return super.shouldOverrideUrlLoading(view, request);
            return processUrlLoading(view, request.getUrl());
        }

        @Override
        // Android 7.0 (Nougat) API level 24 이만에서 호출됨
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            return super.shouldOverrideUrlLoading(view, url);
            return processUrlLoading(view, Uri.parse(url));
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            Kit.log(Kit.LogType.VALUE, "shouldInterceptRequest::request.getUrl() = " + request.getUrl());
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
//            Kit.log(Kit.LogType.VALUE, "onReceivedLoginRequest::account = " + account);
            super.onReceivedLoginRequest(view, realm, account, args);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Log.e("WebViewEX", "onPageStarted url : " + url);
            checkNetworkConnection();

            if (mContext instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) mContext;
                if (url.contains("myticket")) {
                    mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);

            if (mLayoutProgress != null)
                mLayoutProgress.setVisibility(View.VISIBLE);

            if (mBtnBack != null) {
                mBtnBack.setVisibility(INVISIBLE);
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //SIDE_MENU_OPEN_CHECK = false;
            //Log.e("WebViewEX", "onPageFinished url : " + url);
            checkNetworkConnection();

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.INVISIBLE);

            if (mLayoutProgress != null)
                mLayoutProgress.setVisibility(View.INVISIBLE);

            if (mBtnBack != null) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    if (host.endsWith("cobe.co.kr")) {
                        if (canGoBack()) {
                            mBtnBack.setVisibility(VISIBLE);
                        } else {
                            mBtnBack.setVisibility(INVISIBLE);
                        }
                    } else {
                        mBtnBack.setVisibility(INVISIBLE);
                    }
                }
            }

            if (mSwipeRefreshLayout != null) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            if (mContext instanceof AppCompatActivity) {
                ActionBar actionBar = ((AppCompatActivity) mContext).getSupportActionBar();
                if (actionBar != null)
                    actionBar.setTitle(view.getTitle());
            }

            // 좌상단 뒤로가기 버튼 표시/숨김
            String canGo;
            if (canGoBack()) {
                canGo = "true";
            } else {
                canGo = "false";
            }
            loadUrl(String.format("javascript:canGoBack(%s);", canGo));

            CookieManager.getInstance().flush();

            Uri uri = Uri.parse(url);
            String path = "";
            if (uri.getPath() != null) {
                path = uri.getPath().toLowerCase();
            }
            if (path.equals("/mypage/setting")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String appVersion = Kit.getPackageVersionCode(mContext);
                        String script = String.format("javascript:version_info('%s', '%s');", appVersion, "");
                        Kit.log(Kit.LogType.TEST, "script = " + script);
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                loadUrl(script);
                            }
                        });
                    }
                }).start();
            }

            if (path.equals(PATH_MEMBER_FINDID) || path.equals(PATH_MEMBER_JOIN) || path.equals(PATH_MEMBER_FINDPW)) {
                TedPermission.create()
                        .setDeniedMessage("설정에서 앱 권한을 확인해 주세요.")
                        .setRationaleConfirmText("확인")
                        .setDeniedCloseButtonText("취소")
                        .setGotoSettingButtonText("설정")
                        .setPermissionListener(new PermissionListener() {

                            @Override
                            public void onPermissionGranted() {
                                if (!TextUtils.isEmpty(Kit.getPhondNumber(getAppContext()))) {
                                    Log.e(TAG, "PhoneNum : " + Kit.getPhondNumber(getAppContext()));
                                    String script = String.format("javascript:sendPhoneNumber('%s');", Kit.getPhondNumber(getAppContext()));
                                    loadUrl(script);
                                }
                            }

                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {
                                Toast.makeText(getAppContext(), "앱 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 일부서비스의 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setPermissions(new String[]{Manifest.permission.READ_PHONE_NUMBERS})
                        .check();

            }
            // 페이지 로딩 후 값 초기화
            IR_CD_Value = "";
            URL_Value = "";
            super.onPageFinished(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            Kit.log(Kit.LogType.VALUE, "onReceivedError::errorCode = " + errorCode);
//            Kit.log(Kit.LogType.VALUE, "onReceivedError::description = " + description);
//            Kit.log(Kit.LogType.VALUE, "onReceivedError::failingUrl = " + failingUrl);
//            String html = "<html><head><title></title></head><body></body></html>";
//            view.loadData(html, "text/html", "UTF-8");

            checkNetworkConnection();

            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Kit.log(Kit.LogType.VALUE, "onReceivedHttpError::request.getUrl() = " + request.getUrl());
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Kit.log(Kit.LogType.VALUE, "onReceivedSslError::error.toString() = " + error.toString());
            super.onReceivedSslError(view, handler, error);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        private View mCustomView;
        private int mOriginalOrientation;
        private FullscreenHolder mFullscreenContainer;
        private CustomViewCallback mCustomViewCollback;
        private WebView mWebView = null;

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            if (mContext != null && !((Activity) mContext).isFinishing()) {
                try {
                    new AlertDialog.Builder(mContext)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok,
                                    new AlertDialog.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.confirm();
                                        }
                                    }).setCancelable(false).create().show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
            if (mContext != null && !((Activity) mContext).isFinishing()) {
                try {
                    new AlertDialog.Builder(mContext)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.confirm();
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            result.cancel();
                                        }
                                    })
                            .create()
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mProgressBar != null)
                mProgressBar.setProgress(progress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::view = " + view);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::isDialog = " + isDialog);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::isUserGesture = " + isUserGesture);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::resultMsg = " + resultMsg);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::resultMsg.obj = " + resultMsg.obj);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::resultMsg.arg1 = " + resultMsg.arg1);
            Kit.log(Kit.LogType.VALUE, "onCreateWindow::resultMsg.arg2 = " + resultMsg.arg2);
            Toast.makeText(mContext,
                    String.format("onCreateWindow::\nresultMsg.obj = %s\nresultMsg.arg1 = %s\nresultMsg.arg2 = %s",
                            resultMsg.obj, resultMsg.arg1, resultMsg.arg2),
                    Toast.LENGTH_SHORT).show();

            Kit.log(Kit.LogType.TEST, "onCreateWindow::mContainer = " + mContainer);
            if (mContainer != null) {
//                Kit.log(Kit.LogType.TEST, "onCreateWindow::mWebView = " + mWebView);
                if (mWebView == null) {
                    mWebView = new WebView(mContext);
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                    mWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                        return super.shouldOverrideUrlLoading(view, request);

//                            Kit.log(Kit.LogType.VALUE, "mWebView::shouldOverrideUrlLoading::request = " + request);
                            Uri uri = request.getUrl();
                            String url = uri.toString();
//                            Kit.log(Kit.LogType.VALUE, "mWebView::shouldOverrideUrlLoading::url = " + url);
                            String scheme = uri.getScheme();
                            String host = uri.getHost();
                            String path = uri.getPath();
                            String query = uri.getQuery();
//                            Kit.log(Kit.LogType.TEST, "scheme = " + scheme);
//                            Kit.log(Kit.LogType.TEST, "host = " + host);
//                            Kit.log(Kit.LogType.TEST, "path = " + path);
//                            Kit.log(Kit.LogType.TEST, "query = " + query);

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                            browserIntent.setData(Uri.parse(url));
                            mContext.startActivity(browserIntent);
                            return true;
                        }
                    });
                }

                WebViewTransport transport = (WebViewTransport) resultMsg.obj;
                transport.setWebView(mWebView);
                resultMsg.sendToTarget();

                return true;
            } else {
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
        }

        @Override
        public void onCloseWindow(WebView window) {
//            Kit.log(Kit.LogType.EVENT, "onCloseWindow");
            super.onCloseWindow(window);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
            Log.e(TAG, "onPermissionRequest : " + request.getResources());
        }

        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            super.onPermissionRequestCanceled(request);
            Log.e(TAG, "onPermissionRequestCanceled : " + request);
        }


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);

            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                TedPermission.create()
//                        .setDeniedMessage("사진 및 동영상\n앱 권한을 확인해 주세요.")
//                        .setRationaleConfirmText("확인")
//                        .setDeniedCloseButtonText("취소")
//                        .setGotoSettingButtonText("설정")
//                        .setPermissionListener(new PermissionListener() {
//
//                            @RequiresApi(api = Build.VERSION_CODES.O)
//                            @Override
//                            public void onPermissionGranted() {
//                                imageChooser(fileChooserParams.isCaptureEnabled(), fileChooserParams.getMode());
//                            }
//
//                            @Override
//                            public void onPermissionDenied(List<String> deniedPermissions) {
//                                if (mFilePathCallback != null) {
//                                    mFilePathCallback.onReceiveValue(null);
//                                }
//                                mFilePathCallback = null;
//                                Toast.makeText(mContext, "앱 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 일부서비스의 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
//                            }
//                        }).setPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES})
//                        .check();
//            } else {
                TedPermission.create()
                        .setDeniedMessage("파일 및 미디어\n앱 권한을 확인해 주세요.")
                        .setRationaleConfirmText("확인")
                        .setDeniedCloseButtonText("취소")
                        .setGotoSettingButtonText("설정")
                        .setPermissionListener(new PermissionListener() {

                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onPermissionGranted() {
                                imageChooser(fileChooserParams.isCaptureEnabled(), fileChooserParams.getMode());
                            }

                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {
                                if (mFilePathCallback != null) {
                                    mFilePathCallback.onReceiveValue(null);
                                }
                                mFilePathCallback = null;
                                Toast.makeText(mContext, "앱 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 일부서비스의 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
                        .check();
//            }

            return true;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
//            Kit.log(Kit.LogType.EVENT, "onShowCustomView");
//            super.onShowCustomView(view, callback);

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            if (mContext instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                mOriginalOrientation = activity.getRequestedOrientation();
                FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
                mFullscreenContainer = new FullscreenHolder(activity);
                mFullscreenContainer.addView(view, ViewGroup.LayoutParams.MATCH_PARENT);
                decor.addView(mFullscreenContainer, ViewGroup.LayoutParams.MATCH_PARENT);
                mCustomView = view;
                mCustomViewCollback = callback;
                activity.setRequestedOrientation(mOriginalOrientation);
            }
        }

        @Override
        public void onHideCustomView() {
//            Kit.log(Kit.LogType.EVENT, "onHideCustomView");
//            super.onHideCustomView();
            if (mCustomView == null) {
                return;
            }

            if (mContext instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
                decor.removeView(mFullscreenContainer);
                mFullscreenContainer = null;
                mCustomView = null;
                mCustomViewCollback.onCustomViewHidden();
                activity.setRequestedOrientation(mOriginalOrientation);
            }
        }

        private class FullscreenHolder extends FrameLayout {

            public FullscreenHolder(Context ctx) {
                super(ctx);
                setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
            }

            @Override
            public boolean onTouchEvent(MotionEvent evt) {
                return true;
            }
        }
    }


    public class WebViewInterface {

        @JavascriptInterface
        public void open_url(String url) {
            Kit.execBrowser(getContext(), url);
        }

        @JavascriptInterface
        public void call_tel(String telno) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telno));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }

        @JavascriptInterface
        public void share(String shareTitle, String title, String content) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, content);
                Intent chooser = Intent.createChooser(intent, shareTitle);

                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(chooser);
                } else {
                    getContext().startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "공유 기능을 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        }

        @JavascriptInterface
        public void share(String title, String content, String img_url, String path) {
            String Dynamin_Link = "https://kanajimo.page.link/?link=" + path + "&apn=com.messeesang.kanajimo&isi=1506112609&ibi=com.esmesse.CouponPet";
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                //intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content + "\n* 바로가기 : " + path);
                Intent chooser = Intent.createChooser(intent, title);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(chooser);
                } else {
                    getContext().startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "공유 기능을 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        }

        @JavascriptInterface
        public void go_store() {
            Kit.openPlayStore(mContext);
        }

        @JavascriptInterface
        public void show_bottombar() {
            String fragmentName = "";
            if (mFragment != null) {
                fragmentName = mFragment.getClass().getSimpleName();
            }
            Kit.log(Kit.LogType.VALUE, String.format("[%s]show_bottombar", fragmentName));
            if (mContext instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) mContext;
                mainActivity.show_bottombar();
            }
        }

        @JavascriptInterface
        public void hide_bottombar() {
            String fragmentName = "";
            if (mFragment != null) {
                fragmentName = mFragment.getClass().getSimpleName();
            }
            Kit.log(Kit.LogType.VALUE, String.format("[%s]hide_bottombar", fragmentName));

            if (mContext instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) mContext;
                mainActivity.hide_bottombar();
            }
        }

        // 2020-03-11 드림시큐리티 휴대폰 본인인증 처리 시작 //
        @JavascriptInterface
        public void open_hp_cert(String result) {
            mWebViewEx.post(new Runnable() {
                @Override
                public void run() {
                    mWebViewEx.loadUrl("javascript:closeSideMenu();");
                }
            });

            Log.e("WebViewEx", "open_hp_cert : " + result);
            Intent intent = new Intent(mContext, CertWebActivity.class);
            intent.putExtra("Data", result);
            mContext.startActivity(intent);
        }

        @JavascriptInterface
        public void hp_cert_ok(String result) {
            Log.e("WebViewEx", "hp_cert_ok : " + result);
            CertWebActivity mCertWebActivity = (CertWebActivity) CertWebActivity.CertWebActivity;

            try {
                JSONObject obj = new JSONObject(result);
                if (obj != null) {
                    String getResult = obj.optString("hp_cert_ok").replace("null", "");
                    String hp = obj.optString("hp").replace("null", "");
                    String name = obj.optString("name").replace("null", "");
                    String ci = obj.optString("ci").replace("null", "");
                    String di = obj.optString("di").replace("null", "");
                    String net_oper = obj.optString("net_oper").replace("null", "");
                    String birth = obj.optString("birth").replace("null", "");
                    String gender = obj.optString("gender").replace("null", "");
                    String foreign = obj.optString("foreign").replace("null", "");
                    String req_no = obj.optString("req_no").replace("null", "");
                    String req_date = obj.optString("req_date").replace("null", "");

                    if (getResult.equals("ok")) {
                        mWebViewEx.post(new Runnable() {
                            @Override
                            public void run() {
                                String script = String.format("javascript:hp_cert_ok('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", hp, name, ci, di, net_oper, birth, gender, foreign, req_no, req_date);
                                mWebViewEx.loadUrl(script);
                            }
                        });
                    } else {
                        Toast.makeText(getAppContext(), "본인인증이 정상적으로 이루어 지지 않았습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getAppContext(), "본인인증이 정상적으로 이루어 지지 않았습니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            }
            mCertWebActivity.finish();
        }

        @JavascriptInterface
        public void close(String result) {
            CertWebActivity mCertWebActivity = (CertWebActivity) CertWebActivity.CertWebActivity;
            mCertWebActivity.finish();
        }

        // 2020-03-11 드림시큐리티 휴대폰 본인인증 처리 끝 //
        @JavascriptInterface
        public void closedSideMenu() {
            SIDE_MENU_OPEN_CHECK = false;
        }

        @JavascriptInterface
        public void openedSideMenu() {
            SIDE_MENU_OPEN_CHECK = true;
        }

        @JavascriptInterface
        public void recordEvent(String name, String json) {
            Map<String, Object> params = null;
            if (json != null) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    params = new HashMap<>();
                    Iterator keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        Object value = jsonObject.opt(key);
                        params.put(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            AppsFlyerLib.getInstance().logEvent(getAppContext(), name, params);
        }
    }

    protected void checkNetworkConnection() {
        if (Kit.isNetworkConnected(mContext)) {
            if (mNetworkCautionPopup != null && mNetworkCautionPopup.isShowing()) {
                mNetworkCautionPopup.dismiss();
            }
        } else {
            if (!((Activity) mContext).isFinishing()) {
                showNetworkCautionPopup();
            }
        }
    }

    protected void showNetworkCautionPopup() {
        try {
            if (mContainer == null)
                return;

            if (mNetworkCautionPopup == null) {
                View view = View.inflate(mContext, R.layout.layout_popup_network_caution, null);
                ImageButton btnRetry = view.findViewById(R.id.btnRetry);
                btnRetry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reload();
                    }
                });

                mNetworkCautionPopup = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
                mNetworkCautionPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            if (mNetworkCautionPopup.isShowing())
                return;

            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    if (mNetworkCautionPopup != null && mContext != null && !((Activity) mContext).isFinishing()) {
                        mNetworkCautionPopup.showAtLocation(mContainer, Gravity.NO_GRAVITY, 0, 0);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // input file (이미지 파일 선택 처리)
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void imageChooser(boolean camera, int mode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(getClass().getName(), "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        "com.messeesang.kanajimo.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            } else {
                takePictureIntent = null;
            }
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        if (mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        if (mContext instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) mContext;

            if (camera) {
                activity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_CODE_INPUT_FILE);
            } else {
                activity.startActivityForResult(chooserIntent, MainActivity.REQUEST_CODE_INPUT_FILE);
            }
        } else {
            Toast.makeText(mContext, "이미지를 가져올 수 없습니다.", Toast.LENGTH_LONG).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDirFileDelete(); // 사이즈 0인 파일 지우기.
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        String imageFileName = "kanajimo_" + timeStamp + "_";

        File storageDir;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            storageDir = new File(mContext.getExternalFilesDir(DIRECTORY_PICTURES).getPath() + File.separator + "kanajimo/");
        } else {
            storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "kanajimo/");
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File imageFile = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",   // suffix
                storageDir      // directory
        );

        mCameraPhotoPath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    }

    protected Uri getResultUri(Intent data) {
        Uri result = null;
        if (data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if (mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathKit.getRealPath(mContext, data.getData());
            }
            result = Uri.parse(filePath);
        }
        return result;
    }

    public boolean onActivityResultForInputFile(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MainActivity.REQUEST_CODE_INPUT_FILE) {

            if (mFilePathCallback == null) {
                return false;
            }
            if (data == null) {
                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;

                return true;
            } else {
                if (data.getClipData() == null) { // 한장 선택 시
                    Uri[] results = new Uri[]{getResultUri(data)};
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                } else {
                    ClipData clipData = data.getClipData();

                    Uri[] resultsmt = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        try {
                            resultsmt[i] = clipData.getItemAt(i).getUri(); // 선택한 이미지들의 uri를 가져온다.
                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                    mFilePathCallback.onReceiveValue(resultsmt);
                    mFilePathCallback = null;
                }
                return true;
            }
        } else {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = null;
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setDirFileDelete() {
        File storageDir;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            storageDir = new File(mContext.getExternalFilesDir(DIRECTORY_PICTURES).getPath() + File.separator + "kanajimo/");
        } else {
            storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "kanajimo/");
        }

        File[] childFileList = storageDir.listFiles();
        if (storageDir.exists()) {
            for (File childFile : childFileList) {
                try {
                    if (Files.size(Paths.get(childFile.getPath())) == 0) {
                        childFile.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // 약관 및 정책 페이지와 같은 WebView 내부에 스크롤 영역이 있을 때 스크롤 영역 내에서 swipe down시 SwipeRefreshLayout가 동작하는 것에 대한 처리
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
//        Kit.log(Kit.LogType.EVENT, "onOverScrolled");

        if (clampedX || clampedY) {
            //Content is not scrolling
            //Enable SwipeRefreshLayout
            ViewParent parent = this.getParent();
            if (parent instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) parent).setEnabled(true);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
//        Kit.log(Kit.LogType.EVENT, "onTouchEvent::event.getActionMasked()  = " + event.getActionMasked());

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            //Disable SwipeRefreshLayout
            ViewParent parent = this.getParent();
            if (parent instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) parent).setEnabled(false);
            }
        }
        return true;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean isAllowedHost(String host) {
        return (host.endsWith("kanajimo.co.kr") || host.endsWith("dev.kanajimo.co.kr") || host.endsWith("bit.ly"));
    }
}
