package com.team_scream_mit.scream;

/**
 * Created by danamukusheva on 9/19/15.
 */

import android.app.Application;


import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class App extends Application
{
    protected String userName;
    protected String userEmail;

    @Override
    public void onCreate()
    {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "pP3u9MjdKTh8xGTZLp5DFnaHtPqJNMovWp34ZCsR", "mAK6zP3JD8D8z9EbFsOK8DcW1zA2xOr9FL7XmnxH");

    }


    protected void addNewUser(final String name, final String email)
    {
        ParseQuery query = new ParseQuery("users");
        query.whereEqualTo("email", email);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    System.err.println("Such user exists");
                } else {
                    // results have all the Posts the current user liked.
                    ParseObject new_user = new ParseObject("users");
                    new_user.put("name", name);
                    new_user.put("email", email);
                    new_user.saveInBackground();
                }
            }
        });

    }


    protected void addNewEvent(JSONObject event){
        final ParseObject new_event = new ParseObject("events");

        // fields that are not required
        try {
            new_event.put("url", event.getString("url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            new_event.put("description", event.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            //required fields
            new_event.put("title", event.getString("title"));
            new_event.put("location", event.getString("title"));
            new_event.put("start", event.getLong("start"));
            new_event.put("end", event.getLong("end"));
            new_event.put("category", event.getString("category"));
            new_event.put("contact", event.getString("contact"));
            new_event.saveInBackground();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new_event.saveInBackground();
    }


    protected void getAllEvents(String user_email,
                             final ArrayList<String> categories,
                             final int days_from_today,
                             final double time_range_from,
                             final double time_range_to,
                             final CallbackInterface callback){

        ParseQuery query = new ParseQuery("users");
        query.whereEqualTo("email", user_email);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject user, ParseException e) {
                if (user != null && e == null) {
                    //Get the list of events that already have been added
                    ArrayList<String> added_events = (ArrayList<String>) user.get("added_events");


                    long hours_to_sec = 60 * 60;
                    long days_to_sec = 24 * hours_to_sec;

                    ParseQuery query = new ParseQuery("events");

                    Calendar c = new GregorianCalendar();
                    c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    Date today = c.getTime(); //the midnight, that's the first second of the day.
                    long date_range_from_today = today.getTime(); //IN SECONDS
                    double from = date_range_from_today + days_from_today * days_to_sec + time_range_from * hours_to_sec;
                    double to = date_range_from_today + days_from_today * days_to_sec + time_range_to * hours_to_sec;

                    query.whereContainedIn("category", categories);
                    query.whereNotContainedIn("objectId", added_events);
                    query.whereLessThanOrEqualTo("end", to);
                    query.whereGreaterThanOrEqualTo("start", from);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            if (e != null) {
                                System.err.println("ParseException: " + e.getMessage());
                            } else {
                                // results have all the Posts the current user liked.
                                callback.onFindEventsFinished(list);
                            }
                        }
                    });

                } else {
                    System.err.println("ParseException while fetching a user: " + e.getMessage());
                }

            }

        });

    }


    protected void addSavedEvent(String user_email, final String eventId){

        ParseQuery query = new ParseQuery("users");
        query.whereEqualTo("email", user_email);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject user, ParseException e) {
                if (e==null){
                    user.addUnique("added_events", eventId);
                    user.saveInBackground();
                }
                else{
                    System.err.println("ParseException: " + e.getMessage());
                }
            }
        });
    }


}

//define callback interface
interface CallbackInterface {

    void onFindEventsFinished(List<ParseObject> events);
}
