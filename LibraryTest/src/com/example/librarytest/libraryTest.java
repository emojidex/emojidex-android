package com.example.librarytest;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import org.genshin.emojidexandroid.Emojidex;

public class libraryTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarytest_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.library_test, menu);
        return true;
    }

    public void emojify(View v)
    {
        Emojidex emojidex = new Emojidex(getApplicationContext());
        EditText editText = (EditText)findViewById(R.id.editText);

        editText.setText( emojidex.emojify( editText.getText() ) );
    }

    public void deEmojify(View v)
    {
        Emojidex emojidex = new Emojidex(getApplicationContext());
        EditText editText = (EditText)findViewById(R.id.editText);

        editText.setText( emojidex.deEmojify( editText.getText() ) );
    }
}
