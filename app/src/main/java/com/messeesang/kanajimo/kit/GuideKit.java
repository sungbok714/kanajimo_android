package com.messeesang.kanajimo.kit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class GuideKit {
    public View mTutorialView = null;
    private ViewGroup mParent = null;

    public GuideKit(Context context, ViewGroup parent) {
        mParent = parent;
    }

    public void showWithView(View view) {
        if (mTutorialView != null)
            hide();

        mTutorialView = view;
        mParent.addView(mTutorialView);
    }

    public void hide() {
        if (mTutorialView != null) {
            mParent.removeView(mTutorialView);
        }
        mTutorialView = null;
    }
}
