package com.messeesang.kanajimo.kit;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ShareKit {

    public static void shareKakao(final Context context,
                                  String shareContent, String shareImageUrl, String shareLink1, String execParams1,
                                  String buttunTitle, String shareLink2, String buttonTitle2) {
        try {
            FeedTemplate params = FeedTemplate.newBuilder(
                    ContentObject.newBuilder(
                            "",
                            shareImageUrl,
                            com.kakao.message.template.LinkObject.newBuilder()
                                    .setWebUrl(shareLink1)
                                    .setMobileWebUrl(shareLink1)
                                    .build())
                            .setDescrption(shareContent)
                            .build())
                    .addButton(new ButtonObject(buttunTitle, com.kakao.message.template.LinkObject.newBuilder()
                            .setWebUrl(shareLink1)
                            .setMobileWebUrl(shareLink1)
                            .setAndroidExecutionParams(execParams1)
                            .setIosExecutionParams(execParams1)
                            .build()))
                    .addButton(new ButtonObject(buttonTitle2, com.kakao.message.template.LinkObject.newBuilder()
                            .setWebUrl(shareLink2)
                            .setMobileWebUrl(shareLink2)
                            .setAndroidExecutionParams("dummy=")
                            .setIosExecutionParams("dummy=")
                            .build()))
                    .build();

            KakaoLinkService.getInstance().sendDefault(context, params, new ResponseCallback<KakaoLinkResponse>() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    Logger.e(errorResult.toString());
                    Toast.makeText(context, errorResult.getErrorMessage().toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(KakaoLinkResponse result) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "카카오링크를 실행 할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareLINE(Context context, String shareContent) {
        String msg = shareContent;
        try {
            msg = URLEncoder.encode(msg, "utf-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format("line://msg/text/%s", msg)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            // LINE 설치
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format("market://details?id=%s", "jp.naver.line.android")));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareSMS(Context context, String shareContent) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("sms_body", shareContent); // 보낼 문자
            intent.setType("vnd.android-dir/mms-sms");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyClipboard(Context context, String content) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label", content);
            clipboardManager.setPrimaryClip(clipData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
