package com.team_scream_mit.scream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PreferencesActivity extends AppCompatActivity {

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferencesEditor;
    // private static final int PREFERENCE_MODE_PRIVATE = 0;
    private App parseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        //preferenceSettings = getPreferences(PREFERENCE_MODE_PRIVATE);
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesEditor = preferenceSettings.edit();

        parseApp = (App) getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.create_event) {
            Intent i = new Intent(this, AddEventActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.change_preferences){
            return true;
        } else if (id == R.id.about){
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.logout){
            parseApp.signOutFromGplus();
            finish();
            return true;
        } else if (id == R.id.main_screen){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Create a new event from the input form and send it to server
    public void updatePreference(View button) {


        //Event types (order of lists matter!!)
        Set<String> selectedCategories = new HashSet<String>();
        ArrayList<String> eventTypes = new ArrayList<String>(
                Arrays.asList("studyBreak","concert","party", "seminar", "infoSession"));
        ArrayList<Integer> eventIDs = new ArrayList<Integer>(
                        Arrays.asList( R.id.preferenceStudyBreak,R.id.preferenceConcert,
                                R.id.preferenceParty, R.id.preferenceSeminar, R.id.preferenceInfoSession));

        for (int i = 0; i< eventTypes.size(); i++){
            final CheckBox checkBox = (CheckBox) findViewById(eventIDs.get(i));
            if (checkBox.isChecked()){
                selectedCategories.add(eventTypes.get(i));
            }
        }

        //Event day range
        final EditText dateDayField = (EditText) findViewById(R.id.preferenceTimeRange);
        int daysFromToday = Integer.parseInt(dateDayField.getText().toString());


        //Start time
        final EditText startTimeHourField = (EditText) findViewById(R.id.preferenceStartTimeHour);
        int startTimeHour = Integer.parseInt( startTimeHourField.getText().toString() );

        final EditText startTimeMinField = (EditText) findViewById(R.id.preferenceStartTimeMin);
        int startTimeMin = Integer.parseInt( startTimeMinField.getText().toString() );


        //End time
        final EditText endTimeHourField = (EditText) findViewById(R.id.preferenceEndTimeHour);
        int endTimeHour = Integer.parseInt( endTimeHourField.getText().toString() );

        final EditText endTimeMinField = (EditText) findViewById(R.id.preferenceEndTimeMin);
        int endTimeMin = Integer.parseInt(endTimeMinField.getText().toString() );

        preferencesEditor.putInt("daysFromToday", daysFromToday);
        preferencesEditor.putInt("startTimeHour", startTimeHour);
        preferencesEditor.putInt("startTimeMin", startTimeMin);
        preferencesEditor.putInt("endTimeHour", endTimeHour);
        preferencesEditor.putInt("endTimeMin", endTimeMin);
        preferencesEditor.putStringSet("selectedCategories", selectedCategories);
        preferencesEditor.commit();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

    }
}
