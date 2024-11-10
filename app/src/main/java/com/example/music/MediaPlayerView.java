package com.example.music;

import android.annotation.SuppressLint;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.example.test.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

public class MediaPlayerView {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PARTIAL = 1;

    private final View mRootView;

    private int mState;

    private MediaPlayerBottomView mMediaPlayer_BottomView;
    private ConstraintLayout mControlsContainer;

    private CardView m_vCardView_Art;

    private TextView m_vTextView_Title;
    private TextView m_vTextView_Artist;

    private SeekBar m_vSeekBar_Main;

    private TextView m_vTextView_CurrentDuration;
    private TextView m_vTextView_MaxDuration;

    private ExtendedFloatingActionButton m_vBtn_Repeat;
    private ExtendedFloatingActionButton m_vBtn_Prev;
    private ExtendedFloatingActionButton m_vBtn_Next;
    private FloatingActionButton m_vBtn_PlayPause;
    private ExtendedFloatingActionButton m_vBtn_Shuffle;

    private boolean m_vCanUpdateSeekbar = true;

    public MediaPlayerView(View rootView, FragmentManager fragmentManager, Lifecycle lifecycle) {
        this.mRootView = rootView;

        this.mRootView.setAlpha(0.0F);

        this.m_vCardView_Art = this.mControlsContainer.findViewById(R.id.card_view_artist_art_container);
        this.m_vTextView_Title = this.mControlsContainer.findViewById(R.id.text_view_song_title);
        this.m_vTextView_Artist = this.mControlsContainer.findViewById(R.id.text_view_song_artist);

        this.m_vSeekBar_Main = this.mControlsContainer.findViewById(R.id.seek_bar_main);

    }
}