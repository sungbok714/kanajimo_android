package com.messeesang.kanajimo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.messeesang.kanajimo.R;
import com.messeesang.kanajimo.data.Extra;
import com.messeesang.kanajimo.kit.PrefKit;

public class AgreePushNotificationActivity extends BaseActivity implements View.OnClickListener {
    private boolean mIsSaveAgreement = false;       // 동의 여부만 저장할지, false면 알림받기 터치시 설정 페이지로 이동
    private Button txtCancel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree_push_notification);

        txtCancel = findViewById(R.id.txtCancel);

        findViewById(R.id.txtAgree).setOnClickListener(this);
        txtCancel.setOnClickListener(this);

        mIsSaveAgreement = getIntent().getBooleanExtra(Extra.KEY_SAVE_AGREEMENT, false);
        if (mIsSaveAgreement) {
            txtCancel.setText("취소");
        } else {
            txtCancel.setText("일주일간 보지 않기");
//            PrefKit.setOpenPushAgreeTime(this, System.currentTimeMillis());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.txtAgree: {
                if (mIsSaveAgreement) {
                    PrefKit.setAgreePushNoti(this, true);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Extra.KEY_ALLOW_PUSH_NOTIFICATION, true);

                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            }
            case R.id.txtCancel: {
                if (mIsSaveAgreement == false) {
                    PrefKit.setDoNotShowWeekTime(this, System.currentTimeMillis());
                }
                finish();
                break;
            }
            default:
                break;
        }
    }
}
