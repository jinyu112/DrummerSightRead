package com.tidisventures.drummersightread;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * Created by JinYu on 11/8/2016.
 */
public class About extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView =(TextView)findViewById(R.id.about_link);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://tidisventures.com'> http://tidisventures.com </a>";
        textView.setText(Html.fromHtml(text));
    }


}
