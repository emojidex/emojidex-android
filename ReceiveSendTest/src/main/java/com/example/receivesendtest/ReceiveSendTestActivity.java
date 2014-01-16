package com.example.receivesendtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ReceiveSendTestActivity extends Activity {
    private EditText editText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_send_test);

        editText = (EditText)findViewById(R.id.editText);
        textView = (TextView)findViewById(R.id.textView);

        receiveSend();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.receive_send_test, menu);
        return true;
    }

    public void onTest(View v)
    {
        final Editable src = editText.getText();

        textView.setText(""
                + "length = " + src.length() + "\n"
        );
    }

    public void onLog(View v)
    {
        log(editText.getText());
    }

    private void receiveSend()
    {
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if(action.equals(Intent.ACTION_SEND))
        {
            final Bundle extras = intent.getExtras();
            Log.d("extras", "extras size = " + extras.size());
            if(extras != null)
            {
                final CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
                log(text);
                editText.setText(text);
            }
            onTest(null);
        }
    }

    private void log(CharSequence src)
    {
        final String logTag = "textDetail";
        final int length = src.length();
        Log.d(logTag, "length = " + length + ", class = " + src.getClass().getName());

        if(src instanceof Spanned)
        {
            final Class kind = Object.class;
            final Spanned spanned = (Spanned)src;
            int next;
            for(int start = 0;  start < length;  start = next)
            {
                next = spanned.nextSpanTransition(start, length, kind);
                final Object[] spans = spanned.getSpans(start, next, kind);
                Log.d(logTag, "　　spans : start = " + start + ", next = " + next + ", count = " + spans.length);
                for(Object span : spans)
                {
                    Log.d(logTag, "　　　　span : class = " + span.getClass().getName());
                }
            }
        }
    }
}
