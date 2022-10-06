package com.messeesang.kanajimo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerProperties;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.appsflyer.deeplink.DeepLink;
import com.appsflyer.deeplink.DeepLinkListener;
import com.appsflyer.deeplink.DeepLinkResult;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.messeesang.kanajimo.kit.Kit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Pattern;

public class MyApplication extends Application {
    private static Context context;
    public static boolean AlertDialog_Check = false;

    public static boolean isLogin_Check = false;
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static String IR_CD_Value = "";
    public static String URL_Value = "";

    private static volatile MyApplication instance = null;

    private static volatile MyApplication obj = null;
    private static volatile Activity currentActivity = null;

    public Uri m_uriResult;
    public boolean b_type = false;

    public static final String m_strLogTag = "PaySample";

    public static boolean SIDE_MENU_OPEN_CHECK = false;

    //# Appsflyer Start
    private static final String AF_DEV_KEY = "";
    public static final String LOG_TAG = "MyApplication";
    //# Appsflyer End

    /**
     * singleton 애플리케이션 객체를 얻는다.
     *
     * @return singleton 애플리케이션 객체
     */
    public static MyApplication getGlobalApplicationContext() {
        if (instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    private static class KakaoSDKAdapter extends KakaoAdapter {
        /**
         * Session Config에 대해서는 default값들이 존재한다.
         * 필요한 상황에서만 override해서 사용하면 됨.
         *
         * @return Session의 설정값.
         */
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    //return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
                    return new AuthType[]{AuthType.KAKAO_TALK};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return MyApplication.getGlobalApplicationContext();
                }
            };
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.context = getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // kakao
        instance = this;
        KakaoSDK.init(new KakaoSDKAdapter());

        // facebook
        //FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.fullyInitialize();
        AppEventsLogger.activateApp(this);

        // Android 9 (Pie) 이상에서 Multi-Process WebView 지원
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                WebView.setDataDirectorySuffix(Application.getProcessName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //# Appsflyer Start
        AppsFlyerLib appsflyerlib = AppsFlyerLib.getInstance();
        appsflyerlib.setMinTimeBetweenSessions(0);
        appsflyerlib.setDebugLog(false);
        appsflyerlib.waitForCustomerUserId(false);

        appsflyerlib.subscribeForDeepLink(new DeepLinkListener() {
            @Override
            public void onDeepLinking(@NonNull DeepLinkResult deepLinkResult) {
                DeepLinkResult.Error dlError = deepLinkResult.getError();
                if (dlError != null) {
                    // You can add here error handling code
                    Log.d(LOG_TAG, "There was an error getting Deep Link data");
                    return;
                }
                DeepLink deepLinkObj = deepLinkResult.getDeepLink();
                try {
                    Log.d(LOG_TAG, "The DeepLink data is: " + deepLinkObj.toString());
                    if (deepLinkObj != null) {
                        try {
                            if (Kit.isNotNullNotEmpty(deepLinkObj.getStringValue("ir_cd"))) {
                                Log.e(LOG_TAG, "onAppOpenAttribution: Deep linking into ir_cd : " + URLDecoder.decode(deepLinkObj.getStringValue("ir_cd"), "UTF-8"));
                                IR_CD_Value = URLDecoder.decode(deepLinkObj.getStringValue("ir_cd"), "UTF-8");
                            }
                            if (Kit.isNotNullNotEmpty(deepLinkObj.getStringValue("url"))) {
                                Log.e(LOG_TAG, "onAppOpenAttribution: Deep linking into url : " + URLDecoder.decode(deepLinkObj.getStringValue("url"), "UTF-8"));
                                URL_Value = URLDecoder.decode(deepLinkObj.getStringValue("url"), "UTF-8");
                            }

                            if (Kit.isNotNullNotEmpty(deepLinkObj.getStringValue("deep_link_value"))) {
                                Log.e(LOG_TAG, "onAppOpenAttribution: Deep linking into url : " + URLDecoder.decode(deepLinkObj.getStringValue("deep_link_value"), "UTF-8"));
                                URL_Value = URLDecoder.decode(deepLinkObj.getStringValue("deep_link_value"), "UTF-8");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, "DeepLink data came back null");
                    return;
                }
            }
        });

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                for (String attrName : conversionData.keySet())
                    Log.d(LOG_TAG, "Conversion attribute: " + attrName + " = " + conversionData.get(attrName));
                //TODO - remove this
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                Log.d(LOG_TAG, "onAppOpenAttribution: This is fake call.");
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };

        appsflyerlib.init(AF_DEV_KEY, conversionListener, this);
        appsflyerlib.start(this, AF_DEV_KEY, myListener());

        Log.d("MyApplication", "AppsFlyerProperties.APP_USER_ID: " + AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID));
        if (Kit.isNotNullNotEmpty(AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID))) {
            appsflyerlib.setCustomerIdAndLogSession(AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID), this);
        }
        //# Appsflyer End
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static String getMarketVersionFast(String packageName) {
        String MarketVersion = null;

        try {
            Elements elements = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName)
                    .timeout(30000)
                    .referrer("http://www.google.com")
                    .get()
                    .select("span.htlgb");

            if (elements.size() != 0) {
                for (int i = 0; i < elements.size(); i++) {
                    String htlgb = elements.get(i).ownText();
                    //Log.d("MyApplication", "htlgb : " + htlgb);
                    if (!TextUtils.isEmpty(htlgb)) {
                        if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", htlgb)) {
                            MarketVersion = htlgb;
                            Log.d("MyApplication", "MarketVersion : " + MarketVersion);
                            return MarketVersion;
                        }
                    }
                }
            } else {
                Connection.Response response = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName)
                        .method(Connection.Method.GET)
                        .execute();
                Document document = response.parse();
                Elements elements_sub = document.select("script");

                for (int i = 0; i < elements_sub.size(); i++) {
                    String html = elements_sub.get(i).html();
                    String[] html_arry = html.split(",");

                    if (html_arry != null) {
                        int nCnt = html_arry.length;

                        for (int k = 0; k < nCnt; ++k) {
                            if (Pattern.matches("\"[0-9]{1}.[0-9]{1}.[0-9]{1}\"", html_arry[k])) {
                                MarketVersion = html_arry[k].replace("\"", "");
                                return MarketVersion;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    //# Appsflyer Start
    private AppsFlyerRequestListener myListener() {
        return new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d("MyApplication", "Event sent successfully");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("MyApplication", "Event failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        };
    }
    //# Appsflyer End
}
