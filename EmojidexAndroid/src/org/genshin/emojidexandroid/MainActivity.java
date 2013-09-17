package org.genshin.emojidexandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        initEmojidexTest();

        final CharSequence src = testEditText.getText();
        final CharSequence dest = emojidex.emojify(src);

        testEditText.setText(dest);
    }

    public void deEmojifyTest(View v)
    {
        initEmojidexTest();

        final CharSequence src = testEditText.getText();
        final CharSequence dest = emojidex.deEmojify(src);

        testEditText.setText(dest);
    }

    private void initEmojidexTest()
    {
        if(emojidex == null)
            emojidex = new Emojidex(getApplicationContext());
        if(testEditText == null)
            testEditText = (EditText)findViewById(R.id.testEditText);
    }
}
