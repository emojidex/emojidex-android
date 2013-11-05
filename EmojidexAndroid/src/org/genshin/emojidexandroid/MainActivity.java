package org.genshin.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
    private ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        initEmojidexTest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    /**
     * Emojidex test.
     */
    private Emojidex emojidex = null;
    private EditText testEditText = null;

    public void emojifyTest(View v)
    {
        final CharSequence src = testEditText.getText();
        final CharSequence dest = emojidex.emojify(src);

        testEditText.setText(dest);
    }

    public void deEmojifyTest(View v)
    {
        final CharSequence src = testEditText.getText();
        final CharSequence dest = emojidex.deEmojify(src);

        testEditText.setText(dest);
    }

    public void actionSendTest(View v)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, testEditText.getText());
            intent.setType("text_view/plain");
            startActivity(Intent.createChooser(intent, "Send to"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("hoge", "send error.");
        }
    }

    private void initEmojidexTest()
    {
        if(emojidex == null)
            emojidex = new Emojidex(getApplicationContext());
        if(testEditText == null)
            testEditText = (EditText)findViewById(R.id.testEditText);

        // set viewFlipper action
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper_editor);
        viewFlipper.setOnTouchListener(new FlickTouchListener());
        viewFlipper.showNext();
    }

    private float lastTouchX;
    private float currentX;
    private class FlickTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    currentX = event.getX();
                    if (lastTouchX < currentX && viewFlipper.getCurrentView() != findViewById(R.id.emoji_text_layout))
                    {
                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
                        viewFlipper.showPrevious();
                    }
                    if (lastTouchX > currentX && viewFlipper.getCurrentView() != findViewById(R.id.text_layout))
                    {
                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
                        viewFlipper.showNext();
                    }
                    break;
            }
            return true;
        }
    }
}
