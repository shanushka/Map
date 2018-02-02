package com.anushka.myapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.anushka.myapp.volley.VolleySingleton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private Location nLocation;
    double sourcelatitudes;
    double sourcelongitudes;
    double destinationLatitudes;
    double destinationLongitudes;
    EditText locationSearch;
    Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationSearch = (EditText) findViewById(R.id.editText);
        search = (Button) findViewById(R.id.search_button);
        gpsTracker = new GPSTracker(getApplicationContext());
        initMap();


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMapSearch();
            }
        });


    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        nLocation = gpsTracker.getLocation();
        if (nLocation != null) {
            sourcelatitudes = nLocation.getLatitude();
            sourcelongitudes = nLocation.getLongitude();
        }
        source = new LatLng(sourcelatitudes, sourcelongitudes);
    }

    List<android.location.Address> addressList = null;

    public void onMapSearch() {

        String location = locationSearch.getText().toString();

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            destinationLatitudes = address.getLatitude();
            destinationLongitudes = address.getLongitude();
            destination = new LatLng(destinationLatitudes, destinationLongitudes);
            mMap.addMarker(new MarkerOptions().position(destination));
            mMap.addMarker(new MarkerOptions().position(source));
        }
        drawRoute();
    }

    LatLng source;
    LatLng destination;

    private void drawRoute() {
        String url = makeURL(27.7056087, 85.3366977, destination.latitude, destination.longitude);
        apiCall(url);

    }


    Marker marker2;
    ArrayList<LatLng> markersValue = new ArrayList<>();

    public void animateCamera(LatLng latLng) {


        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng mv : markersValue) {
            b.include(mv);
        }
        LatLngBounds bounds = b.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, pxToDp());
        mMap.moveCamera(cu);


    }

    private int pxToDp() {
        Resources r = getResources();
        float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, r.getDisplayMetrics());
        return (Math.round(margin));

    }

    int[] counts = {100, 500, 299, 50, 150};
    int count = 0;

    public void drawPath(JSONObject json) {

        try {
            JSONArray routeArray = json.getJSONArray("routes");

            for (int i = 0; i < routeArray.length(); i++) {
                count = counts[i];
                JSONObject routes = routeArray.getJSONObject(i);
                JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                String encodedString = overviewPolylines.getString("points");
                List<LatLng> list = decodePoly(encodedString);
                drawPolyLine(list);
            }

        } catch (JSONException e) {

        }


    }

    public void drawPolyLine(List<LatLng> list) {
        PolylineOptions options;
        PolylineOptions options1;
        if (count > 0 && count < 200) {
            options = new PolylineOptions()
                    .addAll(list).color(Color.YELLOW).width(12).geodesic(true);

            options1 = new PolylineOptions()
                    .addAll(list).color(Color.BLACK).width(14).geodesic(true);
        } else if (count > 200 && count < 300) {
            options = new PolylineOptions()
                    .addAll(list).color(Color.BLUE).width(12).geodesic(true);

            options1 = new PolylineOptions()
                    .addAll(list).color(Color.YELLOW).width(14).geodesic(true);
        } else {
            options = new PolylineOptions()
                    .addAll(list).color(Color.RED).width(12).geodesic(true);

            options1 = new PolylineOptions()
                    .addAll(list).color(Color.GREEN).width(14).geodesic(true);
        }


        mMap.addPolyline(options1);
        mMap.addPolyline(options);
        markersValue.add(list.get(list.size() - 1));


        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 18));
    }


    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + Double.toString(sourcelat) + "," + Double.toString(sourcelog) +
                "&destination=" + Double.toString(destlat) + "," + Double.toString(destlog) +
                "&sensor=false" +
                "&mode=walking" +
                "&alternatives=true";

        return url;
    }


    public List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void apiCall(String url) {

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        drawPath(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance().getQueue().add(req);
    }
}
