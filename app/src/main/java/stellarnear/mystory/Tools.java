package stellarnear.mystory;

import android.app.Activity;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Log.SelfCustomLog;

/**
 * Created by jchatron on 16/02/2018.
 */

public class Tools extends SelfCustomLog {

    private static Tools instance;

    public Tools(){  }

    public static Tools getTools(){
        if (instance==null){
            instance = new Tools();
        }
        return instance;
    }

    public Integer toInt(String key) {
        Integer value = 0;
        try {
            value = Integer.parseInt(key);
        } catch (Exception e) {
            //some value are tested and if abscent we return default value
        }
        return value;
    }

    public List<Integer> toInt(List<String> listKey) {
        List<Integer> list = new ArrayList<>();
        for (String key : listKey) {
            list.add(toInt(key));
        }
        return list;
    }

    public Long toLong(String key) {
        Long value = 0L;
        try {
            value = Long.parseLong(key);
        } catch (Exception e) {
            //some value are tested and if abscent we return default value
        }
        return value;
    }

    public BigInteger toBigInt(String key) {
        BigInteger value = BigInteger.ZERO;
        try {
            value = new BigInteger(key);
        } catch (Exception e) {
            //some value are tested and if abscent we return default value
        }
        return value;
    }

    public Double toDouble(String key) {
        Double value;
        try {
            value = Double.parseDouble(key);
        } catch (Exception e){
            value=0.0;
            //some value are tested and if abscent we return default value
        }
        return value;
    }

    public Boolean toBool(String key) {
        Boolean value = false;
        try {
            value = Boolean.valueOf(key);
        } catch (Exception e) {
            //some value are tested and if abscent we return default value
        }
        return value;
    }

    public void resize(ImageView img, int dimensionPixelSize) {
        img.setLayoutParams(new LinearLayout.LayoutParams(dimensionPixelSize,dimensionPixelSize)); //note that it don't work with relative layout para
    }

    public Drawable convertToGrayscale(Drawable inputDraw) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        Drawable newDraw = inputDraw.mutate();
        newDraw.setColorFilter(filter);
        return newDraw;
    }


    public void customToast(Context mC, String txt, String... modeInput) {
        // Set the toast and duration
        String mode = modeInput.length > 0 ? modeInput[0] : "";
        Toast mToastToShow = Toast.makeText(mC, txt, Toast.LENGTH_LONG);

        if (mode.contains("center")) {
            TextView v = (TextView) mToastToShow.getView().findViewById(android.R.id.message);
            if (v != null) v.setGravity(Gravity.CENTER);
        }
        mToastToShow.setGravity(Gravity.CENTER, 0, 0);
        mToastToShow.show();

    }

    /*
    public void playVideo(Activity activity,Context context, String rawPath) {
        LayoutInflater inflater = activity.getLayoutInflater();
        final View layoutRecordVideo = inflater.inflate(R.layout.video_full_screen_gold_border, null);
        final CustomAlertDialog customVideo = new CustomAlertDialog(activity, context, layoutRecordVideo);
        customVideo.setPermanent(true);
        final VideoView video = (VideoView) layoutRecordVideo.findViewById(R.id.fullscreen_video_gold_border);
        video.setVisibility(View.VISIBLE);
        String fileName = "android.resource://" + activity.getPackageName() + rawPath;
        video.setMediaController(null);
        video.setVideoURI(Uri.parse(fileName));
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video.stopPlayback();
                customVideo.dismissAlert();
            }
        });
        video.start();
        customVideo.showAlert();
    }*/
}
