package com.anders.SMarker.data;

import com.anders.SMarker.model.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class JSONWeatherParser {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Calendar cal = Calendar.getInstance();

    public static ArrayList getWeather(String data) throws JSONException  {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        ArrayList arrayList = new ArrayList();

        //getting the list node from the json
        JSONArray list=jObj.getJSONArray("list");

        // Now iterate through each one creating our data structure and grabbing the info we need
        for(int i=0;i<list.length();i++)
        {
            // Create a new instance of our
            Weather dtEntry = new Weather();

            // Get the dateTime object
            JSONObject dtItem = list.getJSONObject(i);
            String mainstr = dtItem.getString("main");
            String windstr = dtItem.getString("wind");
            JSONObject mainObject = new JSONObject(mainstr);
            JSONObject windObject = new JSONObject(windstr);

            String temperature = mainObject.getString("temp");
            if(temperature.length()>0) {
                double tmp_d = Double.parseDouble(temperature);
                double temp = Math.round(tmp_d);

                temperature = Double.toString(temp);
            }

            // pull out the date and put it in our own data
            dtEntry.temperature = temperature;
            dtEntry.humidity = mainObject.getString("humidity");
            dtEntry.start_date = dtItem.getString("dt_txt");
            dtEntry.time = dtItem.getString("dt_txt").substring(11,16);
            String date = dtItem.getString("dt_txt");
            try {
                Date dt_txt = dateFormat.parse(date);
                cal.setTime(dt_txt);
                cal.add(Calendar.HOUR, 3);

                dtEntry.end_date = dateFormat.format(cal.getTime());

            } catch (ParseException e) {
                e.printStackTrace();
            }


            dtEntry.wind_speed = windObject.getString("speed");
            dtEntry.wind_deg = windObject.getString("deg");
            // Now go for the weather object
            JSONArray weatherArray = dtItem.getJSONArray("weather");
            JSONObject ob = (JSONObject) weatherArray.get(0);

            // Grab what we are interested in

            dtEntry.icon = ob.getString("icon");

            arrayList.add(dtEntry);
        }

        return arrayList;
    }


    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}
