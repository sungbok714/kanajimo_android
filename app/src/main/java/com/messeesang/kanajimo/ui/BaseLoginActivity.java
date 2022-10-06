package com.messeesang.kanajimo.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.AuthType;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.User;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.messeesang.kanajimo.kit.Kit;
import com.messeesang.kanajimo.kit.PrefKit;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;

import org.json.JSONObject;

import java.util.Arrays;

public abstract class BaseLoginActivity extends BaseActivity {
    // Facebook
    protected CallbackManager mFacebookCallbackManager = null;
    protected AccessTokenTracker mFacebookAccessTokenTracker = null;

    // Kakao
    protected SessionCallback mKakaoSessionCallback = null;

    // Naver Login
    public static final String OAUTH_CLIENT_ID = "XlE0i0sZU6tglY0OYIjs";
    public static final String OAUTH_CLIENT_SECRET = "BNaZ_EqCZ9";
    public static final String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";
    protected static NidOAuthLogin mNidOAuthLogin = null;
    protected static NaverIdLoginSDK mNaverIdLoginSDK = null;
    protected Context mContext;

    protected boolean mConnect = false;     // 환경 설정 페이지의 로그인 연동 여부 (true: SNS로그인 연동, false: SNS로그인)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("BaseLoginActivity", "onCreate");
        mContext = this;

        // facebook
        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
//                Kit.log(Kit.LogType.VALUE, "onCurrentAccessTokenChanged::oldAccessToken = " + oldAccessToken);
//                Kit.log(Kit.LogType.VALUE, "onCurrentAccessTokenChanged::newAccessToken = " + newAccessToken);
            }
        };

        // naver login
        mNaverIdLoginSDK = NaverIdLoginSDK.INSTANCE;
        mNaverIdLoginSDK.initialize(this
                , OAUTH_CLIENT_ID
                , OAUTH_CLIENT_SECRET
                , OAUTH_CLIENT_NAME);

        mNidOAuthLogin = new NidOAuthLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mKakaoSessionCallback != null)
            Session.getCurrentSession().removeCallback(mKakaoSessionCallback);

        mFacebookAccessTokenTracker.stopTracking();
    }

    protected abstract void onLoginSuccess(String loginType, String userID, String userName, String accessToken);

    protected abstract void onLoginFailed(String loginType);

    protected void facebookLogin() {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        Profile currentProfile = Profile.getCurrentProfile();
        Kit.log(Kit.LogType.VALUE, "facebookLogin::currentAccessToken: " + currentAccessToken);
        Kit.log(Kit.LogType.VALUE, "facebookLogin::currentProfile: " + currentProfile);
        if (currentAccessToken == null || currentProfile == null) {
            mFacebookCallbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
            LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(final LoginResult result) {
                    AccessToken accessToken = result.getAccessToken();
                    Kit.log(Kit.LogType.VALUE, "facebookLogin::onSuccess::accessToken: " + accessToken);

                    GraphRequest request;
                    request = GraphRequest.newMeRequest(result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                        @Override
                        public void onCompleted(JSONObject user, GraphResponse response) {
                            if (response.getError() != null) {
                                String title = response.getError().getErrorUserTitle();
                                String msg = response.getError().getErrorUserMessage();
                                Kit.showAlertDialog(BaseLoginActivity.this, title, msg, "확인");
                                onLoginFailed(PrefKit.MEMBER_TYPE_FACEBOOK);
                            } else {
                                AccessToken accessToken = result.getAccessToken();
                                Kit.log(Kit.LogType.VALUE, "facebookLoin::onCompleted::accessToken: " + accessToken);
                                Kit.log(Kit.LogType.VALUE, "facebookLoin::onCompleted::user: " + user);
                                if (accessToken == null || user == null) {
                                    Kit.showAlertDialog(BaseLoginActivity.this, "페이스북 로그인", "로그인 할 수 없습니다.", "확인");
                                    onLoginFailed(PrefKit.MEMBER_TYPE_FACEBOOK);
                                } else {
                                    setResult(RESULT_OK);

                                    //{"id":"xxxxxxxxxxxxxxxx","name":"사용자명","email":"xxx@xxx.com","gender":"male","age_range":{"min":21},"picture":{"data":{"is_silhouette":false,"url":"https:\/\/scontent.xx.fbcdn.net\/v\/t1.0-1\/c89.26.322.322\/s50x50\/260120_115001558589043_2583936_n.jpg?oh=1d9afd96eead0cb05c3ea797d419b791&oe=59DD62E0"}}}
                                    String userID = accessToken.getUserId();
                                    String token = accessToken.getToken();
                                    Kit.log(Kit.LogType.VALUE, "facebookLoin::onCompleted::userID: " + userID);
                                    Kit.log(Kit.LogType.VALUE, "facebookLoin::onCompleted::token: " + token);

                                    String name = user.optString("name");


                                    Kit.log(Kit.LogType.VALUE, "facebookLoin::onCompleted::name: " + name);

                                    onLoginSuccess(PrefKit.MEMBER_TYPE_FACEBOOK, userID, name, token);
                                }
                            }
                        }
                    });
                    Bundle parameters = new Bundle();

                    parameters.putString(GraphRequest.FIELDS_PARAM, "id,name,email,gender,age_range,picture");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("BaseLoginActivity", "facebookLogin error= " + error);
                    //Toast.makeText(MemberStartActivity.this, String.format("처리할 수 없습니다.\n(%s)", error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                    Toast.makeText(BaseLoginActivity.this, "로그인 할 수 없습니다.", Toast.LENGTH_LONG).show();
                    onLoginFailed(PrefKit.MEMBER_TYPE_FACEBOOK);
                }

                @Override
                public void onCancel() {
                    Log.e("BaseLoginActivity", "facebookLogin onCancel");
                    onLoginFailed(PrefKit.MEMBER_TYPE_FACEBOOK);
                }
            });
        } else {
            Kit.log(Kit.LogType.VALUE, "facebookLogin::currentAccessToken.getToken(): " + currentAccessToken.getToken());
            String userID = currentAccessToken.getUserId();
            String userName = currentProfile.getName();
            Uri uri = currentProfile.getProfilePictureUri(100, 100);
            onLoginSuccess(PrefKit.MEMBER_TYPE_FACEBOOK, userID, userName, currentAccessToken.getToken());
        }
    }

    protected static void facebookLogout() {
        Kit.log(Kit.LogType.EVENT, "facebookLogout");
        LoginManager.getInstance().logOut();
    }

    class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            Kit.log(Kit.LogType.EVENT, "SessionCallback::onSessionOpened");
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Kit.log(Kit.LogType.VALUE, "SessionCallback::onSessionOpenFailed::exception = " + exception);
            if (exception != null) {
                Logger.e(exception);
            }
            onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
        }
    }

    protected void requestMe() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onFailure(ErrorResult errorResult) {
                Kit.log(Kit.LogType.VALUE, "requestMe::onFailure::errorResult = " + errorResult);

                int errorCode = errorResult.getErrorCode();
                //ErrorCode errorCode = ErrorCode.valueOf(errorResult.getErrorCode());
                if (errorCode == ApiErrorCode.CLIENT_ERROR_CODE) {
                    Kit.showAlertDialog(BaseLoginActivity.this, "서비스 연결상태가 좋지 않습니다.\n\n잠시 후 다시 시도해 주세요.", "확인");
                } else {
                    Kit.showAlertDialog(BaseLoginActivity.this, String.format("카카오톡 연동 중 오류가 발생했습니다.(%d)\n\n네트워크 연결 상태를 확인 후 다시 시도 해주세요.", errorResult.getErrorMessage()), "확인");
                }
                onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            }

            @Override
            public void onSuccess(MeV2Response result) {
//                Kit.log(Kit.LogType.VALUE, "requestMe::onSuccess::result = " + result);

                long userIDValue = result.getId();
                String nickName = result.getKakaoAccount().getProfile().getNickname();
                String userID = Long.toString(userIDValue);
                Session currentSession = Session.getCurrentSession();
                String accessToken = currentSession.getTokenInfo().getAccessToken();
                String thumbnailImg = result.getKakaoAccount().getProfile().getThumbnailImageUrl();
                onLoginSuccess(PrefKit.MEMBER_TYPE_KAKAO, userID, "", accessToken);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Kit.log(Kit.LogType.VALUE, "requestMe::onSessionClosed::errorResult = " + errorResult);

                //ErrorCode errorCode = ErrorCode.valueOf(errorResult.getErrorCode());
                Kit.showAlertDialog(BaseLoginActivity.this, String.format("카카오톡 연동 중 오류가 발생했습니다.(%d)\n\n네트워크 연결 상태를 확인 후 다시 시도 해주세요.", errorResult.getErrorMessage()), "확인");
                onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            }
        });
    }

    protected void kakaoLogin() {
        UserProfile userProfile = UserProfile.loadFromCache();
        Log.e("BaseLoginActivity", "kakaoLogin : " + userProfile.getNickname());
        if (userProfile == null) {
            Kit.showAlertDialog(BaseLoginActivity.this, "카카오톡 로그인", "로그인 할 수 없습니다.", "확인");
            onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            return;
        }

        Session currentSession = Session.getCurrentSession();
        if (currentSession == null) {
            Kit.showAlertDialog(this, "카카오톡 연동 중 세션 오류가 발생했습니다. 네트워크 연결 상태를 확인해주세요.", "확인");
            onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            return;
        }

        if (mKakaoSessionCallback == null) {
            mKakaoSessionCallback = new SessionCallback();
            currentSession.addCallback(mKakaoSessionCallback);
        }

        String accessToken = currentSession.getTokenInfo().getAccessToken();
        String refreshToken = currentSession.getTokenInfo().getRefreshToken();
        Kit.log(Kit.LogType.VALUE, "kakaoLogin::accessToken = " + accessToken);
        Kit.log(Kit.LogType.VALUE, "kakaoLogin::refreshToken = " + refreshToken);
        long userIDValue = userProfile.getId();
        Kit.log(Kit.LogType.VALUE, "kakaoLogin::userIDValue = " + userIDValue);
        if (accessToken == null || accessToken.isEmpty() || userIDValue == 0) {
            boolean isCheckAndImplicitOpen = currentSession.checkAndImplicitOpen();
            Kit.log(Kit.LogType.VALUE, "kakaoLogin::isCheckAndImplicitOpen = " + isCheckAndImplicitOpen);
            if (!isCheckAndImplicitOpen) {
                Kit.log(Kit.LogType.VALUE, "kakaoLogin::currentSession.isAvailableOpenByRefreshToken() = " + currentSession.isAvailableOpenByRefreshToken());
                currentSession.open(AuthType.KAKAO_TALK, this);
            }
        } else {
            String userID = Long.toString(userIDValue);
            String nickName = userProfile.getNickname();
            onLoginSuccess(PrefKit.MEMBER_TYPE_KAKAO, userID, "", accessToken);
        }
    }

    protected static void kakaoLogout() {
        Kit.log(Kit.LogType.EVENT, "kakaoLogout");
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onCompleteLogout");
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                super.onFailure(errorResult);
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onFailure::errorResult = " + errorResult);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                super.onSessionClosed(errorResult);
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onSessionClosed::errorResult = " + errorResult);
            }

            @Override
            public void onNotSignedUp() {
                super.onNotSignedUp();
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onNotSignedUp");
            }

            @Override
            public void onSuccess(Long result) {
                super.onSuccess(result);
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onSuccess::result = " + result);
            }

            @Override
            public void onDidEnd() {
                super.onDidEnd();
                Kit.log(Kit.LogType.EVENT, "kakaoLogout::onDidEnd");
            }
        });
    }

    protected void naverLogin() {

        mNaverIdLoginSDK.authenticate(mContext, new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                String accessToken = mNaverIdLoginSDK.getAccessToken();
                String refreshToken = mNaverIdLoginSDK.getRefreshToken();
                long expiresAt = mNaverIdLoginSDK.getExpiresAt();
                String tokenType = mNaverIdLoginSDK.getTokenType();
                String State = mNaverIdLoginSDK.getState().toString();

                Log.e("BaseLoginActivity", "naverLogin::accessToken = " + accessToken);
                Log.e("BaseLoginActivity", "naverLogin::refreshToken = " + refreshToken);
                Log.e("BaseLoginActivity", "naverLogin::expiresAt = " + expiresAt);
                Log.e("BaseLoginActivity", "naverLogin::tokenType = " + tokenType);
                Log.e("BaseLoginActivity", "naverLogin::State = " + State);

                mNidOAuthLogin.callProfileApi(new NidProfileCallback<NidProfileResponse>() {
                    @Override
                    public void onSuccess(NidProfileResponse nidProfileResponse) {
                        Log.e("BaseLoginActivity", "callProfileApi component1()= " + nidProfileResponse.component1());
                        Log.e("BaseLoginActivity", "callProfileApi component2()= " + nidProfileResponse.component2());
                        Log.e("BaseLoginActivity", "callProfileApi component3()= " + nidProfileResponse.component3());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile()= " + nidProfileResponse.getProfile());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getEmail()= " + nidProfileResponse.getProfile().getEmail());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getAge()= " + nidProfileResponse.getProfile().getAge());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getId()= " + nidProfileResponse.getProfile().getId());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getNickname()= " + nidProfileResponse.getProfile().getNickname());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getMobile()= " + nidProfileResponse.getProfile().getMobile());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getName()= " + nidProfileResponse.getProfile().getName());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getGender()= " + nidProfileResponse.getProfile().getGender());
                        Log.e("BaseLoginActivity", "callProfileApi getMessage()= " + nidProfileResponse.getMessage());
                        Log.e("BaseLoginActivity", "callProfileApi getResultCode()= " + nidProfileResponse.getResultCode());
                        onLoginSuccess(PrefKit.MEMBER_TYPE_NAVER, nidProfileResponse.getProfile().getId(), nidProfileResponse.getProfile().getName(), accessToken);
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                        onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                        naverLogout(mContext);
                    }

                    @Override
                    public void onError(int i, @NonNull String s) {
                        onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                        naverLogout(mContext);
                    }
                });
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.e("BaseLoginActivity", "naverLogin::onFailure = " + i);
                Log.e("BaseLoginActivity", "naverLogin::onFailure = " + s);
                onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                naverLogout(mContext);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.e("BaseLoginActivity", "naverLogin::onError = " + i);
                Log.e("BaseLoginActivity", "naverLogin::onError = " + s);
                onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                naverLogout(mContext);
            }
        });
    }

    protected static void naverLogout(Context context) {
        Kit.log(Kit.LogType.EVENT, "naverLogout");
        mNaverIdLoginSDK.logout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Kit.log(Kit.LogType.EVENT, "onActivityResult::");
        Kit.log(Kit.LogType.VALUE, "onActivityResult::requestCode = " + requestCode);
        Kit.log(Kit.LogType.VALUE, "onActivityResult::resultCode = " + resultCode);

        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            //Kit.log(Kit.LogType.VALUE, "onActivityResult::Session.getCurrentSession().handleActivityResult");
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (mFacebookCallbackManager != null)
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private static String getAccessToken(final Context context) {
        String accessToken = "";
        String memberType = PrefKit.getMemberType(context);
        if (memberType.equals(PrefKit.MEMBER_TYPE_FACEBOOK)) {
            AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
            accessToken = currentAccessToken.getToken();
        } else if (memberType.equals(PrefKit.MEMBER_TYPE_KAKAO)) {
            Session currentSession = Session.getCurrentSession();
            accessToken = currentSession.getAccessToken();
        } else if (memberType.equals(PrefKit.MEMBER_TYPE_NAVER)) {
            mNaverIdLoginSDK.getRefreshToken();
            accessToken = mNaverIdLoginSDK.getAccessToken();
        }

//        Kit.log(Kit.LogType.TEST, "getAccessToken::accessToken = " + accessToken);
        if (accessToken == null)
            accessToken = "";

        return accessToken;
    }

    protected String getLoginName(String loginType) {
        String loginName = "";
        if (loginType.equals(PrefKit.MEMBER_TYPE_FACEBOOK)) {
            loginName = "페이스북";
        } else if (loginType.equals(PrefKit.MEMBER_TYPE_KAKAO)) {
            loginName = "카카오";
        } else if (loginType.equals(PrefKit.MEMBER_TYPE_NAVER)) {
            loginName = "네이버";
        }

        return loginName;
    }
}
