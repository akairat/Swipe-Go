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
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class PreferencesActivity extends AppCompatActivity {

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferencesEditor;
    // private static final int PREFERENCE_MODE_PRIVATE = 0;
    private App parseApp;

    private TimePicker tp1;
    private TimePicker tp2;
    private int startTimeHour;
    private int startTimeMin;
    private int endTimeHour;
    private int endTimeMin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        //preferenceSettings = getPreferences(PREFERENCE_MODE_PRIVATE);
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferencesEditor = preferenceSettings.edit();

        parseApp = (App) getApplication();

        tp1 = (TimePicker) findViewById(R.id.preferenceStartTimeSpinner);
        tp1.setIs24HourView(true);
        tp1.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                //Display the new time to app interface
                startTimeHour = hourOfDay;
                startTimeMin = minute;
            }
        });

        tp2 = (TimePicker) findViewById(R.id.preferenceEndTimeSpinner);
        tp2.setIs24HourView(true);
        tp2.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                //Display the new time to app interface
                endTimeHour = hourOfDay;
                endTimeMin = minute;
            }
        });
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
                Arrays.asList("Study breaks", "Concerts", "Parties", "Info Sessions", "Seminars"));
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
