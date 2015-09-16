package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Locale;

public class TutorialActivity extends Activity {
    private static final int PAGE_NUM = 13;

    private int page;
    private String localeString;

    private ViewFlipper viewFlipper;
    private Animation rightInAnimation;
    private Animation rightOutAnimation;
    private Animation leftInAnimation;
    private Animation leftOutAnimation;
    private RadioGroup radioGroup;
    private GestureDetector gestureDetector;

    private TextView textView;
    private ImageButton nextButton;
    private ImageButton prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        createTutorial();
    }

    private void createTutorial() {
        // Close keyboard.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        page = 0;

        // Set next/prev button.
        nextButton = (ImageButton)findViewById(R.id.tutotial_next_button);
        prevButton = (ImageButton)findViewById(R.id.tutotial_prev_button);
        prevButton.setVisibility(View.INVISIBLE);

        // TODO: 英語版画像は仮につき後で上書きする。
        // TODO: 画像が増える予定なのでstringsにも追加する。

        // Get locale.
        Locale locale = Locale.getDefault();
        if (Locale.JAPAN.equals(locale)) {
            localeString = "tutorial_ja_";
        } else {
            localeString = "tutorial_en_";
        }

        // Create view flipper.
        viewFlipper = (ViewFlipper)findViewById(R.id.tutorial_view_flipper);
        for (int i = 1; i <= PAGE_NUM; i++) {
            ImageView imageView = new ImageView(getApplicationContext());

            if (i <= 2) {
                String pageString = localeString + String.format("%1$02d", i);
                int resourceId = getResources().getIdentifier(pageString, "drawable", getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
                imageView.setImageBitmap(bitmap);
            }

            viewFlipper.addView(imageView);
        }

        // Create paging animation.
        rightInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in);
        rightOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
        rightOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nop
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ImageView imageView = (ImageView) viewFlipper.getChildAt(page + 1);
                releaseImage(imageView);
                radioGroup.check(page);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nop
            }
        });

        leftInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
        leftOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out);
        leftOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nop
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ImageView imageView = (ImageView) viewFlipper.getChildAt(page - 1);
                releaseImage(imageView);
                radioGroup.check(page);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nop
            }
        });

        // Create gesture.
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                // nop
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // nop
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float disX = e1.getX() - e2.getX();
                if (Math.abs(disX) < 100) return true;

                if (disX > 0) {
                    tutorialShowNext(null);
                } else {
                    tutorialShowPrevious(null);
                }

                return false;
            }
        });

        // Set header text.
        textView = (TextView)findViewById(R.id.tutorial_header);
        textView.setText(R.string.tutorial_header_01);

        // Create radio button. (Instead of page indicator.)
        radioGroup = (RadioGroup)findViewById(R.id.tutorial_indicator);

        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        for (int i = 0; i < PAGE_NUM; i++) {
            RadioButton button = new RadioButton(getApplicationContext());
            button.setId(i);
            button.setClickable(false);
            button.setMaxWidth(size.x / PAGE_NUM);
            radioGroup.addView(button);
        }
        radioGroup.check(0);
    }

    public void tutorialShowNext(View v) {
        // Last page.
        if (page == PAGE_NUM - 1) return;

        page++;
        setImage();
        setText();
        setButtonVisibility();

        viewFlipper.setInAnimation(rightInAnimation);
        viewFlipper.setOutAnimation(leftOutAnimation);
        viewFlipper.showNext();
    }

    public void tutorialShowPrevious(View v) {
        // First page.
        if (page == 0) return;

        page--;
        setImage();
        setText();
        setButtonVisibility();

        viewFlipper.setInAnimation(leftInAnimation);
        viewFlipper.setOutAnimation(rightOutAnimation);
        viewFlipper.showPrevious();
    }

    private void setImage() {
        ImageView imageView = (ImageView)viewFlipper.getChildAt(page);
        String pageString = localeString + String.format("%1$02d", (page + 1));
        int resourceId = getResources().getIdentifier(pageString, "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        imageView.setImageBitmap(bitmap);
    }

    private void setText() {
        String pageString = "tutorial_header_" + String.format("%1$02d", (page + 1));
        int resourceId = getResources().getIdentifier(pageString, "string", getPackageName());
        textView.setText(resourceId);
    }

    private void setButtonVisibility() {
        if (page == 0) {
            // First page.
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.INVISIBLE);
        } else if (page == PAGE_NUM - 1) {
            // Last page.
            nextButton.setVisibility(View.INVISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        } else {
            // Other page.
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        }
    }

    public void closeTutorial(View v) {
        releaseImages();
        finish();
    }

    private void releaseImage(ImageView imageView) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
    }

    private void releaseImages() {
        for (int i = 0; i < PAGE_NUM; i++) {
            ImageView imageView = (ImageView) viewFlipper.getChildAt(i);
            releaseImage(imageView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseImages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setImage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }
}
