package org.genshin.emojidexandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testEmojidex();
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Emojidex test space.
     * Finally delete this method.
     */
    private void testEmojidex()
    {
        Emojidex emojidex = new Emojidex(getApplicationContext());
        String buf = "hot_beverage:hoge:piyo:hot_beverage:fuga::hogera:hot_beverage";

        android.util.Log.d("ime", "Source = " + buf);
        buf = emojidex.emojify(buf);
        android.util.Log.d("ime", "Encode = " + buf);
        buf = emojidex.deEmojify(buf);
        android.util.Log.d("ime", "Decode = " + buf);
    }
}
