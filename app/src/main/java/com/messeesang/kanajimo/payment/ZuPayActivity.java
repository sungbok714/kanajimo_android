package com.messeesang.kanajimo.payment;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.messeesang.kanajimo.ui.WebViewEx.SCHEME_KANACAT;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.appsflyer.AppsFlyerLib;
import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.Kit;
import com.messeesang.kanajimo.ui.BaseActivity;
import com.messeesang.kanajimo.ui.SslAlertDialog;
import com.messeesang.kanajimo.ui.WebActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ZuPayActivity extends BaseActivity {
    private String TAG = getClass().getSimpleName();
    private Toolbar mToolbar = null;
    private RelativeLayout mLayoutProgress = null;
    private TextView mTxtTitle = null;
    private ImageButton mBtnClose = null;

    public WebView mWebView;
    private final String SCHEME = SCHEME_KANACAT + "pay://card_pay";

    private boolean mIsCloseAndReload = false;

    public static final String SCHEME_APP = "kanacatpay";
    private ZuPayActivityInterface mZuPayActivityInterface = new ZuPayActivityInterface();
    private String Title = "";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        mToolbar = findViewById(R.id.toolbar);
        mLayoutProgress = findViewById(R.id.layoutProgress);
        mTxtTitle = findViewById(R.id.txtTitle);
        mBtnClose = findViewById(R.id.btnClose);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFinish();
            }
        });

        mWebView = findViewById(R.id.webview);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new mWebViewClient());
        mWebView.setWebViewClient(new SslWebViewConnect());

        mWebView.getSettings().setSavePassword(false);
        //mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setTextZoom(110);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        String userAgent = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString(userAgent + "/AppVer" + Kit.getPackageVersionName(this) + "/kanacat");

        mWebView.addJavascriptInterface(mZuPayActivityInterface, SCHEME_APP);

        Intent intent = getIntent();
        String url = intent.getStringExtra(Extra.KEY_URL);
        Title = intent.getStringExtra(Extra.KEY_TITLE);
        mTxtTitle.setText(Title);
        String method = intent.getStringExtra(Extra.KEY_METHOD);
        if (method == null || method.isEmpty()) {
            method = "post";
        }

//        Kit.log(Kit.LogType.VALUE, "PayActivity::onCreate::url = " + url);
//        Kit.log(Kit.LogType.VALUE, "PayActivity::onCreate::method = " + method);
        if (Kit.isNotNullNotEmpty(url) && Kit.isNotNullNotEmpty(method)) {
            if (Kit.isXSSPreventSafeUrl(url)) {
                if (method.equalsIgnoreCase("post")) {
                    Uri uri = Uri.parse(url);
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String path = uri.getPath();
                    String query = uri.getQuery();

                    String postUrl = String.format("%s://%s%s", scheme, host, path);
//                    Kit.log(Kit.LogType.VALUE, "postUrl = " + postUrl);
                    String postData = String.format("AppUrl=%s&%s", SCHEME, query);
//                    Kit.log(Kit.LogType.VALUE, "postData = " + postData);
                    mWebView.postUrl(postUrl, postData.getBytes());
                } else {
                    mWebView.loadUrl(url);
                }
            }
        }
    }

    private class mWebViewClient extends WebViewClient {
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

        protected boolean processUrlLoading(WebView view, Uri uri) {
            String url = uri.toString();
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();
            //Kit.log(Kit.LogType.TEST, "scheme = " + scheme);
            //Kit.log(Kit.LogType.TEST, "host = " + host);
            //Kit.log(Kit.LogType.TEST, "path = " + path);
            //Kit.log(Kit.LogType.TEST, "query = " + query);

            if (url != null && !url.equals("about:blank")) {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    if (Kit.isXSSPreventSafeUrl(url)) {
                        view.loadUrl(url);
                    }
                    return false;

                } else if (url.startsWith(SCHEME_KANACAT + "://")) {
                    if ("pay_close_and_move".equalsIgnoreCase(host)) {

                        String urlParam = uri.getQueryParameter("url");

                        Intent intent = new Intent();
                        intent.putExtra(Extra.KEY_URL, urlParam);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if ("pay_close_and_reload".equalsIgnoreCase(host)) {
                        mIsCloseAndReload = true;
                    }
                    return true;
                } else if (url.startsWith("mailto:")) {
                    return false;
                } else if (url.startsWith("tel:")) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            Kit.log(Kit.LogType.VALUE, "onPageStarted::url = " + url);

            if (mLayoutProgress != null)
                mLayoutProgress.setVisibility(VISIBLE);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            Kit.log(Kit.LogType.VALUE, "onPageFinished::url = " + url);

            if (mLayoutProgress != null)
                mLayoutProgress.setVisibility(INVISIBLE);

            CookieManager.getInstance().flush();

            super.onPageFinished(view, url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        confirmFinish();
    }

    @Override
    public void finish() {
        super.finish();
    }

    protected void confirmFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("창을 닫으시겠습니까?");
        builder.setCancelable(false);
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIsCloseAndReload) {
                            Intent intent = new Intent();
                            intent.putExtra(Extra.KEY_PARENT_RELOAD, true);
                            setResult(RESULT_OK, intent);
                        }
                        finish();
                    }
                });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }

    // 2020-03-12 SSL인증서 무시 & 이니시스결제 클래스 시작
    private class SslWebViewConnect extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mLayoutProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mLayoutProgress.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //handler.proceed(); // SSL 에러가 발생해도 계속 진행!
            SslAlertDialog dialog = new SslAlertDialog(handler, ZuPayActivity.this);
            dialog.show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            //String query = uri.getQuery();
            Kit.log(Kit.LogType.TEST, "scheme = " + scheme);
            Kit.log(Kit.LogType.TEST, "host = " + host);
            Kit.log(Kit.LogType.TEST, "path = " + path);
            //Kit.log(Kit.LogType.TEST, "query = " + query);

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
                            if ("eventName".equals(key)){
                                eventName = pair[1];
                                Log.e(TAG, "shouldOverrideUrlLoading Appsflyer eventName : " + eventName);
                            } else if ("eventValue".equals(key)){
                                JSONObject event;
                                JSONArray keys;
                                try {
                                    event = new JSONObject(pair[1]);
                                    keys = event.names();
                                    for (int i = 0; i < keys.length(); i++){
                                        eventValue.put(keys.getString(i), event.getString(keys.getString(i)));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    AppsFlyerLib.getInstance().logEvent(getApplicationContext(),eventName,eventValue);
                }
                return true;
            }

            if ("http".equalsIgnoreCase(scheme) && "messeesang.com".equals(host)) {
                Kit.execBrowserEx(ZuPayActivity.this, url);
                return true;
            } else if (SCHEME_KANACAT.equals(scheme)) {
                if ("newpage".equalsIgnoreCase(host)) {     // 새 웹뷰 화면
                    String urlParam = uri.getQueryParameter("url");
                    String methodParam = uri.getQueryParameter("method");

                    Intent intent = new Intent(ZuPayActivity.this, WebActivity.class);
                    intent.putExtra(Extra.KEY_URL, urlParam);
                    intent.putExtra(Extra.KEY_METHOD, methodParam);
                    startActivity(intent);
                }
            } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                Intent intent;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException ex) {
                    return false;
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    if (url.startsWith("ispmobile://")) {
                        showDialog(1005);
                        return false;
                    } else if (url.startsWith("intent")) {
                        try {
                            Intent tempIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String strParams = tempIntent.getDataString();

                            Intent intent2 = new Intent(Intent.ACTION_VIEW);
                            intent2.setData(Uri.parse(strParams));

                            startActivity(intent2);
                            return true;
                        } catch (Exception e1) {

                            e1.printStackTrace();
                            Intent intent3 = null;

                            try {
                                intent3 = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                marketIntent.setData(Uri.parse("market://details?id=" + intent3.getPackage()));
                                startActivity(marketIntent);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            return true;
                        }
                    }
                }
            } else {
                view.loadUrl(url);
                return false;
            }
            return true;
        }
    }

    public class ZuPayActivityInterface {

        @JavascriptInterface
        public void kanacatpayment(String url) {
            Log.e(TAG, "kanacatpayment url : " + url);
            Intent intent = new Intent();
            intent.putExtra(Extra.KEY_URL, url);
            setResult(RESULT_OK, intent);
            finish();
        }

        @JavascriptInterface
        public void closePay() {
            Log.e(TAG, "closePay ");
            confirmFinish();
        }

        @JavascriptInterface
        public void recordEvent(String name, String json){
            Log.e(TAG, "recordEvent name : " + name);
            Log.e(TAG, "recordEvent json : " + json);
            Map<String, Object> params = null;
            if(json!=null) {
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
            AppsFlyerLib.getInstance().logEvent(getApplicationContext(), name, params);
        }

    }
    // 2020-03-12 SSL인증서 무시 & 이니시스결제 클래스 끝
}