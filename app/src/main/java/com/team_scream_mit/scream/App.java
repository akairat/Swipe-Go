package com.team_scream_mit.scream;

/**
 * Created by danamukusheva on 9/19/15.
 */

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
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
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class App extends Application
{
    protected String userName;
    protected String userEmail;
    protected GoogleApiClient mGoogleApiClient;
    protected Boolean mGooglePlusLogoutClicked = false;
    static final String PREF_USER_NAME= "username";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static void clearUserName(Context ctx)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

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

        Log.d("-KAIRAT-", categories.toString());
        Log.d("-KAIRAT-", Integer.toString(days_from_today));
        Log.d("-KAIRAT-", Double.toString(time_range_from));
        Log.d("-KAIRAT-", Double.toString(time_range_to));

        ParseQuery query = new ParseQuery("users");
        query.whereEqualTo("email", user_email);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject user, ParseException e) {
                if (e != null) {
                    System.err.println("User wasn't found while fetching events");
                }
                if (user != null && e == null) {
                    //Get the list of events that already have been added
                    ArrayList<String> added_events = (ArrayList<String>) user.get("added_events");
                    Log.i("Dana tag", "GOT SOME USER");
                    Log.i("Dana tag", user.get("name").toString());

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
                    double from_d = (date_range_from_today/1000 + days_from_today * days_to_sec + time_range_from * hours_to_sec);
                    double to_d = (date_range_from_today/1000 + days_from_today * days_to_sec + time_range_to * hours_to_sec);
                    long from = (long) from_d;
                    long to = (long) to_d;

                    Log.d("-KAIRAT-", Long.toString(from));
                    Log.d("-KAIRAT-", Long.toString(to));

                    //query.whereContainedIn("category", categories);
                    query.whereNotContainedIn("objectId", added_events);
                    query.whereLessThanOrEqualTo("end", to);
                    query.whereGreaterThanOrEqualTo("start", from);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            if (e != null) {
                                System.err.println("Didn't find any events or error");
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

    /**
     * Sign-out from google
     * */
    public void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            clearUserName(this);

        }
    }

}

//define callback interface
interface CallbackInterface {

    void onFindEventsFinished(List<ParseObject> events);
}
