package com.team_scream_mit.scream;

/**
 * Created by danamukusheva on 9/19/15.
 */

import android.app.Application;
import android.os.AsyncTask;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "pP3u9MjdKTh8xGTZLp5DFnaHtPqJNMovWp34ZCsR", "mAK6zP3JD8D8z9EbFsOK8DcW1zA2xOr9FL7XmnxH");

        //getMITEvents();
    }


    public void addNewUser(String name, String email)
    {
        /*
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");

        */
        ParseObject new_user = new ParseObject("users");
        new_user.put("name", name);
        new_user.put("email", email);
        new_user.saveInBackground();
    }


    public void addNewEvent(JSONObject event){
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
            new_event.put("start", event.getInt("start"));
            new_event.put("end", event.getInt("end"));
            new_event.put("category", event.getString("category"));
            new_event.put("contact", event.getString("contact"));
            new_event.saveInBackground();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new_event.saveInBackground();
    }

    private void getMITEvents(){
        String url = "http://m.mit.edu/apis/calendars/events_calendar/events";
        new SendGETTask().execute(url);
    }

    private void addMITEvents(){

    }
    /*
     Copied from http://www.journaldev.com/7148/java-httpurlconnection-example-to-send-http-getpost-requests
     */
    private static StringBuffer sendGET(String url) throws IOException {
        String user_agent = "Mozilla/5.0";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", user_agent);
        int responseCode = con.getResponseCode();

        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // print result
            return response;
        } else {
            System.out.println("GET request not worked");
            return null;
        }


    }

    class SendGETTask extends AsyncTask<String, Void, StringBuffer> {

        protected StringBuffer doInBackground(String... urls) {
            try {
                StringBuffer response = sendGET(urls[0]);
                return response;
            }
            catch(IOException e){
                System.err.println("IOException: " + e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(StringBuffer response) {
            // check if null
            if (response == null){
                System.err.print("The response was either empty or there was an error");
                return;
            }

        }
    }



}
