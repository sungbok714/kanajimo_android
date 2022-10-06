package com.messeesang.kanajimo.kit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.messeesang.kanajimo.MyApplication.AlertDialog_Check;
import static com.messeesang.kanajimo.kit.Kit.isNetworkConnected;

public class TelKit {

    public static final String URL_API_BASE_PRD = "kanajimo.co.kr";  // 도메인

    //public static final String URL_BASE_PRD = "https://" + URL_API_BASE_PRD;    //운영
    public static final String URL_BASE_PRD = "https://dev." + URL_API_BASE_PRD;  //개발

    public static final String PATH_HOME = "/";
    public static final String PATH_TICKET = "/ticket";
    public static final String PATH_TICKET_LIST = "/ticket_list";
    public static final String PATH_COUPON = "/coupon";
    public static final String PATH_EVENT = "/event";
    public static final String PATH_SEARCH = "/search/search_word"; // 검색
    public static final String PATH_MY_PAGE = "/mypage"; // 마이페이지
    public static final String PATH_CHALLENGE = "/challenge"; // 챌린지

    public static final String PATH_AUTH_LOGIN = "/auth/login";
    public static final String PATH_MEMBER_FINDID = "/member/findid";
    public static final String PATH_MEMBER_FINDPW = "/member/findpw";
    public static final String PATH_MEMBER_JOIN = "/member/join";

    public static final String PATH_REQUEST_DEVICE_INFO = "/api_client/member_device_info";
    public static final String PATH_REQUEST_VERSION_INFO = "/api_client/app_version";
    public static final String PATH_REQUEST_END_POPUP = "/api_client/endpop_api";

    private RequestAsyncTask mRequestTask = null;
    private OnResultListener mOnResultListener = null;
    private OkHttpClient mHttpClient = null;
    private ProgressBar mProgressBar = null;
    private View mProgressView = null;
    private ProgressDialog mProgressDialog = null;
    private Context mContext = null;
    private LinearLayout mLinearLayout = null;

    public class Result {
        public boolean mIsSucc = false;
        public String mRequestUrl = "";
        public String mResponse = "";
        public int mRequestCode = 0;
    }

    public interface OnResultListener {
        public abstract void onResult(Result result);
    }

    public TelKit(Context context, OnResultListener listener) {
        super();
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnResultListener = listener;
        initHttpClient();
    }

    public TelKit(Context context, OnResultListener listener, ProgressBar progressBar) {
        super();
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnResultListener = listener;
        mProgressBar = progressBar;
        initHttpClient();
    }

    public TelKit(Context context, OnResultListener listener, View progressView) {
        super();
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnResultListener = listener;
        mProgressView = progressView;
        initHttpClient();
    }

    public TelKit(Context context, OnResultListener listener, ProgressDialog progressDialog) {
        super();
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnResultListener = listener;
        mProgressDialog = progressDialog;
        initHttpClient();
    }

    public TelKit(Context context, OnResultListener listener, LinearLayout linearLayout) {
        super();
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnResultListener = listener;
        mLinearLayout = linearLayout;
        initHttpClient();
    }

    public void initHttpClient() {
        if (mHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(20 * 1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
            mHttpClient = builder.build();
        }
    }

    @Deprecated
    public void request(String url, String body) {
        request(url, body, 0);
    }

    @Deprecated
    public void request(String url, String body, int requestCode) {
        if (isNetworkConnected(mContext)) {
            if (mRequestTask != null) {
                if (mRequestTask.isCancelled() == false)
                    mRequestTask.cancel(true);
            }

            mRequestTask = new RequestAsyncTask(url, body, requestCode);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mRequestTask.execute();
            }

        } else {
            if (AlertDialog_Check == false) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog_Check = false;
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                if (alertDialog != null && !alertDialog.isShowing()) {
                    AlertDialog_Check = true;
                    alertDialog.show();
                }
            }
        }
    }

    public void request(String url, HashMap<String, String> body) {
        request(url, body, 0);
    }

    public void request(String url, HashMap<String, String> body, int requestCode) {
        if (isNetworkConnected(mContext)) {
            if (mRequestTask != null) {
                if (mRequestTask.isCancelled() == false)
                    mRequestTask.cancel(true);
            }

            mRequestTask = new RequestAsyncTask(url, body, requestCode);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mRequestTask.execute();
            }

        } else {
            if (AlertDialog_Check == false) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog_Check = false;
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                if (alertDialog != null && !alertDialog.isShowing()) {
                    AlertDialog_Check = true;
                    alertDialog.show();
                }
            }
        }
    }

    private class RequestAsyncTask extends AsyncTask<Void, Integer, Result> {
        private String mUrl = "";
        private String mBody = "";
        private HashMap<String, String> mBodyMap = null;
        private int mRequestCode = 0;

        public RequestAsyncTask(String url, String body, int requestCode) {
            super();

            mUrl = url;
            mBody = body;
            mRequestCode = requestCode;
        }

        public RequestAsyncTask(String url, HashMap<String, String> body, int requestCode) {
            super();

            mUrl = url;
            mBodyMap = body;
            mRequestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (mProgressView != null) {
                mProgressView.setVisibility(View.VISIBLE);
            }

            if (mProgressDialog != null) {
                mProgressDialog.show();
            }

            if (mLinearLayout != null) {
                mLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Result doInBackground(Void... args) {
            Result result = new Result();
            result.mIsSucc = false;
            result.mRequestUrl = mUrl;
            result.mResponse = "";
            result.mRequestCode = mRequestCode;

            Kit.log(Kit.LogType.TELKIT, "RequestAsyncTask::mUrl = " + mUrl);
            Kit.loglong(Kit.LogType.TELKIT, "RequestAsyncTask::mBody = " + mBody, 700);
            Kit.loglong(Kit.LogType.TELKIT, "RequestAsyncTask::mBodyMap = " + mBodyMap, 700);

            // get
//            RequestBody body = RequestBody.create(TEXT, mBody);
//            Request.Builder reqBuilder = new Request.Builder();
//            reqBuilder.url(mUrl + "?" + mBody);
//            reqBuilder.get();

            FormBody.Builder formBuilder = new FormBody.Builder();
            if (!mBody.isEmpty()) {
                Map<String, String> map = Kit.getQueryMap(mBody);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
            }
            if (mBodyMap != null) {
                for (Map.Entry<String, String> entry : mBodyMap.entrySet()) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
            }
            RequestBody body = formBuilder.build();
            Request.Builder reqBuilder = new Request.Builder();

            //String url_api_base = PrefKit.getTestMode(mContext) ? URL_API_BASE_DEV : URL_API_BASE_PRD;
            String url = URL_BASE_PRD + mUrl;
            //String url = URL_API_BASE_DEV + mUrl;
            Kit.log(Kit.LogType.TELKIT, "RequestAsyncTask::mUrl: " + url);
            reqBuilder.url(url);
            reqBuilder.post(body);
            Request request = reqBuilder.build();
            try {
                Response resp = mHttpClient.newCall(request).execute();
                result.mResponse = resp.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                result.mIsSucc = false;
                return result;
            }

            result.mIsSucc = true;

            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            // TODO Auto-generated method stub
            Kit.log(Kit.LogType.TELKIT, "TelKit::RequestAsyncTask::onPostExecute::result.mIsSucc = " + result.mIsSucc);
            Kit.log(Kit.LogType.TELKIT, "TelKit::RequestAsyncTask::onPostExecute::result.mRequestUrl = " + result.mRequestUrl);
            Kit.loglong(Kit.LogType.TELKIT, "TelKit::RequestAsyncTask::onPostExecute::result.mResponse = " + result.mResponse, 700);
            if (mOnResultListener != null)
                mOnResultListener.onResult(result);

            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            if (mProgressView != null) {
                mProgressView.setVisibility(View.INVISIBLE);
            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            if (mLinearLayout != null) {
                mLinearLayout.setVisibility(View.GONE);
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }
    }

    public static void tokenRegistrationToServer(Context context, String mem_idx, String token) {
//        Kit.log(Kit.LogType.TELKIT, "tokenRegistrationToServer::mem_idx: " + token);
//        Kit.log(Kit.LogType.TELKIT, "tokenRegistrationToServer::token: " + token);

        if (!Kit.isNotNullNotEmpty(token))
            return;

        String device_id = "";
        String device_uuid = "";

        String adv_id = "";
        try {
            adv_id = PrefKit.getUserAdId(context);
            if (TextUtils.isEmpty(adv_id)) {
                adv_id = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String bodyStr = String.format("mem_idx=%s&device_id=%s&token_id=%s&device_uuid=%s&device_flag=1&adv_id=%s",
                mem_idx,
                device_id,
                token,
                device_uuid,
                adv_id);
//        Kit.log(Kit.LogType.TELKIT, "tokenRegistrationToServer::bodyStr: " + bodyStr);

        Log.e("TelKit", "tokenRegistrationToServer bodyStr : " + bodyStr);
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (!bodyStr.isEmpty()) {
            Map<String, String> map = Kit.getQueryMap(bodyStr);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody body = formBuilder.build();

        //request
        Request request = new Request.Builder()
                .url(URL_BASE_PRD + PATH_REQUEST_DEVICE_INFO)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Kit.log(Kit.LogType.TELKIT, "onFailure::response = " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Kit.log(Kit.LogType.TELKIT, "onResponse::response.body().string() = " + response.body().string());
            }
        });
    }
}
