package com.team_scream_mit.scream;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddEventActivity extends AppCompatActivity {

    private App parseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        parseApp = (App) getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.create_event) {
            return true;
        } else if (id == R.id.change_preferences){
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
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
    public void createNewEvent(View button) {


        //category
        final Spinner eventTypeSpinner = (Spinner) findViewById(R.id.category);
        String evenType = eventTypeSpinner.getSelectedItem().toString();


        //title, description, location
        final EditText titleField = (EditText) findViewById(R.id.title);
        String title = titleField.getText().toString();

        final EditText descriptionField = (EditText) findViewById(R.id.description);
        String description = descriptionField.getText().toString();

        final EditText locationField = (EditText) findViewById(R.id.location);
        String location = locationField.getText().toString();

        //Event date
        final EditText dateDayField = (EditText) findViewById(R.id.dateDay);
        int dateDay = Integer.parseInt(dateDayField.getText().toString());

        final EditText dateMonthField = (EditText) findViewById(R.id.dateMonth);
        int dateMonth = Integer.parseInt(dateMonthField.getText().toString());

        final EditText dateYearField = (EditText) findViewById(R.id.dateYear);
        int dateYear = Integer.parseInt( dateYearField.getText().toString() );

        //Start time
        final EditText startTimeHourField = (EditText) findViewById(R.id.startTimeHour);
        int startTimeHour = Integer.parseInt( startTimeHourField.getText().toString() );

        final EditText startTimeMinField = (EditText) findViewById(R.id.startTimeMin);
        int startTimeMin = Integer.parseInt( startTimeMinField.getText().toString() );


        //End time
        final EditText endTimeHourField = (EditText) findViewById(R.id.endTimeHour);
        int endTimeHour = Integer.parseInt( endTimeHourField.getText().toString() );

        final EditText endTimeMinField = (EditText) findViewById(R.id.endTimeMin);
        int endTimeMin = Integer.parseInt( endTimeMinField.getText().toString() );

        //Contact
        final EditText contactField = (EditText) findViewById(R.id.contact);
        String contact = contactField.getText().toString();

        //TODO: SEND TO SERVER

        JSONObject new_event = new JSONObject();
        try {
            new_event.put("title", title);
            new_event.put("location", location);
            new_event.put("description", description);
            new_event.put("contact", contact);
            new_event.put("category", evenType);
            Calendar c = GregorianCalendar.getInstance();
            c.set(Calendar.YEAR, dateYear);
            c.set(Calendar.MONTH, dateMonth - 1); // 11 = december
            c.set(Calendar.DAY_OF_MONTH, dateDay); // new years eve
            c.set(Calendar.HOUR, startTimeHour);
            c.set(Calendar.MINUTE, startTimeMin);
            long start = c.getTimeInMillis();
            long start_sec = (long) start/1000;
            c.set(Calendar.HOUR, endTimeHour);
            c.set(Calendar.MINUTE, endTimeMin);
            long end = c.getTimeInMillis();
            long end_sec = (long) end/1000;
            new_event.put("start", start_sec);
            new_event.put("end", end_sec);
            parseApp.addNewEvent(new_event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        makeToast(this, "Even has been successfully created and added to events pool.");
        finish();
    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }
}
