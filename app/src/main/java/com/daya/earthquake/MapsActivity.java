package com.daya.earthquake;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.daya.earthquake.Model.EarthQuake;
import com.daya.earthquake.UI.CustomInfoWindow;
import com.daya.earthquake.Util.Constants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
  //  private BitmapDescriptor[] iconColors;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        queue = Volley.newRequestQueue(this);
        getEarthQuakes();
    }

    private void getEarthQuakes() {
        final EarthQuake earthQuake = new EarthQuake();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET
                , Constants.URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray features = response.getJSONArray("features");

                    for (int i = 0;i <= Constants.LIMIT;i++){
                        JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                        JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                        JSONArray cooridnates = geometry.getJSONArray("coordinates");

                        Double longitude = cooridnates.getDouble(0);
                        Double latitude = cooridnates.getDouble(1);
                        earthQuake.setPlace(properties.getString("place"));
                        earthQuake.setMagnitude(properties.getDouble("mag"));
                        earthQuake.setDetailLink(properties.getString("detail"));
                        earthQuake.setType(properties.getString("type"));
                        earthQuake.setLat(latitude);
                        earthQuake.setLon(longitude);

                        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                        String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());
                        //earthQuake.setTime(formattedDate);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        markerOptions.position(new LatLng(latitude,longitude));
                        markerOptions.title(earthQuake.getPlace());
                        markerOptions.snippet("Magnitude: "+
                                earthQuake.getMagnitude()+"\nDate: "+formattedDate);

                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(earthQuake.getDetailLink());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),3));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

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
        if(Build.VERSION.SDK_INT < 23){
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }catch (SecurityException e){

            }
        }
        else{
            if(ActivityCompat.checkSelfPermission(this
            , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .title("Hello"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,8));

            }

        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this
            ,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getQuakeDetails(marker.getTag().toString());

    }

    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                    String detailsurl = "";

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for (int i = 0;i <= geoserve.length();i++){
                        JSONObject geoserveObj = geoserve.getJSONObject(i);
                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");
                        detailsurl = geoJsonObj.getString("url");
                        getMoredetails(detailsurl);
                        Log.d("GetDetails", "onResponse: "+detailsurl);
                    }
                    getMoredetails(detailsurl);
                    Log.d("GetDetails", "onResponse: "+detailsurl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    public void getMoredetails(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                builder = new AlertDialog.Builder(MapsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.popup, null);
                Button dismiss = view.findViewById(R.id.dismisPopup);
                TextView popList = view.findViewById(R.id.popList);
                WebView webView = view.findViewById(R.id.htmlWebView);
                StringBuilder stringBuilder = new StringBuilder();
                //Log.d("GetMoreDetails", "onResponse: "+"IN more Details");
                try {
                    if (response.has("tectonicSummary") && response.getString("tectonicSummary")!=null){
                        JSONObject tectonic = response.getJSONObject("tectonicSummary");

                        if(tectonic.has("text") && tectonic.getString("text")!=null){
                            String text = tectonic.getString("text");
                            webView.loadDataWithBaseURL(null,text,"text/html","UTF-8",null);
                        }
                    }


                    JSONArray cities = response.getJSONArray("cities");
                    //Log.d("GetMoreDetails", "onResponse: "+"IN more Details");
                    for (int i = 0;i < cities.length();i++){
                        JSONObject citiesObj = cities.getJSONObject(i);
                        stringBuilder.append("City: " + citiesObj.getString("name")
                         + "\nDistance: " + citiesObj.getString("distance")
                         + "\nPopulation: " + citiesObj.getString("population"));
                        stringBuilder.append("\n\n\n");
                    }
                   // Log.d("GetMoreDetails", "onResponse: "+stringBuilder);
                    popList.setText(stringBuilder);
                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    builder.setView(view);
                    alertDialog = builder.create();
                    alertDialog.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
