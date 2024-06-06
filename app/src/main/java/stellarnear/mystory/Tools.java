package stellarnear.mystory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.Log.SelfCustomLog;

/**
 * Created by jchatron on 16/02/2018.
 */

public class Tools extends SelfCustomLog {

    private static Tools instance;

    public Tools() {
    }

    public static Tools getTools() {
        if (instance == null) {
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
        } catch (Exception e) {
            value = 0.0;
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
        img.setLayoutParams(new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize)); //note that it don't work with relative layout para
    }

    public Drawable convertToGrayscale(Drawable inputDraw) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        Drawable newDraw = inputDraw.mutate();
        newDraw.setColorFilter(filter);
        return newDraw;
    }


    public void customToast(Context mC, String txt) {
        // Set the toast and duration
        Toast mToastToShow = Toast.makeText(mC, txt, Toast.LENGTH_LONG);
        mToastToShow.setGravity(Gravity.CENTER, 0, 0);
        mToastToShow.show();
    }

    public Snackbar customSnack(Context mC, View v, String txt, String... option) {
        try {
            // Set the toast and duration
            int dura = Snackbar.LENGTH_LONG;
            if (option.length > 0) {
                if (option[0].contains("short")) {
                    dura = Snackbar.LENGTH_SHORT;
                }
            }
            Snackbar snack = Snackbar.make(mC, v, txt, dura);
            View view = snack.getView();
            TextView tv = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setText(txt);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            if (option.length > 0) {
                if (option[0].contains("yellow")) {
                    snack.setTextColor(mC.getColor(R.color.primary_light_yellow));
                    view.setBackgroundColor(mC.getColor(R.color.primary_middle_yellow));
                } else if (option[0].contains("purple")) {
                    snack.setTextColor(mC.getColor(R.color.primary_light_purple));
                    view.setBackgroundColor(mC.getColor(R.color.primary_middle_purple));
                } else if (option[0].contains("pink")) {
                    snack.setTextColor(mC.getColor(R.color.primary_light_pink));
                    view.setBackgroundColor(mC.getColor(R.color.primary_middle_pink));
                } else if (option[0].contains("brown")) {
                    snack.setTextColor(mC.getColor(R.color.primary_light_brown));
                    view.setBackgroundColor(mC.getColor(R.color.primary_middle_brown));
                } else if (option[0].contains("green")) {
                    snack.setTextColor(mC.getColor(R.color.primary_light_green));
                    view.setBackgroundColor(mC.getColor(R.color.primary_middle_green));
                }
            }

            snack.show();

            return snack;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void convertByteToStoredFile(Book book) {
        if (book == null || book.getImagePath() != null) { //blank or already done
            return;
        }

        try {
            // Read the original image from bytes

            Bitmap originalBitmap = BitmapFactory.decodeByteArray(book.getImage(), 0, book.getImage().length);

            // Create a new scaled bitmap
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 400, 600, true);

            // Convert the scaled bitmap back to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);

            File imageFile = new File(LibraryLoader.getInternalStorageDir(), book.getUuid().toString() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                // Assuming 'imageBytes' is your byte array that you want to save
                fos.write(outputStream.toByteArray());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            book.setImagePath(imageFile.getAbsolutePath());
            book.discardBytes();
            LibraryLoader.saveBook(book);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fixMissingImage(Book book) {
        if (book.getImagePath() != null && !new File(book.getImagePath()).exists()) {
            new BookNodeCalls().refreshImage(book);
            book.setOnImageRefreshedEventListener(new Book.OnImageRefreshedEventListener() {
                @Override
                public void onEvent() {
                    book.setImagePath(null);
                    Tools.convertByteToStoredFile(book);
                }
            });
        }
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
