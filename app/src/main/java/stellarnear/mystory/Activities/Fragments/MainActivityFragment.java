package stellarnear.mystory.Activities.Fragments;

import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.seosh817.circularseekbar.BarStrokeCap;
import com.seosh817.circularseekbar.CircularSeekBar;
import com.seosh817.circularseekbar.CircularSeekBarAnimation;
import com.seosh817.circularseekbar.callbacks.OnProgressChangedListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private View returnFragView;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        unlockOrient();

        returnFragView = inflater.inflate(R.layout.fragment_main, container, false);

        Book book = MainActivity.getCurrentBook();
        if (book != null) {
            //TODO remove ca
            book.setCurrentPercent(69);

            FrameLayout mainCenter = returnFragView.findViewById(R.id.mainframe_center_for_progress);
            mainCenter.removeAllViews();


            CircularSeekBar seekBar = createProgress();
            seekBar.setOnProgressChangedListener(new OnProgressChangedListener() {
                @Override
                public void onProgressChanged(float v) {
                    ((TextView)returnFragView.findViewById(R.id.mainframe_progress_text)).setText(((int)v)+" %");
                    if(book.getMaxPages()!=null && book.getMaxPages()>0){
                        ((TextView)returnFragView.findViewById(R.id.mainframe_progress_page_text)).setText("("+book.getCurrentPage()+"/"+book.getMaxPages()+" pages)");
                    } else {
                        returnFragView.findViewById(R.id.mainframe_progress_page_text).setVisibility(View.GONE);
                    }
                }
            });
            mainCenter.addView(seekBar);
            seekBar.setProgress(book.getCurrentPercent());

            ((TextView) returnFragView.findViewById(R.id.mainfram_title)).setText(book.getName());
            ((TextView) returnFragView.findViewById(R.id.mainfram_author)).setText(book.getAutor().getFullName());

            setImage(book);
        } else {
            returnFragView.findViewById(R.id.mainfrag_info_linear).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.mainfrag_center).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.mainframe_no_current).setVisibility(View.VISIBLE);
        }

        return returnFragView;
    }

    private void setImage(Book book) {
        byte[] b = book.getImage();
        if (b == null || b.length < 7) {
            new BookNodeCalls().refreshImage(book);
            book.setOnImageRefreshedEventListener(new Book.OnImageRefreshedEventListener() {
                @Override
                public void onEvent() {
                    setImage(book);
                }
            });
            String file = "res/raw/no_image.png";
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(file)) {
                int nRead;
                byte[] dataBytes = new byte[16384];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = in.read(dataBytes, 0, dataBytes.length)) != -1) {
                    buffer.write(dataBytes, 0, nRead);
                }
                byte[] bNoimg = buffer.toByteArray();
                Drawable noImage = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bNoimg, 0, bNoimg.length));
                ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(noImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(b, 0, b.length));
            ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(image);
        }
    }

    private CircularSeekBar createProgress() {
        CircularSeekBar seekBar = new CircularSeekBar(getContext());

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.minimize_progress));
        seekBar.setLayoutParams(param);
        seekBar.setShowAnimation(true);
        seekBar.setAnimationInterpolator(CircularSeekBarAnimation.DECELERATE.getInterpolator());
        seekBar.setAnimationDurationMillis(3000);

        seekBar.setBarWidth(20);
        seekBar.setDashGap(2);
        seekBar.setDashWidth(1);
        seekBar.setInteractive(false);
        seekBar.setBarStrokeCap(BarStrokeCap.BUTT);

        seekBar.setInnerThumbColor(getContext().getColor(R.color.primary_dark_purple));
        seekBar.setInnerThumbRadius(getContext().getResources().getDimension(R.dimen.progress_inner_radius));
        seekBar.setInnerThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_inner_stroke));


        seekBar.setOuterThumbColor(getContext().getColor(R.color.primary_middle_purple));
        seekBar.setOuterThumbRadius(getContext().getResources().getDimension(R.dimen.progress_outer_radius));
        seekBar.setOuterThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_outer_stroke));


        // seekBar.setMin(0);
        // seekBar.setMax(100);

        seekBar.setProgressGradientColorsArray(getContext().getResources().getIntArray(R.array.rainbow));
        seekBar.setStartAngle(45);
        seekBar.setSweepAngle(270);
        return seekBar;
    }


    private void unlockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void lockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
