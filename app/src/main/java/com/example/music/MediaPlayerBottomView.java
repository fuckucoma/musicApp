package com.example.music;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.example.test.R;


public class MediaPlayerBottomView {
    private View m_vRootView;

    private ViewPager2 m_vViewPager;

    public MediaPlayerBottomView(View rootView, FragmentManager fragmentManager, Lifecycle lifecycle) {
        this.m_vRootView = rootView;

    }


    public <T extends View> T findViewById(@IdRes int id) {
        return this.m_vRootView.findViewById(id);
    }
}
