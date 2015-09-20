package com.team_scream_mit.scream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.google.android.gms.plus.Plus;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.Calendar;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.parse.ParseObject;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // progress dialog while we load the data from the server
    private ProgressDialog progress;

    // Use SharedPreferences to access user's saved preferences.
    private SharedPreferences preferenceSettings;
    private static final int PREFERENCE_MODE_PRIVATE = 0;

    // use App to load the events data from the server
    private App parseApp;

    // need user email
    private String userEmail;

    // 5, 9 and 9 are default values
    private int daysFromToday = 7;
    private double timeRangeFrom = 9.00;
    private double timeRangeTo = 21.00;
    private ArrayList<String> searchCategories;

    // Array of events
    private ArrayList<String> eventsArray;
    // eventsArray[i] has id eventIds[i]
    private ArrayList<String> eventIds;
    private ArrayList<String> eventTitles;
    private ArrayList<String> eventDescriptions;
    private ArrayList<Long> eventStartTimes;
    private ArrayList<Long> eventEndTimes;
    private ArrayList<String> eventLocations;
    // keep track of the last removed event's id
    private String lastRemovedEventId;
    private String lastRemovedEventTitle;
    private String lastRemovedEventDesc;
    private Long lastRemovedEventStartTime;
    private Long lastRemovedEventEndTime;
    private String lastRemovedEventLocation;

    // Adapter is needed for swiping.
    private ArrayAdapter<String> arrayAdapter;
    SwipeFlingAdapterView flingContainer;

    // Buttons alternative to swiping
    private ImageButton like_button;
    private ImageButton dislike_button;

    // Log message for debugging
    private String LOG_MESSAGE = "-KAIRAT-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.icon_label);

        showLoadingDialog();

        preferenceSettings = getPreferences(PREFERENCE_MODE_PRIVATE);

        searchCategories = getDefaultCategories();
        useSavedPreferencesIfAvailable();

        // dummy value for now.


        like_button = (ImageButton) findViewById(R.id.like_button);
        like_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rightSwipe();
            }
        });
        dislike_button = (ImageButton) findViewById(R.id.dislike_button);
        dislike_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                leftSwipe();
            }
        });

        // initialize the variables
        eventsArray = new ArrayList<>();
        eventIds = new ArrayList<>();
        eventTitles = new ArrayList<>();
        eventDescriptions = new ArrayList<>();
        eventStartTimes = new ArrayList<>();
        eventEndTimes = new ArrayList<>();
        eventLocations = new ArrayList<>();

        parseApp = (App) getApplication();
        userEmail = parseApp.userEmail;
        CallbackInterface callbackHandler = new DataReception();
        parseApp.getAllEvents(userEmail, searchCategories, daysFromToday, timeRangeFrom, timeRangeTo, callbackHandler);

    }

    // Toast (for debugging purposes)
    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }


    /**
     * Called when right swipe button clicked
     */
    public void rightSwipe() {
        flingContainer.getTopCardListener().selectRight();
    }

    /**
     * Called when left swipe button clicked
     */
    public void leftSwipe() {
        flingContainer.getTopCardListener().selectLeft();
    }

    private void loadCards(){
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.card, R.id.event_title_id, eventsArray);
        //Log.i(LOG_MESSAGE, arrayAdapter.toString());

        flingContainer.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d(LOG_MESSAGE, "remove handler");
                // delete an object from the Adapter (/AdapterView)
                eventsArray.remove(0);
                lastRemovedEventId = eventIds.get(0);
                lastRemovedEventTitle = eventTitles.get(0);
                lastRemovedEventDesc = eventDescriptions.get(0);
                lastRemovedEventStartTime = eventStartTimes.get(0);
                lastRemovedEventEndTime = eventEndTimes.get(0);
                lastRemovedEventLocation = eventLocations.get(0);
                Log.d(LOG_MESSAGE, lastRemovedEventId + " - before");
                eventIds.remove(0);
                eventTitles.remove(0);
                eventDescriptions.remove(0);
                eventStartTimes.remove(0);
                eventEndTimes.remove(0);
                eventLocations.remove(0);
                arrayAdapter.notifyDataSetChanged();
                Log.d(LOG_MESSAGE, lastRemovedEventId + " - after");
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                //makeToast(MainActivity.this, "Left!");
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Log.d(LOG_MESSAGE, "Right swipe handler");
                // last removed event id should not null
                if (lastRemovedEventId != null) {
                    addEventToCalendar(lastRemovedEventTitle, lastRemovedEventDesc,
                            lastRemovedEventLocation, lastRemovedEventStartTime, lastRemovedEventEndTime);
                    parseApp.addSavedEvent(userEmail, lastRemovedEventId);
                    //makeToast(MainActivity.this, "Event added to calendar!");
                } else {
                    Log.e(LOG_MESSAGE, "Last card id is null. NOT SUPPOSED TO HAPPEN!!!");
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // When user runs out of cards, show loading dialog and load new data from the server
                if (itemsInAdapter == 0) {
                    showLoadingDialog();
                    CallbackInterface callbackHandler = new DataReception();
                    parseApp.getAllEvents(userEmail, searchCategories, daysFromToday, timeRangeFrom, timeRangeTo, callbackHandler);
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                //View view = flingContainer.getSelectedView();
                //view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                //view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                // we can show a new dialog with the description of the event
                // (if event has description)
                makeToast(MainActivity.this, "Clicked!");
            }
        });

    }

    public void addEventToCalendar(String title, String description, String location, long startTime, long endTime) {

        // Signin button clicked
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");


        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,endTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        intent.putExtra(Events.TITLE, title);
        intent.putExtra(Events.DESCRIPTION, description);
        intent.putExtra(Events.EVENT_LOCATION, location);
        //intent.putExtra(Events.RRULE, "FREQ=ONCE");

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.about){
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.logout){
            if (parseApp.mGoogleApiClient.isConnected()) {
                Log.e(LOG_MESSAGE, "heloohoooo");
                Plus.AccountApi.clearDefaultAccount(parseApp.mGoogleApiClient);
                parseApp.mGoogleApiClient.disconnect();
                parseApp.mGoogleApiClient.connect();
                parseApp.clearUserName(this);
            }

            parseApp.clearUserName(this);
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.main_screen){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @return default categories - all
     */
    private ArrayList<String> getDefaultCategories(){
        String[] cats = {"Study breaks", "Concerts", "Parties", "Info Sessions", "Seminars"};
        return new ArrayList<String>(Arrays.asList(cats));
    }


    /**
     *
     * @param string_date format - yyyy-MM-dd'T'HH:mm:ss
     * @return Date object
     */
    private Date convertStringToDate(String string_date){
        Date converted_date = null;
        SimpleDateFormat simple_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Log.e(LOG_MESSAGE, "simple_date is null");

        try {
            converted_date = simple_date.parse(string_date);
        } catch (ParseException e) {
            Log.e(LOG_MESSAGE, string_date);
            e.printStackTrace();
        }

        if (converted_date == null){
            Log.e(LOG_MESSAGE, "Could not convert string to date object: " + string_date);
        }
        return converted_date;
    }

    private void useSavedPreferencesIfAvailable(){
        int dft = preferenceSettings.getInt("daysFromToday", -1);
        int sth = preferenceSettings.getInt("startTimeHour", -1);
        int stm = preferenceSettings.getInt("startTimeMin", -1);
        int eth = preferenceSettings.getInt("endTimeHour", -1);
        int etm = preferenceSettings.getInt("endTimeMin", -1);
        Set<String> sc = preferenceSettings.getStringSet("selectedCategories", null);

        if (dft != -1){
            daysFromToday = dft;
        }
        if (sth != -1 && stm != -1){
            timeRangeFrom = sth + stm*1.0/60;
        }
        if (eth != -1 && etm != -1){
            timeRangeTo = eth + etm*1.0/60;
        }
        if (sc != null){
            searchCategories = new ArrayList<>(sc);
        }
    }

    private class DataReception implements CallbackInterface{
        @Override
        public void onFindEventsFinished(List<ParseObject> events) {
            String eventText;
            if (events.size() == 0){
                progress.dismiss();
                like_button.setEnabled(false);
                dislike_button.setEnabled(false);
                showAlertDialog();
            } else {
                like_button.setEnabled(true);
                dislike_button.setEnabled(true);
                for (int i = 0; i < events.size(); i++) {
                    ParseObject entry = events.get(i);
                    String eventTitle = entry.getString("title");
                    long eventStartInSec = entry.getLong("start");
                    long eventEndInSec = entry.getLong("end");
                    String eventLocation = entry.getString("location");
                    String eventId = entry.getObjectId();
                    Date eventStartDate = new Date(eventStartInSec * 1000);
                    Date eventEndDate = new Date(eventEndInSec * 1000);
                    String formattedDate = getFormattedDate(eventStartDate, eventEndDate);

                    eventText = eventTitle + "\n" + formattedDate + ",\n" + eventLocation;

                    eventsArray.add(eventText);
                    eventIds.add(eventId);
                    eventTitles.add(eventTitle);
                    eventDescriptions.add((entry.getString("description")));
                    eventStartTimes.add(eventStartInSec*1000);
                    eventEndTimes.add(eventEndInSec*1000);
                    eventLocations.add(eventLocation);
                }
                progress.dismiss();
                //Log.i(LOG_MESSAGE, eventsArray.toString());
                Log.d(LOG_MESSAGE, eventIds.toString());
                loadCards();
            }
        }
    }

    private String getFormattedDate(Date start_date, Date end_date){
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal.setTime(start_date);
        cal2.setTime(end_date);
        int monthNum = cal.get(Calendar.MONTH);
        String yearNum = Integer.toString(cal.get(Calendar.YEAR));
        String dayNum = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String weekDayName = getWeekdayName(cal.get(Calendar.DAY_OF_WEEK));
        String start_hourNum = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        String end_hourNum = Integer.toString(cal2.get(Calendar.HOUR_OF_DAY));
        String start_minuteNum = Integer.toString(cal.get(Calendar.MINUTE));
        if (start_minuteNum.length() == 1){
            start_minuteNum = "0" + start_minuteNum;
        }
        String end_minuteNum = Integer.toString(cal2.get(Calendar.MINUTE));
        if (end_minuteNum.length() == 1){
            end_minuteNum = "0" + end_minuteNum;
        }
        String monthName = getMonthName(monthNum);

        String formattedDate = weekDayName + ", " + monthName + " " + dayNum
                + ", " + start_hourNum + ":" + start_minuteNum + " - "
                + end_hourNum + ":" + end_minuteNum;

        return formattedDate;
    }

    /**
     *
     * @param monthNum number of the month in a year [0-11]
     * @return name of the month
     */
    private String getMonthName(int monthNum){
        String monthName = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (monthNum >= 0 && monthNum <= 11){
            monthName = months[monthNum];
        } else {
            Log.e(LOG_MESSAGE, "Wrong date number: " + monthNum);
        }
        return monthName;
    }

    /**
     *
     * @param weekdayNum number of the day in the week [0-6]
     * @return name of the day of the week
     */
    private String getWeekdayName(int weekdayNum){
        String weekdayName = "Wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] weekdays = dfs.getWeekdays();
        if (weekdayNum >= 1 && weekdayNum <= 7){
            weekdayName = weekdays[weekdayNum];
        } else {
            Log.e(LOG_MESSAGE, "Wrong weekday number: " + weekdayNum);
        }
        return weekdayName;
    }

    private void showLoadingDialog(){
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
    }

    private void showAlertDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("No events!");
        alertDialog.setMessage("There are no events in the provided interval");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    ///////////////////////////////// OLD CODE /////////////////////////////////////////

    /*


    private ArrayList<String> event_titles;
    private ArrayList<String> event_locations;
    private ArrayList<String> event_times;
    private int event_index;
    private Date event_start_time;
    private Date event_end_time;
    private String time_range = "?start=2015-09-19&end=2015-09-25";
    private ArrayAdapter<String> arrayAdapter;

    SwipeFlingAdapterView flingContainer;
    private ImageButton like_button;
    private ImageButton dislike_button;

    private String LOG_MESSAGE = "-KAIRAT-";
    private String serviceAPI = "http://m.mit.edu/apis/calendars/events_calendar/events/";
    private String apiUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        like_button = (ImageButton) findViewById(R.id.like_button);
        like_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                right();
            }
        });
        dislike_button = (ImageButton) findViewById(R.id.dislike_button);
        dislike_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                left();
            }
        });

        // Default start and end values of the date
        event_start_time = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 10);
        event_end_time = c.getTime();

        // If new preferences have been applied.
        Intent intent = getIntent();
        // time_range should be formatted properly i.e. ?start=2013-02-23&end=2013-02-23
        if (intent.getExtras() != null){
            time_range = intent.getStringExtra("time_range");
            // format of the string is the following - yyyy-MM-dd'T'HH:mm:ss
            String start_string = intent.getStringExtra("start_time");
            String end_string = intent.getStringExtra("end_time");
            event_start_time = convertStringToDate(start_string);
            event_end_time = convertStringToDate(end_string);
        }

        getEventsData(time_range);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.card, R.id.event_title_id, event_titles);


        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                event_titles.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                makeToast(MainActivity.this, "Left!");
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                makeToast(MainActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                //event_titles.add("XML ".concat(String.valueOf(i)));
                //arrayAdapter.notifyDataSetChanged();
                //Log.d("LIST", "notified");
                //i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                //view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                //view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                makeToast(MainActivity.this, "Clicked!");
            }
        });

    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }


   // @OnClick(R.id.right)
    public void right() {

    flingContainer.getTopCardListener().selectRight();
}

    //@OnClick(R.id.left)
    public void left() {
        flingContainer.getTopCardListener().selectLeft();
    }


*/



    ////////////////////////////////////////////////////////////////////////////////
    /*
    private void getEventsData(String time_range){
        // If we really get input from the user, we'll need to URL encode it before including it
        // in an HTTP request string
        String encodedInput = null;

        try {
            encodedInput = URLEncoder.encode(time_range, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_MESSAGE, "Encoding exception");
            e.printStackTrace();
        }

        if (encodedInput != null) {
            apiUrl = serviceAPI + time_range;
            new CallAPI().execute(apiUrl);
        }
    }

    private class CallAPI extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0]; // URL to call
            Log.i(LOG_MESSAGE, urlString);

            HttpURLConnection urlConnection = null;

            InputStream in = null;
            StringBuilder sb = new StringBuilder();

            char[] buf = new char[1000000];

            // do the HTTP Get
            try {
                Log.i(LOG_MESSAGE, "entered try");
                URL url = new URL(urlString);
                Log.i(LOG_MESSAGE, "created URL object");
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.i(LOG_MESSAGE, "opened url connection");
                InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());

                Log.i(LOG_MESSAGE, "got input stream");

                int read;
                while ((read = reader.read(buf)) != -1) {
                    sb.append(buf, 0, read);
                }
            } catch (Exception e) {
                // if any I/O error occurs
                Log.i(LOG_MESSAGE, "caught exception.");
                e.printStackTrace();
            } finally {
                Log.i(LOG_MESSAGE, "Entered finally");
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    // releases any system resources associated with the stream
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    Log.i(LOG_MESSAGE + " Error:", e.getMessage());
                }
            }
            Log.i(LOG_MESSAGE, "Finished reading");
            Log.i(LOG_MESSAGE, "LINE:257## " + sb.toString());

            return sb.toString();
        }


        protected void onPostExecute(String result) {
            Log.i(LOG_MESSAGE, "starting onPostExecute");
            Log.i(LOG_MESSAGE, result);

            JSONArray eventEntries = null;

            // separate this out so people can work on it.
            try {
                eventEntries = new JSONArray(result);

            } catch (JSONException e) {
                Log.e(LOG_MESSAGE, "Could not find hits entry in JSON result");
                Log.i(LOG_MESSAGE, e.getMessage());
            }

            if (eventEntries != null) {
                fillEventArrays(eventEntries);
            }
        }
    } // end CallAPI

    private void fillEventArrays(JSONArray eventEntries){
        event_titles = new ArrayList<>();
        event_times = new ArrayList<>();
        event_locations = new ArrayList<>();
        event_index = 0;

        // just for testing
        event_titles.add("Family Day at the List: Imaginative Alphabets Workshop");
        event_times.add("12:00p–4:00p, September 19, 2015");
        event_locations.add("Building: E15");

        Log.i(LOG_MESSAGE, "default value has been added to the arrays of events.");

        String event_title;
        String event_time;
        String event_location;

        if (eventEntries != null){
            for (int i = 0; i < eventEntries.length(); i++){
                try {
                    JSONObject eventInfo = (JSONObject) eventEntries.get(i);
                    event_title = eventInfo.getString("title");
                    JSONObject locationInfo = eventInfo.getJSONObject("location");
                    try {
                        event_location = locationInfo.getString("description") + ", ";
                    } catch (Exception e){
                        event_location = "";
                    }

                    try {
                        event_location += locationInfo.getString("room_number");
                    } catch (Exception e){

                    }

                    String start_at = eventInfo.getString("start_at").substring(0, 19);
                    String end_at = eventInfo.getString("end_at").substring(0, 19);
                    Date start_at_date = convertStringToDate(start_at);
                    Date end_at_date = convertStringToDate(end_at);

                    if (start_at_date.after(event_start_time) && end_at_date.before(event_end_time)){
                        event_titles.add(event_title);
                        event_locations.add(event_location);
                        event_times.add(getFormattedDate(start_at_date, end_at_date));
                    }
                } catch (JSONException e){
                    Log.e(LOG_MESSAGE, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    */

    /**
     *
     * @param string_date format - yyyy-MM-dd'T'HH:mm:ss
     * @return Date object
     */
    /*
    private Date convertStringToDate(String string_date){
        Date converted_date = null;
        SimpleDateFormat simple_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (simple_date == null){
            Log.e(LOG_MESSAGE, "simple_date is null");
        }

        try {
            converted_date = simple_date.parse(string_date);
        } catch (ParseException e) {
            Log.e(LOG_MESSAGE, string_date);
            e.printStackTrace();
        }

        if (converted_date == null){
            Log.e(LOG_MESSAGE, "Could not convert string to date object: " + string_date);
        }
        return converted_date;
    }

    private String getFormattedDate(Date start_date, Date end_date){
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal.setTime(start_date);
        cal2.setTime(end_date);
        int monthNum = cal.get(Calendar.MONTH);
        String yearNum = Integer.toString(cal.get(Calendar.YEAR));
        String dayNum = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        String weekDayName = getWeekdayName(cal.get(Calendar.DAY_OF_WEEK));
        String start_hourNum = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        String end_hourNum = Integer.toString(cal2.get(Calendar.HOUR_OF_DAY));
        String start_minuteNum = Integer.toString(cal.get(Calendar.MINUTE));
        if (start_minuteNum.length() == 1){
            start_minuteNum = "0" + start_minuteNum;
        }
        String end_minuteNum = Integer.toString(cal2.get(Calendar.MINUTE));
        if (end_minuteNum.length() == 1){
            end_minuteNum = "0" + end_minuteNum;
        }
        String monthName = getMonthName(monthNum);

        String formattedDate = weekDayName + ", " + monthName + " " + dayNum
                + ", " + start_hourNum + ":" + start_minuteNum + " - "
                + end_hourNum + ":" + end_minuteNum;

        return formattedDate;
    }

    private String getMonthName(int monthNum){
        String monthName = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (monthNum >= 0 && monthNum <= 11){
            monthName = months[monthNum];
        } else {
            Log.e(LOG_MESSAGE, "Wrong date number: " + monthNum);
        }
        return monthName;
    }

    private String getWeekdayName(int weekdayNum){
        String weekdayName = "Wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] weekdays = dfs.getWeekdays();
        if (weekdayNum >= 0 && weekdayNum <= 6){
            weekdayName = weekdays[weekdayNum];
        } else {
            Log.e(LOG_MESSAGE, "Wrong weekday number: " + weekdayNum);
        }
        return weekdayName;
    }
*/
}
