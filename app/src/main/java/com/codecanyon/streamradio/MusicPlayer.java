package com.codecanyon.streamradio;

import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import com.codecanyon.radio.R;
import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

/**
 * Created by User on 2014.07.03..
 */
public class MusicPlayer {
    public static boolean isStarted = false;
    private static String trackTitle = "";
    private static String radioName = "";
    private static Context context;
    private RadioListElement radioListElement;
    private Timer timer = new Timer();
    private boolean timerIndicator = false;

    public static boolean isWorking() {
        return isWorking;
    }

    public static void setIsWorking(boolean isWorking) {
        MusicPlayer.isWorking = isWorking;
    }

    private static boolean isWorking = true;

    public static String getRadioName() {
        return radioName;
    }

    public static String getTrackTitle() {
        return trackTitle;
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public static void stopMediaPlayer() {
        isStarted = false;
        Intent stopIntent = new Intent(context, ForegroundService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_PLAYER);
        context.startService(stopIntent);
    }

    public void play(RadioListElement rle) {
        startThread();
        TelephonyManagerReceiver.message = false;
        isWorking = true;
        isStarted = true;
        radioListElement = rle;
        context = radioListElement.getContext();
        MainActivity.setViewPagerSwitch();
        Intent startIntent = new Intent(context, ForegroundService.class);
        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        context.startService(startIntent);
        radioListElement.getName();
        radioName = radioListElement.getName();
    }
    public void startThread() {
        if (!timerIndicator) {
            timerIndicator = true;
            timer.schedule(new TimerTask() {
                public void run() {
                    if (isStarted) {
                        URL url;
                        try {
                            url = new URL(radioListElement.getUrl());
                            IcyStreamMeta icy = new IcyStreamMeta(url);
                            if (icy.getArtist().length() > 0 && icy.getTitle().length() > 0) {
                                String title = icy.getArtist() + " - " + icy.getTitle();
                                trackTitle = new String(title.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            } else {
                                String title = icy.getArtist() + "" + icy.getTitle();
                                trackTitle = new String(title.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 1000);
        }
    }
}
