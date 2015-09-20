package com.team_scream_mit.scream;

import android.content.Context;
import android.content.Intent;
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

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private App parseApp;

    private ArrayList<String> searchCategories;

    private ArrayList<String> event_titles;
    private ArrayList<String> event_locations;
    private ArrayList<String> event_times;
    private int event_index;
    private Date event_start_time;
    private Date event_end_time;
    private ArrayAdapter<String> arrayAdapter;

    SwipeFlingAdapterView flingContainer;
    private ImageButton like_button;
    private ImageButton dislike_button;

    private String LOG_MESSAGE = "-KAIRAT-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        searchCategories = getDefaultCategories();

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

        // If new preferences have been applied.
        Intent intent = getIntent();
        // time_range should be formatted properly i.e. ?start=2013-02-23&end=2013-02-23
        if (intent.getExtras() != null){
            // format of the string is the following - yyyy-MM-dd'T'HH:mm:ss
            String start_string = intent.getStringExtra("start_time");
            String end_string = intent.getStringExtra("end_time");
            event_start_time = convertStringToDate(start_string);
            event_end_time = convertStringToDate(end_string);
        }


        String a = "<h1>Yelp Tech Talk</h1>\n<h2>Date/Time:</h2>\n" +
                "<p>Thursday, February 5, 2015 - 5:30pm</p>\n" +
                "<h2>Location:</h2>\n<p>Goodell Hall, 5th floor lounge</p>";
        Html.fromHtml(a);
        event_titles = new ArrayList<>();
        event_titles.add("");

        arrayAdapter = new ArrayAdapter<>(this, R.layout.card, R.id.event_title_id, event_titles);


        parseApp = (App) getApplication();
//        parseApp.getAllEvents(user_email,
//        final ArrayList<String> categories,
//        final int days_from_today,
//        final int time_range_from,
//        final int time_range_to,
//        final CallbackInterface callback);

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
    public void rightSwipe() {
        /**
         * Trigger the right event manually.
         */
        flingContainer.getTopCardListener().selectRight();
    }

    //@OnClick(R.id.left)
    public void leftSwipe() {
        flingContainer.getTopCardListener().selectLeft();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @return default categories - all
     */
    private ArrayList<String> getDefaultCategories(){
        String[] cats = {"Tech Talks", "Study Breaks", "Parties", "Info Sessions"};
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
