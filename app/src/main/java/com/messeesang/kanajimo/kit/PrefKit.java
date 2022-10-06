package com.messeesang.kanajimo.kit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefKit {

    public static final String PREF_IS_TEST_MODE = "is_test_mode";
    public static final String PREF_USER_INFO = "user_info";
    public static final String PREF_IS_VIEW_TUTORIAL = "is_view_tutorial";
    public static final String SECRET_SHARED_PREF = "secret_shared_prefs";
    public static final String PREF_NOTI_ON_OFF = "noti_on_off";

    public static final String MEMBER_TYPE_FACEBOOK = "facebook";
    public static final String MEMBER_TYPE_KAKAO = "kakao";
    public static final String MEMBER_TYPE_NAVER = "naver";
    public static final String PREF_MEMBER_TYPE = "member_type";
    public static final String PREF_NAVER_ID = "naver_id";
    public static final String PREF_NAVER_NICKNAME = "naver_nickname";

    public static final String PREF_MEMBER_IDX = "member_idx";
    public static final String PREF_FIRST_EXEC = "first_exec";
    public static final String PREF_EXEC_COUNT_FOR_PUSH_AGREE = "EXEC_count_for_push_agree";
    public static final String PREF_AGREE_PUSH_NOTI = "agree_push_noti";
    public static final String PREF_DONOT_SHOW_A_WEEK = "donot_show_a_week";
    public static final String PREF_OPEN_PUSH_AGREE_TIME = "open_push_agree_time";
    public static final String PREF_IS_DEV_MODE = "is_dev_mode";
    public static final String PREF_FIRST_PERMISSION = "first_permission";
    public static final String PREF_USER_ADID = "user_adid";

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public String getDefaultPreferenceString(Context context, String key, String defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getString(key, defaultValue);
    }

    static public void setDefaultPreferenceString(Context context, String key, String value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putString(key, value);
        editor.commit();
    }

    static public boolean getDefaultPreferenceBoolean(Context context, String key, boolean defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getBoolean(key, defaultValue);
    }

    static public void setDefaultPreferenceBoolean(Context context, String key, boolean value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    static public int getDefaultPreferenceInt(Context context, String key, int defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getInt(key, defaultValue);
    }

    static public void setDefaultPreferenceInt(Context context, String key, int value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    static public long getDefaultPreferenceLong(Context context, String key, long defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getLong(key, defaultValue);
    }

    static public void setDefaultPreferenceLong(Context context, String key, long value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putLong(key, value);
        editor.commit();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean getTestMode(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_IS_TEST_MODE, false);
    }

    static public void setTestMode(Context context, boolean isTestMode) {
        setDefaultPreferenceBoolean(context, PREF_IS_TEST_MODE, isTestMode);
    }

//    static public UserInfo getUserInfo(Context context) {
//        String json = getDefaultPreferenceString(context, PREF_USER_INFO, "");
//        return new Gson().fromJson(json, UserInfo.class);
//    }
//
//    static public void setUserInfo(Context context, UserInfo userInfo) {
//        setDefaultPreferenceString(context, PREF_USER_INFO, new Gson().toJson(userInfo));
//    }

    static public boolean getIsViewTutorial(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_IS_VIEW_TUTORIAL, true);
    }

    static public void setIsViewTutorial(Context context, boolean isFirst) {
        setDefaultPreferenceBoolean(context, PREF_IS_VIEW_TUTORIAL, isFirst);
    }

    static public boolean getNoitOnOff(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_NOTI_ON_OFF, false);
    }

    static public void setNoitOnOff(Context context, boolean isFirst) {
        setDefaultPreferenceBoolean(context, PREF_NOTI_ON_OFF, isFirst);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public String getNaverID(Context context) {
        return getDefaultPreferenceString(context, PREF_NAVER_ID, "");
    }

    static public void setNaverID(Context context, String id) {
        setDefaultPreferenceString(context, PREF_NAVER_ID, id);
    }

    static public String getNaverNickName(Context context) {
        return getDefaultPreferenceString(context, PREF_NAVER_NICKNAME, "");
    }

    static public void setNaverNickName(Context context, String id) {
        setDefaultPreferenceString(context, PREF_NAVER_NICKNAME, id);
    }

    static public String getMemberType(Context context) {
        return getDefaultPreferenceString(context, PREF_MEMBER_TYPE, "");
    }

    static public void setMemberType(Context context, String memType) {
        setDefaultPreferenceString(context, PREF_MEMBER_TYPE, memType);
    }

    static public String getMemberIdx(Context context) {
        return getDefaultPreferenceString(context, PREF_MEMBER_IDX, "");
    }

    static public void setMemberIdx(Context context, String mem_idx) {
        setDefaultPreferenceString(context, PREF_MEMBER_IDX, mem_idx);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 첫 실행 여부 (MainActivity 진입 전)
    static public boolean getFirstExec(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_FIRST_EXEC, true);
    }

    static public void setFirstExec(Context context, boolean firstExec) {
        setDefaultPreferenceBoolean(context, PREF_FIRST_EXEC, firstExec);
    }

    static public boolean getFirstPermission(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_FIRST_PERMISSION, true);
    }

    static public void setFirstPermission(Context context, boolean firstPermission) {
        setDefaultPreferenceBoolean(context, PREF_FIRST_PERMISSION, firstPermission);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 첫 실행 여부 (MainActivity 진입 후)
    static public long getExecCountForPushAgree(Context context) {
        return getDefaultPreferenceLong(context, PREF_EXEC_COUNT_FOR_PUSH_AGREE, 0);
    }

    static public void increaseExecCountForPushAgree(Context context) {
        long count = getExecCountForPushAgree(context);
        setDefaultPreferenceLong(context, PREF_EXEC_COUNT_FOR_PUSH_AGREE, ++count);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 푸시 알림 동의 여부
    static public boolean getAgreePushNoti(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_AGREE_PUSH_NOTI, false);
    }

    static public void setAgreePushNoti(Context context, boolean agree) {
        setDefaultPreferenceBoolean(context, PREF_AGREE_PUSH_NOTI, agree);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 일주일간 보지 않기 (시간 저장)
    static public long getDoNotShowWeekTime(Context context) {
        return getDefaultPreferenceLong(context, PREF_DONOT_SHOW_A_WEEK, 0);
    }

    static public void setDoNotShowWeekTime(Context context, long time) {
        setDefaultPreferenceLong(context, PREF_DONOT_SHOW_A_WEEK, time);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 푸시알림 동의 화면을 띄운 시간 (로그인 후 MainActivity에서)
    static public long getOpenPushAgreeTime(Context context) {
        return getDefaultPreferenceLong(context, PREF_OPEN_PUSH_AGREE_TIME, 0);
    }

    static public void setOpenPushAgreeTime(Context context, long time) {
        setDefaultPreferenceLong(context, PREF_OPEN_PUSH_AGREE_TIME, time);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean getDevMode(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_IS_DEV_MODE, false);
    }

    static public void setDevMode(Context context, boolean isDevMode) {
        setDefaultPreferenceBoolean(context, PREF_IS_DEV_MODE, isDevMode);
    }

    static public String getUserAdId(Context context) {
        return getDefaultPreferenceString(context, PREF_USER_ADID, "");
    }

    static public void setUserAdId(Context context, String adid) {
        setDefaultPreferenceString(context, PREF_USER_ADID, adid);
    }
}
