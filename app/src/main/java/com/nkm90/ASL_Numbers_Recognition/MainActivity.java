package com.nkm90.ASL_Numbers_Recognition;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Loading the default language  of the Locale class
        loadLocale();
        setContentView(R.layout.activity_main);

        // Assign the elements with the id on the layout
        ImageButton btnLaunch = findViewById(R.id.btnLaunch);
        ImageButton btnLangChang = findViewById(R.id.btnLangChange);
        resultView = findViewById(R.id.result);

        //click listener to launch MediaPipe activity using a lambda, to invoke the openMP method
        // passing the view and the activity to launch when the button is clicked.
        btnLaunch.setOnClickListener(v -> openMP(v, MediaPipeActivity.class));

        //this click listener does not apply the lambda method on the view, it overrides the the onClick
        // method from the view, providing the change language dialog to be displayed when the button
        // btnLangChang is clicked.
        btnLangChang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }

    /**
     * Private method to change the language for the app. It will open an alert dialogue
     * that will display the list of languages, english and spanish, to be selected, setting
     * it as the current language by passing this result to the setLocale method and
     * restarting the whole app to display the language selected
     *
     */
    private void showChangeLanguageDialog() {
        //Array of languages available
        final String[] listitems = {"English", "Spanish"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Choose Language"); // Tittle of the alert dialog
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

    /**
     * The setLocale method takes a String with the language to be set on the default
     * language for the Locale class. I also get saved into the shared preferences of
     * the app, ready to be used on the others activities if needed.
     * @param lang String with the language code
     */
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(
                config,getBaseContext().getResources().getDisplayMetrics());
        //Saving the data to the shared preferences
        SharedPreferences.Editor editor =
                getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    //Load the language saved on the shared preferences

    /**
     * Method to load the language saved into the shared preferences to be used as the Locale
     * when called
     */
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    /**
     * Method to launch the MediaPipe hand tracking solution from its activity using an intent,
     * that will return an String containing the message obtained on the gesture recognition.
     * This will be handle by the onActivityResult method below.
     *
     * @param view Taking the global View class
     * @param activity It makes reference to the activity class for MediaPipe
     */
    public void openMP(View view, Class<MediaPipeActivity> activity) {
        Intent intent = new Intent(this, activity);
        startActivityForResult(intent, 1);
    }

    /**Method that handles the results obtained from the intents with activityForResults.
     * In this case only MediaPipe intent, the back button provides a message that is passed ready
     * to be displayed on the resultView.
     *
     * @param requestCode The code belonging to the different intents, being 1 for MediaPipe Activity
     * @param resultCode Code that helps to identify the Extra data passed from the intents
     * @param data The information obtained from the intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            String message = data.getStringExtra("MESSAGE");
            resultView.setText(message);
            resultView.setGravity(Gravity.BOTTOM);
        }
    }

    /*LIFECYCLE INTEGRATION
     * With the aim of keeping track of the different states that this activity is changing.
     * I just basically logs a message to the console as no other function is needed in this case*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("ActivityLifeCycle", "Main Activity - SaveInstanceState");
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