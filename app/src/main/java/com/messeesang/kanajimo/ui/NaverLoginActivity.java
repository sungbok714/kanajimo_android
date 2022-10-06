package com.messeesang.kanajimo.ui;

import android.content.Intent;
import android.os.Bundle;

import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.Kit;

public class NaverLoginActivity extends BaseLoginActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_naver_login);

        naverLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Kit.log(Kit.LogType.EVENT, "onActivityResult");
        Kit.log(Kit.LogType.VALUE, "onActivityResult::requestCode = " + requestCode);
        Kit.log(Kit.LogType.VALUE, "onActivityResult::resultCode = " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onLoginSuccess(String loginType, String userID, String userName, String accessToken) {
        Kit.log(Kit.LogType.EVENT, "NaverLoginActivity::onLoginSuccess");
        Kit.log(Kit.LogType.VALUE, "NaverLoginActivity::onLoginSuccess::loginType = " + loginType);
        Kit.log(Kit.LogType.VALUE, "NaverLoginActivity::onLoginSuccess::userID = " + userID);
        Kit.log(Kit.LogType.VALUE, "NaverLoginActivity::onLoginSuccess::userName = " + userName);
        Kit.log(Kit.LogType.VALUE, "NaverLoginActivity::onLoginSuccess::accessToken = " + accessToken);

        Intent intent = new Intent();
        intent.putExtra(Extra.KEY_LOGIN_TYPE, loginType);
        intent.putExtra(Extra.KEY_USER_ID, userID);
        intent.putExtra(Extra.KEY_USER_NAME, userName);
        intent.putExtra(Extra.KEY_ACCESS_TOKEN, accessToken);

        setResult(RESULT_OK, intent);
        finish();

        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onLoginFailed(String loginType) {
        Kit.log(Kit.LogType.EVENT, "NaverLoginActivity::onLoginFailed");
        Kit.log(Kit.LogType.VALUE, "NaverLoginActivity::onLoginFailed::loginType = " + loginType);

        Intent intent = new Intent();
        intent.putExtra(Extra.KEY_LOGIN_TYPE, loginType);

        setResult(RESULT_CANCELED, intent);
        finish();

        overridePendingTransition(0, android.R.anim.fade_out);
    }
}
