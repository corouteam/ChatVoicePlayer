    /*
    This class is a part of Chat Voice Player View Android Library
    Developed by: Jagar Yousef
    Date: 2019
    */

package me.jagar.chatvoiceplayerlibrary;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import androidx.core.content.FileProvider;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

    public class VoicePlayerView extends LinearLayout {

    private int playPaueseBackgroundColor, shareBackgroundColor, viewBackgroundColor,
            seekBarProgressColor, seekBarThumbColor, progressTimeColor;
    private float viewCornerRadius, playPauseCornerRadius, shareCornerRadius;
    private boolean showShareButton, showTiming;
    private GradientDrawable playPauseShape, shareShape, viewShape;
    private Context context;
    private String path;
    private String shareTitle = "Share Voice";
    public boolean isLoaded = false;

    private LinearLayout main_layout, padded_layout, container_layout;
    private ImageView imgPlay, imgPause, imgShare;
    private SeekBar seekBar;
    private ProgressBar progressBar, loadingBar;
    private TextView txtProcess;
    private MediaPlayer mediaPlayer;

    public VoicePlayerView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.main_view, this);
        this.context = context;
    }

    public VoicePlayerView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
        this.context = context;
    }

    public VoicePlayerView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
        this.context = context;
    }

    private void initViews(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.VoicePlayerView, 0, 0);

        viewShape = new GradientDrawable();
        playPauseShape = new GradientDrawable();
        shareShape = new GradientDrawable();

        try{
            showShareButton = typedArray.getBoolean(R.styleable.VoicePlayerView_showShareButton, true);
            showTiming = typedArray.getBoolean(R.styleable.VoicePlayerView_showTiming, true);
            viewCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_viewCornerRadius, 0);
            playPauseCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0);
            shareCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_shareCornerRadius, 0);
            playPaueseBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseBackgroundColor, getResources().getColor(R.color.pink));
            shareBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_shareBackgroundColor, getResources().getColor(R.color.pink));
            viewBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_viewBackground, getResources().getColor(R.color.white));
            seekBarProgressColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarProgressColor, getResources().getColor(R.color.pink));
            seekBarThumbColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarThumbColor, getResources().getColor(R.color.pink));
            progressTimeColor = typedArray.getColor(R.styleable.VoicePlayerView_progressTimeColor, Color.GRAY);
            shareTitle = typedArray.getString(R.styleable.VoicePlayerView_shareText);
        }finally {
            typedArray.recycle();
        }


        LayoutInflater.from(context).inflate(R.layout.main_view, this);
        main_layout = this.findViewById(R.id.collectorLinearLayout);
        padded_layout = this.findViewById(R.id.paddedLinearLayout);
        container_layout = this.findViewById(R.id.containerLinearLayout);
        imgPlay = this.findViewById(R.id.imgPlay);
        imgPause = this.findViewById(R.id.imgPause);
        imgShare = this.findViewById(R.id.imgShare);
        seekBar = this.findViewById(R.id.seekBar);
        progressBar = this.findViewById(R.id.progressBar);
        loadingBar = this.findViewById(R.id.loadingBar);
        txtProcess = this.findViewById(R.id.txtTime);


        viewShape.setColor(viewBackgroundColor);
        viewShape.setCornerRadius(viewCornerRadius);
        playPauseShape.setColor(playPaueseBackgroundColor);
        playPauseShape.setCornerRadius(playPauseCornerRadius);
        shareShape.setColor(shareBackgroundColor);
        shareShape.setCornerRadius(shareCornerRadius);

        imgPlay.setBackground(playPauseShape);
        imgPause.setBackground(playPauseShape);
        imgShare.setBackground(shareShape);
        main_layout.setBackground(viewShape);
        seekBar.getProgressDrawable().setColorFilter(seekBarProgressColor, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(seekBarThumbColor, PorterDuff.Mode.SRC_IN);
        txtProcess.setTextColor(progressTimeColor);


        if (!showShareButton)
            imgShare.setVisibility(GONE);
        if (!showTiming)
            txtProcess.setVisibility(INVISIBLE);

    }


    //Set the audio source and prepare mediaplayer

    public void setAudio(String AudioPath){

        path = AudioPath;
        mediaPlayer =  new MediaPlayer();
        handleLoading();
        if (AudioPath != null) {
            try {
                mediaPlayer.setDataSource(AudioPath);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setVolume(10, 10);

                //START and PAUSE are in other listeners
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        seekBar.setMax(mp.getDuration());
                        txtProcess.setText("00:00:00/"+convertSecondsToHMmSs(mp.getDuration() / 1000));
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                            imgPause.setVisibility(View.GONE);
                            imgPlay.setVisibility(View.VISIBLE);

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        seekBar.setOnSeekBarChangeListener(seekBarListener);
        imgPlay.setOnClickListener(imgPlayClickListener);
        imgPause.setOnClickListener(imgPauseClickListener);
        imgShare.setOnClickListener(imgShareClickListener);
    }


    //Components' listeners

    View.OnClickListener imgPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPause.setVisibility(View.VISIBLE);
            imgPlay.setVisibility(View.GONE);
            mediaPlayer.start();
            try{
                update(mediaPlayer, txtProcess, seekBar, context);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };



    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser)
            {
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);


        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            imgPlay.setVisibility(View.GONE);
            imgPause.setVisibility(View.VISIBLE);
            mediaPlayer.start();

        }
    };

    View.OnClickListener imgPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPause.setVisibility(View.GONE);
            imgPlay.setVisibility(View.VISIBLE);
            mediaPlayer.pause();
        }
    };

    View.OnClickListener imgShareClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgShare.setVisibility(GONE);
                    progressBar.setVisibility(VISIBLE);
                }
            });
            File file = new File(path);
            if (file.exists()){
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                }
                    intentShareFile.putExtra(Intent.EXTRA_STREAM,
                            Uri.parse("file://"+file.getAbsolutePath()));

                context.startActivity(Intent.createChooser(intentShareFile, shareTitle));
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(GONE);
                            imgShare.setVisibility(VISIBLE);
                        }
                    });

                }
            }, 500);

        }
    };

    public void onAudioUploaded(){
        loadingBar.setVisibility(GONE);
        imgPlay.setVisibility(VISIBLE);
    }

    public void audioIsUploading(){
        loadingBar.setVisibility(VISIBLE);
        imgPlay.setVisibility(GONE);
        imgPause.setVisibility(GONE);

    }

    public void handleLoading(){
            if(isLoaded){
                loadingBar.setVisibility(GONE);
                imgPlay.setVisibility(View.VISIBLE);
                imgPause.setVisibility(View.GONE);
            }else {
                loadingBar.setVisibility(VISIBLE);
                imgPlay.setVisibility(View.GONE);
                imgPause.setVisibility(View.GONE);
            }


    }

    //Updating seekBar in realtime
    private void update(final MediaPlayer mediaPlayer, final TextView time, final SeekBar seekBar, final Context context) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() > 100) {
                    time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition() / 1000) + " / " + convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                }
                else {
                    time.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                    seekBar.setProgress(0);
                }
                Handler handler = new Handler();
                try{
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if (mediaPlayer.getCurrentPosition() > -1) {
                                    try {
                                        update(mediaPlayer, time, seekBar, context);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    };
                    handler.postDelayed(runnable, 2);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    //Convert long milli seconds to a formatted String to display it

    private static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }

    //These both functions to avoid mediaplayer errors

    public void onStop(){
        try{
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onPause(){
        try{
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
            imgPause.setVisibility(View.GONE);
            imgPlay.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.GONE);


    }


  // Programmatically functions

   public void setViewBackgroundShape(int color, float radius){
       GradientDrawable shape = new GradientDrawable();
       shape.setColor(getResources().getColor(color));
       shape.setCornerRadius(radius);
       main_layout.setBackground(shape);
   }
    public void setShareBackgroundShape(int color, float radius){
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(color));
        shape.setCornerRadius(radius);
        imgShare.setBackground(shape);
    }
    public void setPlayPaueseBackgroundShape(int color, float radius){
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(color));
        shape.setCornerRadius(radius);
        imgPause.setBackground(shape);
        imgPlay.setBackground(shape);
    }
    public void setSeekBarStyle(int progressColor, int thumbColor){
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(progressColor), PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(getResources().getColor(thumbColor), PorterDuff.Mode.SRC_IN);
    }
    public void setTimingVisibility(boolean visibility){
       if (!visibility)
           txtProcess.setVisibility(INVISIBLE);
       else
           txtProcess.setVisibility(VISIBLE);
    }
    public void setShareButtonVisibility(boolean visibility){
        if (!visibility)
            imgShare.setVisibility(GONE);
        else
            imgShare.setVisibility(VISIBLE);
    }
    public void setShareText(String shareText){
       shareTitle = shareText;
    }
}
