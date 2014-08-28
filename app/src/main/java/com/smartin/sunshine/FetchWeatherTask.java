package com.smartin.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shaunmartin on 8/9/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LogTag = FetchWeatherTask.class.getSimpleName();
    private final String OutputModeParam = "mode";
    private final String UnitsParam = "units";
    private final String CountParam = "cnt";
    private final String LocationQueryParam = "q";


    protected String[] doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            Uri builtUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?")
                    .buildUpon()
                    .appendQueryParameter(LocationQueryParam, params[0])
                    .appendQueryParameter(OutputModeParam, "json")
                    .appendQueryParameter(UnitsParam, "metric")
                    .appendQueryParameter(CountParam, "7")
                    .build();

            // Create the request to OpenWeatherMap, and open the connection
            URL url = new URL(builtUri.toString());

            Log.v(LogTag, "Uri: " + builtUri.toString());
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();

            Log.v(LogTag, "Forecast JSON: " + forecastJsonStr);

        } catch (IOException e) {
            Log.e(LogTag, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LogTag, "Error closing stream", e);
                }
            }
        }
        try {
            return new ForecastOutputFormatter().getWeatherDataFromJson(forecastJsonStr, 7);
        }
        catch (JSONException je)
        {
            return null;
        }
    }
}
