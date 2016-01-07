package com.trublo.bathroommaps.fragments;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.bathroommaps.BathroomMaps;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by julianlo on 1/6/16.
 */
public class BathroomMapFragment extends SupportMapFragment {
    private static final String TAG = "BathroomMapFragment";

    private static final int CLICKED_BATHROOM_ZOOM_LEVEL = 16;
    private static final int CENTER_MAP_ANIMATION_DURATION = 200;

    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private HashMap<String, Bathroom> mMarkerMap = new HashMap<>(); // maps marker to bathroom
    private HashMap<String, Bathroom> mBathroomsOnMap = new HashMap<>(); // maps bathroom id to bathroom for bathrooms on the map already
    private Marker mSelectedMarker;

    public static BathroomMapFragment newInstance() {
        return new BathroomMapFragment();
    }

    public interface Callbacks {
        void onFetchingBathrooms(boolean fetching);
        void onBathroomMarkerSelected(Bathroom bathroom, boolean selected);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // to keep markers on config change

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Connected to GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "Connected suspended to GoogleApiClient");
                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException se) {
                    Log.w(TAG, "Could not set my location enabled", se);
                }

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {

                        // Get the distance in meters between the corners of the map bounds.
                        // We'll use this as the distance to fetch bathrooms for.
                        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        float[] results = new float[1];
                        Location.distanceBetween(
                                bounds.northeast.latitude,
                                bounds.northeast.longitude,
                                bounds.southwest.latitude,
                                bounds.southwest.longitude,
                                results);
                        int distance = (int) results[0];

                        if (distance > 20 * 1000) {
                            Log.v(TAG, "Distance is " + distance + "m, skipping bathroom fetch");
                            return;
                        }
                        fetchBathrooms(cameraPosition.target.latitude, cameraPosition.target.longitude, distance);
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        clearSelectedBathroom();

                        Bathroom bathroom = mMarkerMap.get(marker.getId());
                        if (bathroom == null) {
                            Log.w(TAG, "Clicked marker '" + marker.getTitle() + "' not found in marker map");
                            return true;
                        }

                        Log.i(TAG, "Marker '" + marker.getTitle() + "' clicked");
                        setSelectedBathroom(bathroom);
                        centerMap(bathroom.getLatitude(), bathroom.getLongitude(), CLICKED_BATHROOM_ZOOM_LEVEL);
                        getCallbacks().onBathroomMarkerSelected(bathroom, true);
                        return true;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        clearSelectedBathroom();
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    public void fetchBathrooms(double latitude, double longitude, int distance) {
        new FetchBathroomsTask().execute(latitude, longitude, (double) distance);
    }

    private void centerMap(double latitude, double longitude, int zoom) {
        CameraPosition.Builder builder = CameraPosition.builder()
                .target(new LatLng(latitude, longitude))
                .zoom(zoom);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()), CENTER_MAP_ANIMATION_DURATION, null);
    }

    private void clearSelectedBathroom() {
        if (mSelectedMarker != null) {
            mSelectedMarker.remove();
            mSelectedMarker = null;
        }
    }

    private void setSelectedBathroom(Bathroom bathroom) {
        clearSelectedBathroom();

        if (bathroom != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(Util.createBathroomLatLng(bathroom))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_poo));
            mSelectedMarker = mMap.addMarker(markerOptions);
        }
    }

    private Callbacks getCallbacks() {
        return Util.cast(getActivity());
    }

    private class FetchBathroomsTask extends AsyncTask<Double,Void,List<Bathroom>> {
        private Exception mException;

        @Override
        protected void onPreExecute() {
            getCallbacks().onFetchingBathrooms(true);
        }

        @Override
        protected List<Bathroom> doInBackground(Double... params) {
            try {
                double latitude = params[0];
                double longitude = params[1];
                int distance = params[2].intValue();

                List<Bathroom> bathrooms = new BathroomMaps().fetchBathrooms(latitude, longitude, distance);
                return bathrooms;
            } catch (Exception e) {
                mException = e;
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Bathroom> bathrooms) {

            if (mException != null) {
                if (mException instanceof JSONException) {
                    Log.e(TAG, "Failed to parse json", mException);
                } else if (mException instanceof IOException) {
                    Log.e(TAG, "Failed to fetch bathrooms", mException);
                    Toast.makeText(getActivity(), R.string.fetch_bathrooms_failure, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                for (Bathroom bathroom : bathrooms) {
                    if (!mBathroomsOnMap.containsKey(bathroom.getId())) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(Util.createBathroomLatLng(bathroom))
                                .title(bathroom.getName());
                        Marker marker = mMap.addMarker(markerOptions);
                        mMarkerMap.put(marker.getId(), bathroom);
                        mBathroomsOnMap.put(bathroom.getId(), bathroom);
                    } else {
                        Log.v(TAG, "Bathroom '" + bathroom.getName() + "' already on map");
                    }
                }
            }
            getCallbacks().onFetchingBathrooms(false);
        }
    }
}
