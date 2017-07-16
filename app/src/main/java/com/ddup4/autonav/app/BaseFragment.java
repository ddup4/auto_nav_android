package com.ddup4.autonav.app;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import com.okandroid.boot.app.ext.dynamic.DynamicFragment;
import com.okandroid.boot.util.AvailableUtil;

/**
 * Created by idonans on 2017/7/15.
 */

public abstract class BaseFragment<T extends BaseViewProxy> extends DynamicFragment implements BaseView {

    @Nullable
    @Override
    public T getDefaultViewProxy() {
        return (T) super.getDefaultViewProxy();
    }

    @Override
    protected abstract T newDefaultViewProxy();

    @CheckResult
    public Activity getAvailableActivity() {
        Activity activity = getActivity();

        if (AvailableUtil.isAvailable(activity)) {
            return activity;
        }

        return null;
    }

    public void finishActivity() {
        Activity activity = getAvailableActivity();
        if (activity == null) {
            return;
        }

        activity.finish();
    }

}
