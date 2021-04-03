package com.nkm90.ASL_Numbers_Recognition;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnLaunch;
    private Button btnLangChang;
    public TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        //Change the ActionBar tittle from the language on the settings
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));

        btnLaunch = findViewById(R.id.btnLaunch);
        btnLangChang = findViewById(R.id.btnLangChange);
        resultView = findViewById(R.id.result);


        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMP(v, MediaPipeActivity.class);
            }
        });

        btnLangChang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }

    private void showChangeLanguageDialog() {
        //Array of languages available
        final String[] listitems = {"English", "Spanish"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(listitems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i==0){
                    setLocale("en");
                    recreate();
                }
                else if (i==1){
                    setLocale("es");
                    recreate();
                }

                //Close the dialog once the language has been selected
                dialog.dismiss();
            }
        });

        //Displaying the dialog
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        //Saving the data to the shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    //Load the language saved on the shared preferences
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    public void openMP(View view, Class<MediaPipeActivity> activity) {
        Intent intent = new Intent(this, activity);
        startActivityForResult(intent, 1);
    }
    //Method to get the message from Mediapipe activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            String message = data.getStringExtra("MESSAGE");
            resultView.setText(message);
        }
    }

    /*LIFECYCLE INTEGRATION
     * With the aim of keeping track of the different states that this activity is changing.
     * I just basically logs a message to the console as no other function is needed in this case*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("ActivityLifeCycle", "Main Activity - onSaveInstanceState()");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d("ActivityLifeCycle", "Main Activity - onStart");
    }

    @Override
    protected void onRestart()
    {
        Log.d("ActivityLifeCycle", "Main Activity - onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        Log.d("ActivityLifeCycle", "Main Activity - onResume");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.d("ActivityLifeCycle", "Main Activity - onPause");
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        Log.d("ActivityLifeCycle", "Main Activity - onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d("ActivityLifeCycle", "Main Activity - onDestroy");
        super.onDestroy();
    }
}