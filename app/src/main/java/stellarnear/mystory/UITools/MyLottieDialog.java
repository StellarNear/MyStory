package stellarnear.mystory.UITools;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RawRes;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import stellarnear.mystory.R;

public class MyLottieDialog {

    /**
     * Dialog variable to setup and control the lottie dialog
     */
    private final Dialog lottieDialog;

    /**
     * View to control the title of lottie dialog
     */
    private final TextView lottieDialogTitle;

    /**
     * View to control the message of lottie dialog
     */
    private final TextView lottieDialogMessage;

    /**
     * View to control the main layout of lottie dialog
     */
    private final LinearLayout lottieDialogLayout;

    /**
     * View to control the action layout of lottie dialog
     */
    private final LinearLayout lottieDialogButtonsLayout;

    /**
     * container of the lottie animation view to change the attributes
     */
    private final LinearLayout lottieDialogAnimationContainer;

    /**
     * Lottie animation view to control the animation
     */
    private final LottieAnimationView lottieDialogAnimationView;

    private final View lottieDialogInnerMessageView;

    private final Context mC;
    private final View view;

    /**
     * Start playing the animation after setup if only if it true
     */
    private boolean isAutoPlayedAnimation = false;

    /**
     * property to repeat the animation indefinitely
     */
    public static final int INFINITE = LottieDrawable.INFINITE;


    /**
     * Layout params used for action buttons to the weight of each one equal to 1
     */
    private static final LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1);

    public MyLottieDialog(Context context) {
        this.mC = context;
        view = LayoutInflater.from(context).inflate(R.layout.my_lottie_alert, null);

        this.lottieDialog = new Dialog(context, R.style.CustomDialog);
        this.lottieDialog.setContentView(view);

        this.lottieDialogTitle = view.findViewById(R.id.lottie_dialog_title);
        this.lottieDialogMessage = view.findViewById(R.id.lottie_dialog_message);
        this.lottieDialogAnimationView = view.findViewById(R.id.lottie_dialog_animation);
        this.lottieDialogLayout = view.findViewById(R.id.lottie_dialog_layout);
        this.lottieDialogButtonsLayout = view.findViewById(R.id.lottie_dialog_buttons_layout);
        this.lottieDialogAnimationContainer = view.findViewById(R.id.lottie_dialog_animation_container);
        lottieDialogAnimationContainer.setVisibility(View.GONE);
        this.lottieDialogInnerMessageView = view.findViewById(R.id.lottie_dialog_inner_view);

      /*
      TypedValue valueLight = new TypedValue();
      mC.getTheme().resolveAttribute(R.attr.colorVariantLight, valueLight, true);
      lottieDialogTitle.setTextColor(valueLight.data);
      TypedValue valueMiddle = new TypedValue();
      mC.getTheme().resolveAttribute(R.attr.colorVariantMiddle, valueMiddle, true);
      lottieDialogMessage.setTextColor(valueMiddle.data);*/

    }

    public MyLottieDialog(Context context, View view) {
        this.mC = context;
        this.view = view;
        this.lottieDialog = new Dialog(context, R.style.CustomDialog);
        this.lottieDialog.setContentView(view);

        this.lottieDialogTitle = view.findViewById(R.id.lottie_dialog_title);
        this.lottieDialogMessage = view.findViewById(R.id.lottie_dialog_message);
        this.lottieDialogAnimationView = view.findViewById(R.id.lottie_dialog_animation);
        this.lottieDialogLayout = view.findViewById(R.id.lottie_dialog_layout);
        this.lottieDialogButtonsLayout = view.findViewById(R.id.lottie_dialog_buttons_layout);
        this.lottieDialogAnimationContainer = view.findViewById(R.id.lottie_dialog_animation_container);
        lottieDialogAnimationContainer.setVisibility(View.GONE);
        this.lottieDialogInnerMessageView = view.findViewById(R.id.lottie_dialog_inner_view);

      /*
      TypedValue valueLight = new TypedValue();
      mC.getTheme().resolveAttribute(R.attr.colorVariantLight, valueLight, true);
      lottieDialogTitle.setTextColor(valueLight.data);
      TypedValue valueMiddle = new TypedValue();
       mC.getTheme().resolveAttribute(R.attr.colorVariantMiddle, valueMiddle, true);
      lottieDialogMessage.setTextColor(valueMiddle.data);*/
    }

    /**
     * Set the dialog title content
     *
     * @param title of the lottie dialog
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setTitle(String title) {
        this.lottieDialogTitle.setText(title);
        return this;
    }

    /**
     * Set the dialog title text color
     *
     * @param color for lottie dialog title
     * @return the instance of lottie dialog to make a chain of function easily
     */
    private MyLottieDialog setTitleColor(int color) {
        this.lottieDialogTitle.setTextColor(color);
        return this;
    }

    /**
     * Set the dialog title text size
     *
     * @param size of the text
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setTitleTextSize(float size) {
        this.lottieDialogTitle.setTextSize(size);
        return this;
    }

    /**
     * Set the dialog title visibility
     *
     * @param visibility value of the title VISIBLE, INVISIBLE or GONE
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setTitleVisibility(int visibility) {
        this.lottieDialogTitle.setVisibility(visibility);
        return this;
    }

    /**
     * Set the dialog message content
     *
     * @param message of the lottie dialog
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setMessage(String message) {
        this.lottieDialogMessage.setText(message);
        return this;
    }

    // my addition to replace with inner view
    public MyLottieDialog setMessage(View alertInnerInfo) {
        this.lottieDialogMessage.setVisibility(View.GONE);

        ViewGroup parent = (ViewGroup) lottieDialogMessage.getParent();
        if (parent != null) {
            final int index = parent.indexOfChild(this.lottieDialogInnerMessageView);
            parent.removeViewAt(index);
            parent.addView(alertInnerInfo, index);
        }
        return this;
    }

    /**
     * Set the dialog message text color
     *
     * @param color for lottie dialog message
     * @return the instance of lottie dialog to make a chain of function easily
     */
    private MyLottieDialog setMessageColor(int color) {
        this.lottieDialogMessage.setTextColor(color);
        return this;
    }

    /**
     * Set the dialog message text size
     *
     * @param size of the text
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setMessageTextSize(float size) {
        this.lottieDialogMessage.setTextSize(size);
        return this;
    }

    /**
     * Set the dialog message visibility
     *
     * @param visibility value of the message VISIBLE, INVISIBLE or GONE
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setMessageVisibility(int visibility) {
        this.lottieDialogMessage.setVisibility(visibility);
        return this;
    }

    /**
     * Set the dialog background color
     *
     * @param color for the dialog background
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogBackground(int color) {
        this.lottieDialogLayout.setBackgroundColor(color);
        return this;
    }

    /**
     * Set the dialog background Drawable
     *
     * @param drawable for the dialog background
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogBackgroundDrawable(Drawable drawable) {
        this.lottieDialogLayout.setBackground(drawable);
        return this;
    }

    /**
     * Set dim amount for lottie dialog
     *
     * @param amount the amount for dim attribute
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogDimAmount(float amount) {
        lottieDialog.getWindow().setDimAmount(amount);
        return this;
    }

    /**
     * Set the Cancelable option for the lottie dialog
     *
     * @param cancelable the dialog will not cancel by the end user if the value is false
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setCancelable(boolean cancelable) {
        this.lottieDialog.setCancelable(cancelable);
        return this;
    }

    /**
     * Set the Cancelable OnTouchOutside option for the lottie dialog
     *
     * @param cancel true to cancel when the end use touch outside
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setCanceledOnTouchOutside(boolean cancel) {
        this.lottieDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    /**
     * Set the height of the dialog
     *
     * @param height value of the dialog layout
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogHeight(int height) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(lottieDialog.getWindow().getAttributes());
        layoutParams.height = height;
        lottieDialog.getWindow().setAttributes(layoutParams);
        return this;
    }

    /**
     * Set the width of the dialog
     *
     * @param width value of the dialog layout
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogWidth(int width) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(lottieDialog.getWindow().getAttributes());
        layoutParams.width = width;
        lottieDialog.getWindow().setAttributes(layoutParams);
        return this;
    }

    /**
     * Set the height of the dialog by percentage
     *
     * @param percentage height of the dialog from 0 to 1
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogHeightPercentage(float percentage) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int heightPixels = (int) (metrics.heightPixels * percentage);
        return setDialogHeight(heightPixels);
    }

    /**
     * Set the width of the dialog by percentage
     *
     * @param percentage width of the dialog from 0 to 1
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setDialogWidthPercentage(float percentage) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int widthPixels = (int) (metrics.widthPixels * percentage);
        return setDialogWidth(widthPixels);
    }

    /**
     * Set the height of the lottie animation view
     *
     * @param height the height of the lottie animation view
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimationViewHeight(int height) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = lottieDialogAnimationContainer.getLayoutParams();
        layoutParams.height = height;
        lottieDialogAnimationContainer.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * Set the width of the lottie animation view
     *
     * @param width the width of the lottie animation view
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimationViewWidth(int width) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = lottieDialogAnimationContainer.getLayoutParams();
        layoutParams.width = width;
        lottieDialogAnimationContainer.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * Set the animation from raw resource directory
     *
     * @param rawRes animation id from raw resource
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimation(@RawRes int rawRes) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setAnimation(rawRes);
        return this;
    }

    /**
     * Set the animation from Animation class
     *
     * @param animation animation object
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimation(Animation animation) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setAnimation(animation);
        return this;
    }

    /**
     * Set the animation from assets
     *
     * @param assetName animation name from assets
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimation(String assetName) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setAnimation(assetName);
        return this;
    }

    /**
     * Set the animation from url
     *
     * @param url animation url to load it, need INTERNET permission in the app to run correctly
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimationFromUrl(String url) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setAnimationFromUrl(url);
        return this;
    }

    /**
     * Set the animation repeat count
     *
     * @param count the animation repeat count can be use MyLottieDialog.INFINITE to repeat infinite
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimationRepeatCount(int count) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setRepeatCount(count);
        return this;
    }

    /**
     * Set the speed of the lottie animation
     *
     * @param speed the speed of the animation
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAnimationSpeed(float speed) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.setSpeed(speed);
        return this;
    }

    /**
     * Add new button inside dialog action buttons layout in the last position
     *
     * @param button the button view
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog addActionButton(Button button) {
        this.lottieDialogButtonsLayout.addView(button, linearLayoutParams);
        return this;
    }

    /**
     * Add new button inside dialog action buttons layout with index
     *
     * @param button the button view
     * @param index  index of this button inside the layout
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog addActionButton(Button button, int index) {
        this.lottieDialogButtonsLayout.addView(button, index, linearLayoutParams);
        return this;
    }

    /**
     * @param autoplay play the animation in show function if it true
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setAutoPlayAnimation(boolean autoplay) {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        isAutoPlayedAnimation = autoplay;
        return this;
    }

    /**
     * @return true if the auto played animation options is true
     */
    public boolean isAutoPlayedAnimation() {
        return isAutoPlayedAnimation;
    }

    /**
     * Play the current animation
     */
    public void playAnimation() {
        lottieDialogAnimationContainer.setVisibility(View.VISIBLE);
        this.lottieDialogAnimationView.playAnimation();
    }

    /**
     * Pause the current animation
     */
    public void pauseAnimation() {
        this.lottieDialogAnimationView.pauseAnimation();
    }

    /**
     * Cancel the current animation
     */
    public void cancelAnimation() {
        this.lottieDialogAnimationView.cancelAnimation();
    }

    /**
     * Clear the current Animation
     */
    public void clearAnimation() {
        this.lottieDialogAnimationView.clearAnimation();
    }

    /**
     * Reverse the current animation speed
     */
    public void reverseAnimationSpeed() {
        this.lottieDialogAnimationView.reverseAnimationSpeed();
    }

    /**
     * @return true if the current animation is playing
     */
    public boolean isAnimating() {
        return lottieDialogAnimationView.isAnimating();
    }

    /**
     * Show the lottie dialog and handle auto playing animation option
     */
    public void show() {
        this.lottieDialog.show();
        if (isAutoPlayedAnimation) playAnimation();
    }

    /**
     * @return true if lottie dialog is showing
     */
    public boolean isShowing() {
        return this.lottieDialog.isShowing();
    }

    /**
     * Dismiss the lottie dialog
     */
    public void dismiss() {
        this.lottieDialog.dismiss();
    }

    /**
     * Cancel the lottie dialog
     */
    public void cancel() {
        this.lottieDialog.cancel();
    }

    /**
     * Set the lottie dialog on dialog show listener
     *
     * @param listener DialogInterface.OnShowListener listener
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setOnShowListener(DialogInterface.OnShowListener listener) {
        this.lottieDialog.setOnShowListener(listener);
        return this;
    }

    /**
     * Set the lottie dialog on dialog cancel listener
     *
     * @param listener DialogInterface.OnCancelListener listener
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.lottieDialog.setOnCancelListener(listener);
        return this;
    }

    /**
     * Set the lottie dialog on dialog dismiss listener
     *
     * @param listener DialogInterface.OnDismissListener listener
     * @return the instance of lottie dialog to make a chain of function easily
     */
    public MyLottieDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.lottieDialog.setOnDismissListener(listener);
        return this;
    }


    public MyLottieDialog setCancelOnTouchItself(boolean b) {
        if (b) {
            this.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
        return this;
    }
}
