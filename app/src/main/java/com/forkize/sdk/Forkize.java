package com.forkize.sdk;

/*   Forkize main class   */

        import android.content.Context;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.util.DisplayMetrics;

        import org.apache.http.HttpRequest;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.entity.StringEntity;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.message.BasicHeader;
        import org.apache.http.protocol.HTTP;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.UnsupportedEncodingException;
        import java.net.URI;
        import java.net.URL;
        import java.util.Locale;
        import java.util.concurrent.ExecutionException;

public class Forkize {

    private JSONObject object;
    private JSONObject responseJSON;
    private Context context;
    private double latitude, longitude;

    public Forkize(Context applicationContext){
        context = applicationContext;
    }

    public void init(){

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        float density = displayMetrics.density;

        latitude = longitude = 500;

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        String type = "no connection";
        boolean iscon = (activeNetwork != null) && activeNetwork.isConnected();
        if (iscon){
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                type = "WiFi";
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                type = "3G";
        }

        object = new JSONObject();
        try {
            object.put("manufacturer", Build.MANUFACTURER);
            object.put("model", Build.MODEL);
            object.put("product", Build.PRODUCT);
            object.put("api", Build.VERSION.SDK_INT);
            object.put("display_width", width);
            object.put("display_height", height);
            object.put("density", density);
            if (longitude != 500 && latitude != 500){
                object.put("longitude", longitude);
                object.put("latitude", latitude);
            }
            if(iscon){
                object.put("connnectivity_type", type);
            }
            object.put("locale", Locale.getDefault().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void init(String token){}
    public void post() throws ExecutionException, InterruptedException {

        responseJSON = new Request().execute().get();
    }

    public String getString() {
        try {
            this.post();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String string = responseJSON.toString();

        return String.valueOf(string);
    }

    private class Request extends AsyncTask<String, String, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... params) {
            InputStream inputStream = null;

            JSONObject returnval = new JSONObject();

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("https://nodesrv-davkhech.c9.io/");

                StringEntity stringEntity = new StringEntity(object.toString());
                stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                httpPost.setEntity(stringEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                inputStream = httpResponse.getEntity().getContent();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (inputStream != null){
                try {
                    returnval.getJSONObject(inputStream.toString());
                    return returnval;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    returnval.put("status", "fail");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return returnval;
        }
    }
}