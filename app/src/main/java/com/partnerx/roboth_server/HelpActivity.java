package com.partnerx.roboth_server;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpActivity extends Activity {
    private ImageView back;
    private ScrollView scrollView;
    private TextView help;
    private TextView title;

    // /////总图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        scrollView = (ScrollView) findViewById(R.id.scrollViewH);
        help = (TextView) findViewById(R.id.helpH);
        title = (TextView) findViewById(R.id.titleH);;
        String titleStr = getString(R.string.help1);
        String str = null;
        try {
            str = "        "+getString(R.string.help2)+ "  \n"+
                    "        "+ getString(R.string.help3)+ "  \n"+
                    "        "+ getString(R.string.help4)+ "  \n"+
                    "        "+ getString(R.string.help5)+ "  \n"+
                    "        "+ getString(R.string.help6)+ "  \n"+
                    "        "+ getString(R.string.help7)+ "  \n"+
                    "        "+ getString(R.string.help8) + "  \n"+"  \n"+
                    "        " + getPackageManager().getPackageInfo(this.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        title.setText(titleStr);
        help.setText(str);
        titleStr = null;
        str = null;

        back = (ImageView) findViewById(R.id.backH);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
