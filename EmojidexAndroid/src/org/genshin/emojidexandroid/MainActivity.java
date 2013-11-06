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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        initEmojidexEditor();
        //initEmojidexTest();
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

    private EditText emojiEditText;
    private EditText textEditText;
    private EditText emojiHalfEditText;
    private EditText textHalfEditText;
    private ViewFlipper viewFlipper;

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

    /*
    public void actionSendTest(View v)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, testEditText.getText());
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Send to"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("hoge", "send error.");
        }
    }
    */

    /*
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
    */

    private void initEmojidexEditor()
    {
        if (emojidex == null)
            emojidex = new Emojidex(getApplicationContext());

        emojiEditText = (EditText)findViewById(R.id.emoji_edittext);
        textEditText = (EditText) findViewById(R.id.text_edittext);
        emojiHalfEditText = (EditText)findViewById(R.id.emoji_half_edittext);
        textHalfEditText = (EditText)findViewById(R.id.text_half_edittext);

        // set viewFlipper action
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper_editor);
        viewFlipper.setOnTouchListener(new FlickTouchListener());
        // set default view
        viewFlipper.showNext();
    }

    private CharSequence emojify(CharSequence cs)
    {
        final CharSequence src = cs;
        final CharSequence dest = emojidex.emojify(src);

        return dest;
    }

    private CharSequence deEmojify(CharSequence cs)
    {
        final CharSequence src = cs;
        final CharSequence dest = emojidex.deEmojify(src);

        return dest;
    }

    public void share(View v)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_SEND);

            if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_layout))
                intent.putExtra(Intent.EXTRA_TEXT, emojiEditText.getText());
            else if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
                intent.putExtra(Intent.EXTRA_TEXT, textEditText.getText());

            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Send to"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("hoge", "send error.");
        }
    }

    public void moveRight(View v)
    {
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        viewFlipper.showNext();
    }

    public void moveLeft(View v)
    {
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
        viewFlipper.showPrevious();
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
                        // move text and emojify
                        if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
                            emojiEditText.setText(emojify(textEditText.getText()));
                        // move emoji and deEmojify
                        else
                        {
                            emojiHalfEditText.setText(emojiEditText.getText());
                            textHalfEditText.setText(deEmojify(emojiEditText.getText()));
                        }

                        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
                        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
                        viewFlipper.showPrevious();
                    }
                    if (lastTouchX > currentX && viewFlipper.getCurrentView() != findViewById(R.id.text_layout))
                    {
                        // move emoji
                        if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_text_layout))
                            emojiEditText.setText(emojiHalfEditText.getText());
                        // move text and deEmojify
                        else
                            textEditText.setText(deEmojify(emojiEditText.getText()));

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
