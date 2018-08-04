package com.example.nikhil.stormy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikhil.stormy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener{
    public  static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private ImageView iconImageView;
    double latitude = 37.8267;
    double longitude = -122.4233;
    String city = "London";
    LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCurrLocation();
        //getForecast(latitude,longitude,city);
        Log.d(TAG,"Main UI Code is running, horray!!!");
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
    }

    private void getForecast(double latitude,double longitude,String city) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        TextView darkSky = (TextView) findViewById(R.id.darkSkyAttribution);
        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        iconImageView = (ImageView) findViewById(R.id.iconImageView);

        String apiKey = "37551ce9206b3a6a5f65632e2e61f7c1";


        String forecastURL = "https://api.darksky.net/forecast/"+apiKey+"/"+latitude+","+longitude;
        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastURL).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            currentWeather=getCurrentDetails(jsonData);

                            final CurrentWeather displayWeather = new CurrentWeather(currentWeather.getLocationLabel(),currentWeather.getIcon(),currentWeather.getTime(),currentWeather.getTemperature(),currentWeather.getHumidity(),currentWeather.getPrecipChance(),currentWeather.getSummary(),currentWeather.getTimeZone());
                            binding.setWeather(displayWeather);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    iconImageView.setImageDrawable(drawable);
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception Caught", e);
                    } catch (JSONException e){
                        Log.e(TAG,"JSON Exception Caught",e);
                    }
                }
            });
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG,"From JSON: " + timezone);
        JSONObject currently = forecast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel(city);
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);
        Log.d(TAG,currentWeather.getFormattedTime());
        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo!=null && networkInfo.isConnected())
        {
            isAvailable=true;
        }
        else
        {
            Toast.makeText(this, R.string.network_unavailable_message,Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragement dialog = new AlertDialogFragement();
        dialog.show(getFragmentManager(),"error_dialog");
    }
    public void refreshOnClick(View view){
        Toast.makeText(this,"Refreshing Data",Toast.LENGTH_LONG).show();
        getCurrLocation();
    }
    void getCurrLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }
    public void onLocationChanged(Location location) {


        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            latitude=location.getLatitude();
            longitude=location.getLongitude();
            String city1 = addresses.get(0).getAddressLine(0);
            Log.d(TAG,city1);
            StringTokenizer stringTokenizer = new StringTokenizer(city1,",");
            int l=stringTokenizer.countTokens();
            for (int i=0;i<l-2;i++)
            {
                city1=stringTokenizer.nextToken();
            }
            city=stringTokenizer.nextToken();
            StringTokenizer stringTokenizer1=new StringTokenizer(city);
            city=stringTokenizer1.nextToken();
            city=city1+", "+city;
            getForecast(latitude,longitude,city);
        }catch(Exception e)
        {

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }
}
