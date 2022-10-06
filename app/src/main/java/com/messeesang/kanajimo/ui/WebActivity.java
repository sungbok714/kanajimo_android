package com.messeesang.kanajimo.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.Kit;


public class WebActivity extends BaseActivity {
    private Toolbar mToolbar = null;
    private WebViewEx mWebViewEx = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private ProgressBar progress_bar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mToolbar = findViewById(R.id.toolbar);
        mWebViewEx = findViewById(R.id.webViewEx);
        mSwipeRefreshLayout = findViewById(R.id.layoutSwipe);
        progress_bar = findViewById(R.id.progress_bar);
        mWebViewEx.setProgressBar(progress_bar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        mWebViewEx.setContainer((RelativeLayout) findViewById(R.id.layoutRoot));
        mWebViewEx.setBackButton((ImageButton) findViewById(R.id.btnBack));
        mWebViewEx.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mWebViewEx.setProgressBar(progress_bar);

        Intent intent = getIntent();
        String url = intent.getStringExtra(Extra.KEY_URL);
        String method = intent.getStringExtra(Extra.KEY_METHOD);
//        Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::url = " + url);
//        Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::method = " + method);
        if (method == null || method.isEmpty()) {
            method = "get";
        }
        if (Kit.isNotNullNotEmpty(url) && Kit.isNotNullNotEmpty(method)) {
            if (Kit.isXSSPreventSafeUrl(url)) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                String path = uri.getPath();
                String query = uri.getQuery();
                if (method.equalsIgnoreCase("post")) {
//                Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::scheme = " + scheme);
//                Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::host = " + host);
//                Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::path = " + path);

                    String postUrl = String.format("%s://%s%s", scheme, host, path);
//                Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::postUrl = " + postUrl);
//                Kit.log(Kit.LogType.TEST, "WebActivity::onCreate::query = " + query);
                    mWebViewEx.postUrl(postUrl, query.getBytes());
                } else {
                    mWebViewEx.loadUrl(url);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebViewEx.canGoBack()) {
            mWebViewEx.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
