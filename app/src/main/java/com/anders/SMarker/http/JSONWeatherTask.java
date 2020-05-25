package com.anders.SMarker.http;

import android.os.AsyncTask;

import com.anders.SMarker.data.JSONWeatherParser;

import org.json.JSONException;

import java.util.ArrayList;

public class JSONWeatherTask extends AsyncTask<String, Void, ArrayList> {
    private String values;
    public JSONWeatherTask(String  values) {
        this.values = values;

    }
    @Override
    protected ArrayList doInBackground(String... params) {

        ArrayList arrayList = new ArrayList();
        String data = ( (new WeatherHttpClient()).getWeatherData(values));

        try {
            arrayList = JSONWeatherParser.getWeather(data);
            arrayList.remove(0);
            arrayList.remove(0);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;

    }




    @Override
    protected void onPostExecute(ArrayList weather) {
        super.onPostExecute(weather);



    }

}
