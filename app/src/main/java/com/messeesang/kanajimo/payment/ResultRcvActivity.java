package com.messeesang.kanajimo.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.messeesang.kanajimo.MyApplication;
import com.messeesang.kanajimo.ui.WebViewEx;

public class ResultRcvActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(MyApplication.m_strLogTag, "[ResultRcvActivity] called__onCreate");

        super.onCreate(savedInstanceState);

        // TODO Auto-generated method stub
        MyApplication myApp = (MyApplication) getApplication();
        Intent myIntent = getIntent();

        Log.d(MyApplication.m_strLogTag,
                "[ResultRcvActivity] launch_uri=[" + myIntent.getData().toString() + "]");

        if (myIntent.getData().getScheme().equals(WebViewEx.SCHEME_KANACAT) == true) {
            myApp.b_type = true;
            myApp.m_uriResult = myIntent.getData();
        } else {
            myApp.m_uriResult = null;
        }

        finish();
    }
}