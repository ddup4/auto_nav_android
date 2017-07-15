package com.ddup4.autonav.module.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ddup4.autonav.R;
import com.ddup4.autonav.app.BaseFragment;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.util.IOUtil;

/**
 * Created by idonans on 2017/2/3.
 */

public class MainFragment extends BaseFragment<MainViewProxy> implements MainView {

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected MainViewProxy newDefaultViewProxy() {
        return new MainViewProxy(this);
    }

    private Content mContent;

    @Override
    protected void showInitSuccessContentView(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView, @NonNull DynamicViewData dynamicViewData) {
        IOUtil.closeQuietly(mContent);
        mContent = new Content(activity, inflater, contentView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IOUtil.closeQuietly(mContent);
        mContent = null;
    }

    @Override
    public void onUpdateContentViewIfChanged() {
    }

    private class Content extends ContentViewHelper {

        private Content(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView) {
            super(activity, inflater, contentView, R.layout.ddup4_autonav_module_main_view);
        }

    }

}
