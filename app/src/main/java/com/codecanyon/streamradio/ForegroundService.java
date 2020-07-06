package com.codecanyon.streamradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.codecanyon.radio.R;
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
import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

public class ForegroundService extends Service {

    private SimpleExoPlayer player;
    private MultiPlayer multiPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            play(this.getString(R.string.radio_url));
            showNotification(true);
        } else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_PLAYER)){
            player.stop();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopMediaPlayer();
            closeNotification();
            stopForeground(true);
            stopSelf();
        }
        }catch (Exception e){
            e.getMessage();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void play(final String url){
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new Handler(), new AdaptiveVideoTrackSelection.Factory(bandwidthMeter));
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(), this.getString(R.string.item_purchase_code));
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "streamradio"), bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource audioSource = new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
        player.prepare(audioSource);
        player.setPlayWhenReady(true);
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                if(isLoading){
                    MainActivity.startBufferingAnimation();
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState==3){
                    MainActivity.stopBufferingAnimation();
                    MusicPlayer.isStarted = true;
                }else{
                    MusicPlayer.isStarted = false;
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object o) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        try {
                            multiPlayer.stop();
                        } catch (Exception e) {
                            e.getMessage();
                        }
                        MainActivity.newNotification(MusicPlayer.getRadioName(), true);
                        multiPlayer = new MultiPlayer(new PlayerCallback() {

                            @Override
                            public void playerStopped(int arg0) {
                                MusicPlayer.isStarted = false;
                            }

                            @Override
                            public void playerStarted() {
                                MusicPlayer.isStarted = true;
                                try {
                                    MainActivity.stopBufferingAnimation();
                                } catch (Exception e) {
                                    e.getMessage();
                                }
                            }

                            @Override
                            public void playerPCMFeedBuffer(boolean arg0, int arg1, int arg2) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void playerMetadata(String arg0, String arg1) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void playerException(Throwable arg0) {
                                // TODO Auto-generated method stub
                                MusicPlayer.setIsWorking(false);
                                try {
                                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                                        MainActivity.stopBufferingAnimation();
                                        MusicPlayer.setIsWorking(false);

                                    } else {
                                        MainActivity.stopBufferingAnimation();
                                        MusicPlayer.setIsWorking(false);
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }

                            }

                            @Override
                            public void playerAudioTrackCreated(AudioTrack arg0) {
                                // TODO Auto-generated method stub

                            }
                        }, 750, 700);
                        multiPlayer.playAsync(url);

                        try {
                            java.net.URL.setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
                                public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
                                    if ("icy".equals(protocol))
                                        return new com.spoledge.aacdecoder.IcyURLStreamHandler();
                                    return new com.spoledge.aacdecoder.IcyURLStreamHandler();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        MusicPlayer.setIsWorking(false);
                        try {
                            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo netInfo = cm.getActiveNetworkInfo();
                            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                                MainActivity.stopBufferingAnimation();
                                MusicPlayer.setIsWorking(false);

                            } else {
                                MainActivity.stopBufferingAnimation();
                                MusicPlayer.setIsWorking(false);
                            }
                        } catch (Exception e2) {
                            // TODO: handle exception
                        }
                    }
                } else {
                    MusicPlayer.setIsWorking(false);
                    try {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                            MainActivity.stopBufferingAnimation();
                            MusicPlayer.setIsWorking(false);
                        } else {
                            MainActivity.stopBufferingAnimation();
                            MusicPlayer.setIsWorking(false);
                        }
                    } catch (Exception e2) {
                        // TODO: handle exception
                    }
                }
            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
    }


    public void showNotification(boolean autoOpen) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(this.getString(R.string.radio_name))
                .setContentText(MainScreen.getRadioListName().getText().toString())
                .setSmallIcon(R.drawable.ic_stat_transmission4)
                .setContentIntent(pendingIntent)
                .setWhen(0)
                .setOngoing(true);

        nBuilder.setContentIntent(pendingIntent);
        Notification noti = nBuilder.build();
        noti.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, noti); //notification id
    }

    public void closeNotification(){
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
    }

    public void stopMediaPlayer() {
        MusicPlayer.isStarted = false;
        try {
            player.stop();
        }catch (Exception e){
            e.getMessage();
        }
        try {
            multiPlayer.stop();
        }catch (Exception e){
            e.getMessage();
        }
    }
}