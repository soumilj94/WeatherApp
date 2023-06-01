package com.soumil.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    EditText search;
    TextView tempr, humidity, wndsped, whtrdesc;
    ProgressBar progBar;
    private ListView previousCitiesListView;

    private static final String LAST_SEARCHED_CITY_KEY = "last_searched_city";
    private static final String PREVIOUS_CITIES_KEY = "previous_cities";
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search = findViewById(R.id.searchBar);
        tempr = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        wndsped = findViewById(R.id.windSpeed);
        whtrdesc = findViewById(R.id.weatherDesc);
        progBar = findViewById(R.id.progressBar);
        previousCitiesListView = findViewById(R.id.previousCitiesListView);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prevSearches = sharedPreferences.getString(LAST_SEARCHED_CITY_KEY, "");
        search.setText(prevSearches);

        Set<String> previousCitiesSet = sharedPreferences.getStringSet(PREVIOUS_CITIES_KEY, new HashSet<>());
        List<String> previousCitiesList = new ArrayList<>(previousCitiesSet);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, previousCitiesList);
        previousCitiesListView.setAdapter(listAdapter);

    }
    public void get(View v){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }


        String apiKey = "1e8fbea5d3232aec06641082d28946fb";
        String getCityName = search.getText().toString();
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="+getCityName+"&appid=410365031b5f54bb274db690720270bb";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SEARCHED_CITY_KEY, getCityName);
        Set<String> previousCitiesSet = sharedPreferences.getStringSet(PREVIOUS_CITIES_KEY, new HashSet<>());
        previousCitiesSet.add(getCityName);
        editor.putStringSet(PREVIOUS_CITIES_KEY, previousCitiesSet);
        editor.apply();

        if (getCityName.isEmpty()){
            Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        progBar.setVisibility(View.VISIBLE);
        RequestQueue q = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject obj1 = response.getJSONObject("main");
                            JSONObject obj2 = response.getJSONObject("wind");

                            JSONArray obj3 = response.getJSONArray("weather");
                            JSONObject weatherObject = obj3.getJSONObject(0);


                            String apiTemp = obj1.getString("temp");
                            String apiHumid = obj1.getString("humidity");

                            String apiWndSpd = obj2.getString("speed");

                            String apiWeatherDesc = weatherObject.getString("description");

                            Double tempConvt = Double.parseDouble(apiTemp)-273.15;
                            tempr.setText(tempConvt.toString().substring(0,2)+" °c");

                            humidity.setText("Humidity "+apiHumid.toString().substring(0,2));

                            wndsped.setText("Wind Speed "+apiWndSpd.toString().substring(0,4));

                            whtrdesc.setText(apiWeatherDesc);

                            progBar.setVisibility(View.GONE);
                        }
                        catch (JSONException e) {
//                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        q.add(req);
    }
}