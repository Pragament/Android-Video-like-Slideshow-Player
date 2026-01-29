package com.technikh.evideos.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PictureDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.danikula.videocache.HttpProxyCacheServer;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomLayout;
import com.technikh.evideos.Animations.TextAnimations;
import com.technikh.evideos.R;
import com.technikh.evideos.Thread.MyHandler;
import com.technikh.evideos.app.MyApplication;
import com.technikh.evideos.models.slideshow.Backgrounds;
import com.technikh.evideos.models.slideshow.Lines;
import com.technikh.evideos.models.slideshow.Slides;
import com.technikh.evideos.models.slideshow.SlideshowJsonModel;
import com.technikh.evideos.models.slideshow.lineMedia;
import com.technikh.evideos.preferences.SlideshowSharedPreferences;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SlideShowActivity extends AppCompatActivity implements MediaController.MediaPlayerControl, TextToSpeech.OnInitListener {

    private ImageView Pause, NextButton, PreviousButton, replayButton, volume_mute_toggle;

    private static String TAG = "SlideShowActivity";
    private BubbleSeekBar speedSeekBar;
    private LinearLayout speedSeekBarWrapper;
    private boolean started = false;
    private boolean startedImage = false;
    private SlideshowJsonModel data;
    private int Counter = 0;
    private int CounterImage = 0;
    private int CounterImages = 0;
    private Handler handler = new Handler();
    private Handler handlerImage = new Handler();
    private Handler handlerImageBackground = new Handler();
    private RelativeLayout relative;
    private KenBurnsView background_one;
    private String background_one_current_url = "";
    private ProgressDialog dialog;
    private TextView[] tv;
    private TextView tv_credits;
    private ImageView[] ImageArray;
    private TextView[] TextViewArray, ImageAttributesTVarray;
    private int NextCount = 0, pausedAtSlideNum = 0;
    private int NextCountForBackground = 0;
    private Object[] objectsArray;
    private Handler handlerImageForImage = new Handler();
    private boolean isFirstTime = true;
    private int imgVal = 0;
    private ArrayList<lineMedia> imagesList;
    private LinearLayout rl;
    private VideoView video;
    private int backgroundCount = 0;
    private boolean isPause = false;
    MyHandler myHandler = new MyHandler();
    private int myind = 0, index = 0, count = 0;
    private int ImagesCounter = 0;
    private int ImagesCounterForAnim = 0;
    private int TextCounter = 0;
    private boolean isButtonPause = false, isButtonMute = false;
    private int handlerImagesCounter = 0;
    private int CountForResumeAddText = 0;

    private int LoopFirstCount = 0;
    private int LoopSecondCount = 0;
    private Handler TimerHandler = new Handler();
    private int TimerFirst = 0;
    private int TimerSecond = 0;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;

    private MediaPlayer mp;
    private String currentMusicMood = "";

    int speedMilliSeconds = 0;

    int ImageCounterValue = 0;
    int TmeDuration = 0;
    YoYo.YoYoString text;
    YoYo.YoYoString image;
    YoYo.YoYoString caption;
    YoYo.YoYoString imageCopyright;
    YoYo.YoYoString Exittext;
    YoYo.YoYoString Exitimage;
    private boolean LoopBrakFromText = false;
    private RequestBuilder<PictureDrawable> requestBuilder;

    HttpProxyCacheServer cacheProxy;
    TextToSpeech tts;
    boolean isTextToSpeechReady = false;
    HashMap<String, Integer> lineUuidMap = new HashMap<>();
    int numOfLinesInCurrentSlide = 0;
    int waitTimeinMillSecBeforeNextSlide = 2000;

    int currentSlideShowSummaryDialogQuestionIndex = 0;
    Boolean SlideShowSummaryDialogFinishMode = false;
    HashMap<Integer, Integer> selectedDialogOption = new HashMap<>();

    float maxVolume = 0.3f;
    float volume = maxVolume;

    private FirebaseAnalytics mFirebaseAnalytics;

    private final Runnable hideSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            speedSeekBarWrapper.setVisibility(View.INVISIBLE);
            setFullScreen();
        }
    };

    @Override
    public void onBackPressed() {
        startFadeOutFlex(1000, 100, true);

        Bundle fbundle = new Bundle();
        fbundle.putString(FirebaseAnalytics.Param.LEVEL, String.valueOf(NextCount));
        mFirebaseAnalytics.logEvent("BUTTON_BACK", fbundle);
        super.onBackPressed();
    }

    @Override
    public int getAudioSessionId() {
        Log.d("TAG", "getAudioSessionId: " + mp.getAudioSessionId());
        return mp.getAudioSessionId();
    }

    @Override
    public boolean canSeekBackward() {
        Boolean status = NextCount >= 1;
        Log.d("ttsseek", "canSeekBackward: NextCount " + NextCount + " status " + status);
        return status;
    }

    @Override
    public boolean canSeekForward() {
        Boolean status = data.getSlides().size() > 1 && NextCount < (data.getSlides().size() - 1);
        Log.d("ttsseek", "canSeekForward: NextCount " + NextCount + " status " + status);
        return status;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 100;
    }

    @Override
    public boolean isPlaying() {
        return !isButtonPause;
    }

    @Override
    public void pause() {
        pauseTTS();
    }

    @Override
    public int getDuration() {
        return data.getSlides().size() * 4000;
    }

    @Override
    public int getCurrentPosition() {
        return NextCount * 4000;
    }

    @Override
    public void start() {
        pause();
    }

    @Override
    public void seekTo(@IntRange(from = 0, to = Integer.MAX_VALUE) final int position) {
        Bundle fbundle = new Bundle();
        fbundle.putString(FirebaseAnalytics.Param.SOURCE, String.valueOf(NextCount));
        Log.d("0tsseek", "1seekTo: position " + position + " NextCount " + NextCount);
        if (position < 0) {
            throw new IllegalArgumentException("Position is not positive");
        }
        int slideNum = (int) position / 4000;
        Log.d("ttsseek", "1seekTo: position " + position + " slide " + slideNum + " NextCount " + NextCount);
        if (slideNum == NextCount) {
            if (position / 4000 > NextCount && (NextCount < (data.getSlides().size() - 1))) {
                NextCount++;
            } else if (position / 4000 < NextCount && NextCount >= 1) {
                NextCount--;
            }
        } else if (slideNum >= 0 && (slideNum <= (data.getSlides().size() - 1))) {
            NextCount = slideNum;
        }
        Log.d("ttsseek", "2seekTo: position " + position + " slide " + slideNum + " NextCount " + NextCount);

        fbundle.putString(FirebaseAnalytics.Param.DESTINATION, String.valueOf(NextCount));
        mFirebaseAnalytics.logEvent("VIDEO_SEEK", fbundle);
        tts.stop();
        loadViewsFromData();
        playSlideTextToSpeech();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public void pauseTTS() {
        Bundle fbundle = new Bundle();
        fbundle.putString(FirebaseAnalytics.Param.LEVEL, String.valueOf(NextCount));
        if (!isButtonPause) {
            mp.pause();
            tts.stop();
            background_one.pause();
            isButtonPause = true;
            pausedAtSlideNum = NextCount;
            mFirebaseAnalytics.logEvent("VIDEO_PAUSE", fbundle);
        } else {
            mp.start();
            background_one.resume();
            if (pausedAtSlideNum != NextCount) {
                loadViewsFromData();
            }
            playSlideTextToSpeech();
            isButtonPause = false;
            mFirebaseAnalytics.logEvent("VIDEO_RESUME", fbundle);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {

                    boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
                    Log.i("XXX", "was onDone() called on a background thread? : " + wasCalledFromBackgroundThread);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int currentLineIndex = getLineIndexFromUUID(utteranceId);
                            int nextLineIndex = currentLineIndex + 1;
                            Slides currentSlide = data.getSlides().get(NextCount);

                            try {
                                Lines currentLine = currentSlide.getLines().get(currentLineIndex);
                                hideLineImagesView(currentLineIndex, currentLine);

                                if (nextLineIndex < numOfLinesInCurrentSlide) {
                                    Lines nextLine = currentSlide.getLines().get(nextLineIndex);
                                    showLineTextView(nextLineIndex, nextLine.getEffects().getEnter());
                                    showLineImagesView(nextLineIndex, nextLine);
                                }

                                if (nextLineIndex == (numOfLinesInCurrentSlide)) {
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (NextCount < (data.getSlides().size() - 1)) {
                                                NextCount++;
                                                if (!data.getSlides().get(NextCount).getMusic_Mood().equals(currentMusicMood)) {
                                                    startFadeOut(false);
                                                }
                                                loadViewsFromData();
                                                playSlideTextToSpeech();
                                            } else {
                                                if (data.getShSummary() != null) {
                                                    showSlideShowSummaryDialog();
                                                } else {
                                                    startFadeOut(true);
                                                    tts.shutdown();
                                                    finish();
                                                }
                                            }
                                        }
                                    }, waitTimeinMillSecBeforeNextSlide);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onError(String utteranceId) {
                }
            });

            String currentlanguage = data.getLanguage();
            int result = tts.setLanguage(new Locale(currentlanguage));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                isTextToSpeechReady = true;
                playSlideTextToSpeech();
            }
        } else
            Log.e("error", "Initilization Failed!");
    }

    private void setFullScreen() {
        Log.d("qse", "setFullScreen: ");
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flags);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Set Fullscreen and Content View
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_testing_new_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 2. ✅ CREATE ATTRIBUTION CONTEXT (Fixes Android 14 Audio Hardening)
        Context attributionContext;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Note: "audioPlayback" must be declared in your AndroidManifest.xml
            attributionContext = createAttributionContext("audioPlayback");
        } else {
            attributionContext = getApplicationContext();
        }

        // 3. Initialize Analytics and FullScreen
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setFullScreen();

        // 4. ✅ INITIALIZE TTS WITH ATTRIBUTION CONTEXT
        // Passing 'attributionContext' instead of 'this' prevents background muting
        tts = new TextToSpeech(attributionContext, this);

        // 5. UI Bindings
        relative = findViewById(R.id.ChildRelative);
        tv_credits = findViewById(R.id.tv_credits);
        background_one = findViewById(R.id.background_one);
        rl = findViewById(R.id.firstLayout);
        video = findViewById(R.id.vv);
        Pause = findViewById(R.id.Pause);
        volume_mute_toggle = findViewById(R.id.volume_mute_toggle);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        speedSeekBarWrapper = findViewById(R.id.speedSeekBarWrapper);
        NextButton = findViewById(R.id.Next);
        replayButton = findViewById(R.id.replay);
        PreviousButton = findViewById(R.id.Previous);

        // 6. ✅ UPDATED ZoomLayout (Otaliastudios Namespace)
        com.otaliastudios.zoom.ZoomLayout mZoomLayout = findViewById(R.id.zoomLayout);

        // Set Zoom Constraints
        mZoomLayout.getEngine().setMinZoom(1.0f, com.otaliastudios.zoom.ZoomApi.TYPE_ZOOM);
        mZoomLayout.getEngine().setMaxZoom(3.0f, com.otaliastudios.zoom.ZoomApi.TYPE_ZOOM);

        // 7. Media Controller Setup
        final MediaController mc = new MediaController(this);
        mc.setMediaPlayer(this);
        mc.setEnabled(true);
        mc.setAnchorView(background_one);

        // Tap to Show/Hide Controls
        mZoomLayout.setOnClickListener(v -> {
            Bundle fbundle = new Bundle();
            fbundle.putString(FirebaseAnalytics.Param.LEVEL, String.valueOf(NextCount));

            if (mc.isShowing()) {
                mFirebaseAnalytics.logEvent("VIDEO_CONTROLS_HIDE", fbundle);
                mc.hide();
                speedSeekBarWrapper.setVisibility(View.INVISIBLE);
                setFullScreen();
            } else {
                mFirebaseAnalytics.logEvent("VIDEO_CONTROLS_SHOW", fbundle);
                mc.show(5000);
                speedSeekBarWrapper.setVisibility(View.VISIBLE);
                speedSeekBarWrapper.postDelayed(hideSeekBarRunnable, 5000);
            }
        });

        // Zoom Engine Listener
        mZoomLayout.getEngine().addListener(new com.otaliastudios.zoom.ZoomEngine.Listener() {
            @Override
            public void onUpdate(@NonNull com.otaliastudios.zoom.ZoomEngine engine, @NonNull android.graphics.Matrix matrix) {
                // Logic for zoom updates if needed
            }

            @Override
            public void onIdle(@NonNull com.otaliastudios.zoom.ZoomEngine engine) {
                Bundle fbundle = new Bundle();
                fbundle.putString(FirebaseAnalytics.Param.LEVEL, String.valueOf(NextCount));
                mFirebaseAnalytics.logEvent("VIDEO_INTERACT_ZOOM_PAN_IDLE", fbundle);
            }
        });

        // 8. Video and Audio Logic
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startFadeIn();
                video.setVisibility(View.GONE);
            }
        });

        speedSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                Bundle fbundle = new Bundle();
                fbundle.putString(FirebaseAnalytics.Param.LEVEL, String.valueOf(NextCount));
                fbundle.putString("speed_rate", String.valueOf(progressFloat / 200));
                mFirebaseAnalytics.logEvent("VIDEO_SPEED", fbundle);

                tts.setSpeechRate(progressFloat / 200);
                speedSeekBarWrapper.removeCallbacks(hideSeekBarRunnable);
                speedSeekBarWrapper.postDelayed(hideSeekBarRunnable, 3000);
            }
        });

        volume_mute_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mc.show(5000);
                if (!isButtonMute) {
                    isButtonMute = true;
                    if (mp != null) mp.pause(); // Added null check for safety
                    volume_mute_toggle.setImageResource(R.drawable.ic_mute_icon);
                } else {
                    isButtonMute = false;
                    if (mp != null) mp.start();
                    volume_mute_toggle.setImageResource(R.drawable.ic_speaker_icon);
                }
            }
        });

        // 9. Data Loading
        imagesList = new java.util.ArrayList<>();
        initSvgFunc();
        loadAnimations();
        cacheProxy = MyApplication.getProxy(SlideShowActivity.this);
        loadJSONFromAsset(SlideShowActivity.this);
    }

    // -------------------- REST OF YOUR ORIGINAL METHODS BELOW (UNCHANGED) --------------------

    private void startFadeIn() {
        final int FADE_DURATION = 3000;
        final int FADE_INTERVAL = 100;
        startFadeInFlex(FADE_DURATION, FADE_INTERVAL);
    }

    private void startFadeInFlex(int FADE_DURATION, int FADE_INTERVAL) {
        Log.d("1q1 music", "startFadeI: ");

        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;
        final float deltaVolume = maxVolume / (float) numberOfSteps;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeInStep(deltaVolume);
                if (volume >= maxVolume) {
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeInStep(float deltaVolume) {
        Log.d("1q1 music", "fadeInStep: ");
        try {
            if (mp != null) {
                mp.setVolume(volume, volume);
            }
            volume += deltaVolume;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startFadeOut(boolean stopPlayer) {
        final int FADE_DURATION = 3000;
        final int FADE_INTERVAL = 100;
        startFadeOutFlex(FADE_DURATION, FADE_INTERVAL, stopPlayer);
    }

    private void startFadeOutFlex(int FADE_DURATION, int FADE_INTERVAL, boolean stopPlayer) {
        Log.d("1q1 music", "startFadeOu: ");

        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;
        final float deltaVolume = volume / numberOfSteps;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                fadeOutStep(deltaVolume);

                if (volume <= 0) {
                    timer.cancel();
                    timer.purge();
                    if (stopPlayer) {
                        stopPlayer();
                        finish();
                    }
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeOutStep(float deltaVolume) {
        Log.d("1q1 music", "fadeOutStep: ");
        try {
            if (mp != null) {
                mp.setVolume(volume, volume);
            }
            volume -= deltaVolume;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    private void initSvgFunc() {
        requestBuilder = GlideToVectorYou
                .init()
                .with(this)
                .getRequestBuilder();
    }

    private void loadAnimations() {
        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_in);
    }

    public int getLineIndexFromUUID(String uuid) {
        return lineUuidMap.get(uuid);
    }

    public void loadJSONFromAsset(Context context) {
        data = (SlideshowJsonModel) SlideshowSharedPreferences.getPreferenceObjectJson(SlideShowActivity.this, "data");
        loadViewsFromData();
    }



public void loadBgMusicFromData(Slides currentSlide){
        if(!currentSlide.getMusic_Mood().equals(currentMusicMood)) {
            currentMusicMood = currentSlide.getMusic_Mood();
            if (mp != null) {
                //mp.stop();
                // Audio track #1 is playing but coming to the end**
                //startFadeOut();
                //mp.stop();
                mp.reset(); // https://stackoverflow.com/questions/26420837/java-lang-illegalstateexception-in-mediaplayer-setdatasource-using-ringtone-cla
            } else {
                //mp = new MediaPlayer();
            }
            //String RES_PREFIX = "android.resource://com.technikh.onedrupal/raw/";
            try {
                AssetFileDescriptor afd;
                if (mp != null) {
                    Log.d("TAG", "run: mp " + mp.toString());
                }
                switch (currentSlide.getMusic_Mood()) {
                    case "adventure":
                        //mp = null;
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.adventure);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                //afd.close();
                            }
                            //String filename = "android.resource://" + this.getPackageName() + "/raw/test0";
                            //mp.setDataSource(getApplicationContext(), Uri.parse(RES_PREFIX + "sad.mp3"));
                            //mp = MediaPlayer.create(getApplicationContext(), R.raw.sad);
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.adventure);
                            mp.start();
                        }
                        break;
                    case "playful":
                        //mp = null;
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.playful);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                //afd.close();
                            }
                            //mp.setDataSource(getApplicationContext(), Uri.parse(RES_PREFIX + "playful.mp3"));
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.playful);
                            mp.start();
                        }
                        break;
                    case "chilled":
                        //mp = null;
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.chilled);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                //afd.close();
                            }
                            //mp.setDataSource(getApplicationContext(), Uri.parse(RES_PREFIX + "inspiring.mp3"));
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.chilled);
                            mp.start();
                        }
                        break;
                    case "christmas":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.christmas);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.christmas);
                            mp.start();
                        }
                        break;
                    case "cinematic":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.cinematic);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.cinematic);
                            mp.start();
                        }
                        break;
                    case "creepy":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.creepy);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.creepy);
                            mp.start();
                        }
                        break;
                    case "cute":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.cute);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.cute);
                            mp.start();
                        }
                        break;
                    case "dreamy":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.dreamy);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.dreamy);
                            mp.start();
                        }
                        break;
                    case "emotional":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.emotional);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.emotional);
                            mp.start();
                        }
                        break;
                    case "energy":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.energy);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.energy);
                            mp.start();
                        }
                        break;
                    case "gentle":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.gentle);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.gentle);
                            mp.start();
                        }
                        break;
                    case "hope":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.hope);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.hope);
                            mp.start();
                        }
                        break;
                    case "inspirational":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.inspirational);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.inspirational);
                            mp.start();
                        }
                        break;
                    case "kids":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.kids);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.kids);
                            mp.start();
                        }
                        break;
                    case "tense":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.tense);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.tense);
                            mp.start();
                        }
                        break;
                    case "upbeat":
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.upbeat);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            }
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.upbeat);
                            mp.start();
                        }
                        break;
                    default:
                        if (mp != null) {
                            afd = getApplicationContext().getResources().openRawResourceFd(R.raw.playful);
                            if (afd != null) {
                                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                //afd.close();
                            }
                            //mp.setDataSource(getApplicationContext(), Uri.parse(RES_PREFIX + "playful.mp3"));
                        } else {
                            mp = MediaPlayer.create(getApplicationContext(), R.raw.playful);
                            mp.start();
                        }
                }
                mp.setLooping(true);
                //mp.start();


                //mp.setDataSource(getApplicationContext(), R.raw.inspiring);

                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            startFadeIn();
            // Audio track #2 has faded in and is now playing**
        }
    }

    public void loadViewsFromData() {
        rl.removeAllViews();
        relative.removeAllViews();
        Log.d("ttsrewrite", "loadViewsFromDat: ");
        Slides currentSlide = data.getSlides().get(NextCount);
        loadBgMusicFromData(currentSlide);
        Backgrounds currentSlideBg = currentSlide.getBackgrounds().get(0);
        if(!background_one_current_url.equals(currentSlideBg.getUrl())) {
            background_one_current_url = currentSlideBg.getUrl();

            if(currentSlideBg.getcredits() != null && !currentSlideBg.getcredits().isEmpty()) {
                tv_credits.setVisibility(View.VISIBLE);
                tv_credits.setText(currentSlideBg.getcredits());
                tv_credits.setHint(currentSlideBg.getcredits());
            }else{
                tv_credits.setVisibility(View.GONE);
                tv_credits.setHint("");
                tv_credits.setText("");
            }

            Glide.with(SlideShowActivity.this).
                    load(background_one_current_url)
                    .into(background_one);
        }

        tv = new TextView[currentSlide.getLines().size()];
        ImageArray = new ImageView[50];
        TextViewArray = new TextView[50];
        for (int i = 0; i < currentSlide.getLines().size(); i++) {
            numOfLinesInCurrentSlide = i+1;
            Lines currentLine = currentSlide.getLines().get(i);
            String lineText = currentLine.getText();
            tv[i] = new TextView(SlideShowActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            switch (currentSlide.getText_allignment()) {
                case "center":
                    params.gravity = Gravity.CENTER;
                    rl.setGravity(Gravity.CENTER);
                    break;
                case "right":
                    params.gravity = Gravity.END;
                    rl.setGravity(Gravity.END);
                    break;
                case "left":
                    params.gravity = Gravity.START;
                    rl.setGravity(Gravity.START);
                    break;
                case "bottom":
                    params.gravity = Gravity.BOTTOM;
                    rl.setGravity(Gravity.BOTTOM);
                    break;
            }
            tv[i].setText(currentLine.getText());
            tv[i].setPadding(20, 10, 20, 10);
            tv[i].setTextSize((float) currentLine.getStyle().getSize());

            tv[i].setTextColor(currentLine.getStyle().getIntColors()[0]);
            // Caused by: java.lang.IllegalArgumentException: needs >= 2 number of colors
            if(currentLine.getStyle().getIntColors().length >= 2) {
                TextPaint paint = tv[i].getPaint();
                float width = paint.measureText(currentLine.getText());

                // https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
                Shader textShader = new LinearGradient(0, 0, width, tv[i].getTextSize(),
                        currentLine.getStyle().getIntColors(), null, Shader.TileMode.CLAMP);
                tv[i].getPaint().setShader(textShader);
            }

            // java.lang.IllegalArgumentException: needs >= 2 number of colors
            if(currentLine.getStyle().getBgIntColors().length >= 2) {
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        currentLine.getStyle().getBgIntColors());
                gd.setCornerRadius(0f);
                tv[i].setBackground(gd);
            }else{
                tv[i].setBackgroundColor(getColorWithAlpha(Color.parseColor(currentLine.getStyle().getBgcolor()), currentLine.getStyle().getOpacity()));
            }

            tv[i].setLayoutParams(params);
            tv[i].setVisibility(View.INVISIBLE);
            rl.addView(tv[i]);

            // Images
            if (currentLine.getImages() != null && currentLine.getImages().size() > 0) {
                for (int valueForImage = 0; valueForImage < currentLine.getImages().size(); valueForImage++) {
                    int imageIndexPerLine = i+valueForImage;
                    Log.d("ttsrewrite1", "loadViewsFromDat: imageIndexPerLine "+imageIndexPerLine);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    RelativeLayout.LayoutParams tlayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    /*if(!currentLine.getImages().get(valueForImage).getMediaAttr().getHeight().isEmpty()){
                        if(!currentLine.getImages().get(valueForImage).getMediaAttr().getHeight().equals("MATCH_PARENT")) {
                            layoutParams.height = Integer.valueOf(currentLine.getImages().get(valueForImage).getMediaAttr().getHeight());
                        }
                    }else{
                        //layoutParams.height = 800;
                    }*/
                    if(!currentLine.getImages().get(valueForImage).getMediaAttr().getHeight().isEmpty()){
                        int specifiedHeight = 0;
                        try {
                            specifiedHeight = Integer.valueOf(currentLine.getImages().get(valueForImage).getMediaAttr().getHeight());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if(specifiedHeight > 1) {
                            layoutParams.height = specifiedHeight;
                        }
                    }
                    if(!currentLine.getImages().get(valueForImage).getMediaAttr().getWidth().isEmpty()){
                        int specifiedWidth = 0;
                        try {
                            specifiedWidth = Integer.valueOf(currentLine.getImages().get(valueForImage).getMediaAttr().getWidth());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if(specifiedWidth > 1) {
                            layoutParams.width = specifiedWidth;
                        }
                    }
                    switch (currentLine.getImages().get(valueForImage).getMediaAttr().getAlignment()) {
                        case "center_left":
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                            break;
                        case "center_right":
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                            break;
                        case "top_left":
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            break;
                        case "top_right":
                            tlayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            tlayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            break;
                        case "bottom_left":
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                            break;
                        case "bottom_right":
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                            break;
                    }
                    if (currentLine.getImages()
                            .get(valueForImage).getMediaFile() != null && currentLine.getImages()
                            .get(valueForImage).getMediaFile().getImage() != null) {
                        //ImageArray[valueForImage].pause();
                        if (currentLine.getImages().get(valueForImage).getMediaAttr().getEffects().getAnimate() != null){
                            Log.d("TAG", "run: KenBurnsEffect1");
                        }
                        if (currentLine.getImages()
                                .get(valueForImage).getMediaAttr().getEffects().getAnimate() != null && currentLine.getImages()
                                .get(valueForImage).getMediaAttr().getEffects().getAnimate().equals("KenBurnsEffect")) {
                            //ImageArray[valueForImage].resume();
                            ImageArray[imageIndexPerLine] = new KenBurnsView(SlideShowActivity.this);
                            DisplayMetrics dm = new DisplayMetrics();
                            this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                            int width = dm.widthPixels;
                            relative.getLayoutParams().width = width/2;
                        }else{
                            ImageArray[imageIndexPerLine] = new ImageView(SlideShowActivity.this);
                        }
                        TextViewArray[imageIndexPerLine] = new TextView(SlideShowActivity.this);
                        TextViewArray[imageIndexPerLine].setText(currentLine.getImages().get(valueForImage).getCaption());
                        TextViewArray[imageIndexPerLine].setTextColor(Color.parseColor("#000000"));
                        TextViewArray[imageIndexPerLine].setVisibility(View.INVISIBLE);
                        TextViewArray[imageIndexPerLine].setLayoutParams(tlayoutParams);
                        TextViewArray[imageIndexPerLine].setTextSize(28);
                        //TextViewArray[imageIndexPerLine].setHeight(40);
                        TextViewArray[imageIndexPerLine].setGravity(Gravity.CENTER_HORIZONTAL);
                        if(currentLine.getStyle().getIntColors().length >= 2) {
                            TextPaint paint = tv[i].getPaint();
                            float width = paint.measureText(currentLine.getText());

                            // https://stackoverflow.com/questions/2680607/text-with-gradient-in-android
                            Shader textShader = new LinearGradient(0, 0, width, tv[i].getTextSize(),
                                    currentLine.getStyle().getIntColors(), null, Shader.TileMode.CLAMP);
                            TextViewArray[imageIndexPerLine].getPaint().setShader(textShader);
                        }
                        if(currentLine.getStyle().getBgIntColors().length >= 2) {
                            GradientDrawable gd = new GradientDrawable(
                                    GradientDrawable.Orientation.TL_BR,
                                    currentLine.getStyle().getBgIntColors());
                            gd.setCornerRadius(0f);
                            TextViewArray[imageIndexPerLine].setBackground(gd);
                        }


                        // Add rule to layout parameters
                        // Add the ImageView below to Button
                        ImageArray[imageIndexPerLine].setPadding(20, 70, 20, 10);
                        ImageArray[imageIndexPerLine].setVisibility(View.INVISIBLE);
                        ImageArray[imageIndexPerLine].setLayoutParams(layoutParams);
//                                    ImageArray[valueForImage].setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                        // Order: Show text above Image, first add image
                        relative.addView(ImageArray[imageIndexPerLine]);
                        relative.addView(TextViewArray[imageIndexPerLine]);
                    }
                }

            }
        }
    }

    public void playSlideTextToSpeech(){
        Log.d("ttsrewrite", "playSlideTextToSpeech: ");
        Slides currentSlide = data.getSlides().get(NextCount);
        for (int i = 0; i < currentSlide.getLines().size(); i++) {
            Lines currentLine = currentSlide.getLines().get(i);
            String lineText = currentLine.getText();
            Log.d("ttsrewrite", "playSlideTextToSpeech: lineText "+lineText);
            if(isTextToSpeechReady) {
                Log.d("ttsrewrite", "playSlideTextToSpeech: in isTextToSpeechReady ");
                // set params
                // *** this method will work for more devices: API 19+ ***
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, currentLine.getUUID());
                lineUuidMap.put(currentLine.getUUID(),i);
                tts.speak(lineText, TextToSpeech.QUEUE_ADD, params);
                //Show Textview for first slide
                if(i==0){
                    showLineTextView(i, currentLine.getEffects().getEnter());
                }
            }
        }
    }

    public void showLineImagesView(int lineIndex, Lines line) {
        if(line.getImages() == null){
            return;
        }
        //int numOfImagesInThisLine = line.getImages().size();
        for (int valueForImage = 0; valueForImage < line.getImages().size(); valueForImage++) {
            int imageIndexPerLine = lineIndex+valueForImage;

            Log.d("axsd", "loadViewsFromDat: url "+line.getImages()
                    .get(valueForImage).getMediaFile().getImage().getUrl());
            Log.d("axsd", "showLineImagesView: line.getImages().get(valueForImage).getCredits() "+line.getImages().get(valueForImage).getCredits());
            Glide.with(SlideShowActivity.this).load(line.getImages()
                    .get(valueForImage).getMediaFile().getImage().getUrl()).into(ImageArray[imageIndexPerLine]);
            Techniques techniques = TextAnimations.getAnimationsTech(line.getImages().get(valueForImage).getMediaAttr().getEffects().getEnter());
            if (techniques == null) techniques = Techniques.FadeIn;
            image = YoYo.with(techniques)
                    .duration(700)
                    .playOn(ImageArray[imageIndexPerLine]);
            try {
                caption = YoYo.with(techniques)
                        .duration(700)
                        .playOn(TextViewArray[imageIndexPerLine]);
                TextViewArray[imageIndexPerLine].setVisibility(View.VISIBLE);

                ImageArray[imageIndexPerLine].setVisibility(View.VISIBLE);
                if (!line.getImages().get(valueForImage).getCredits().isEmpty()) {
                    tv_credits.setVisibility(View.VISIBLE);
                    tv_credits.setText(line.getImages().get(valueForImage).getCaption()+" From "+ line.getImages().get(valueForImage).getCredits());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG", "run: " + e.getLocalizedMessage());
            }
        }
    }

    public void hideLineImagesView(int lineIndex, Lines line) {
        if(line.getImages() == null){
            return;
        }
        for (int valueForImage = 0; valueForImage < line.getImages().size(); valueForImage++) {
            int imageIndexPerLine = lineIndex + valueForImage;
            Techniques techniques = TextAnimations.getAnimationsTech(line.getImages().get(valueForImage).getMediaAttr().getEffects().getExit());
            if (techniques == null) techniques = Techniques.FadeOut;
            try {
                if(tv_credits.getHint().toString().isEmpty()){
                    tv_credits.setVisibility(View.INVISIBLE);
                    tv_credits.setText("");
                }else if(!tv_credits.getHint().toString().isEmpty() && !tv_credits.getText().toString().equals(tv_credits.getHint())){
                    tv_credits.setVisibility(View.VISIBLE);
                    tv_credits.setText(tv_credits.getHint());
                }
                Exitimage = YoYo.with(techniques)
                        .duration(line.getImages().get(valueForImage).getMediaAttr().getEffects().getDuration(speedMilliSeconds))
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if(ImageArray[imageIndexPerLine] != null) {
                                    ImageArray[imageIndexPerLine].setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(ImageArray[imageIndexPerLine]);
            } catch (Exception e) {
                Log.d("TAG", "run: " + e.getLocalizedMessage());
            }
            if (TextViewArray[imageIndexPerLine] != null) {
                //Techniques techniques = TextAnimations.getAnimationsTech(AnimName);
                //if (techniques == null) techniques = Techniques.FadeIn;
                try {
                    Exittext = YoYo.with(techniques)
                            .duration(line.getImages().get(valueForImage).getMediaAttr().getEffects().getDuration(speedMilliSeconds))
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if(TextViewArray[imageIndexPerLine] != null) {
                                        TextViewArray[imageIndexPerLine].setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(TextViewArray[imageIndexPerLine]);
                } catch (Exception e) {
                    Log.d("TAG", "run: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void showLineTextView(int lineIndex, String animation){
        tv[lineIndex].setVisibility(View.VISIBLE);
        Techniques techniques = TextAnimations.getAnimationsTech(animation);
        if (techniques == null) techniques = Techniques.FadeIn;
        try {
            text = YoYo.with(techniques)
                    .duration(700)
                    .playOn(tv[lineIndex]);
        } catch (Exception e) {
            Log.d("TAG", "run: " + e.getLocalizedMessage());
        }
    }

    private int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }
    private void showSlideShowSummaryDialog() {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Slideshow Completed")
                    .setMessage("Your slideshow has finished playing.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

