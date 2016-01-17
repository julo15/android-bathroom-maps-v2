package com.trublo.bathroommaps.fragments;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
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
import com.trublo.bathroommaps.GoogleMapCategorizer;
import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.bathroommaps.BathroomMaps;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by julianlo on 1/6/16.
 */
public class BathroomMapFragment extends SupportMapFragment {
    private static final String TAG = "BathroomMapFragment";

    private static final int CLICKED_BATHROOM_ZOOM_LEVEL = 16;

    private static final GoogleMapCategorizer.CategoryDescriptor<String>[] DEFAULT_CATEGORIES = new GoogleMapCategorizer.CategoryDescriptor[] {
            new GoogleMapCategorizer.CategoryDescriptor<String>()
                .setId("Public"),
            new GoogleMapCategorizer.CategoryDescriptor<String>()
                .setId("Coffee Shop")
    };

    private GoogleApiClient mClient;
    private GoogleMapCategorizer<String> mMap;
    private HashMap<String, Bathroom> mMarkerMap = new HashMap<>(); // maps marker to bathroom
    private HashMap<String, Bathroom> mBathroomsOnMap = new HashMap<>(); // maps bathroom id to bathroom for bathrooms on the map already
    private Marker mSelectedMarker;
    private boolean mPerformedInitialCentering;
    private LatLng mLastFetchLocation;
    private int mLastFetchDistance;
    private float mMinimumRatingFilter = 0;

    public static BathroomMapFragment newInstance() {
        return new BathroomMapFragment();
    }

    public interface Callbacks {
        void onFetchingBathrooms(boolean fetching);
        void onBathroomMarkerSelected(Bathroom bathroom);
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

                        if (mMap == null) {
                            Log.w(TAG, "GoogleApiClient connected before GoogleMap is ready. Cannot center map.");
                            return;
                        }

                        // Kind of hacky. When the fragment loads up, we want the map to centre on the user's current
                        // location. Ideally we'd call centerMap on onMapReady, but that happens before GoogleApiClient
                        // has connected, i.e. before we can get the user's current location.
                        // Here we assume that the first time GoogleApiClient connects corresponds to the first time
                        // the fragment is shown. This should generally be correct.
                        if (!mPerformedInitialCentering) {
                            centerMap();
                            mPerformedInitialCentering = true;
                        }
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
                mMap = new GoogleMapCategorizer<>(googleMap, DEFAULT_CATEGORIES);

                try {
                    mMap.getMap().setMyLocationEnabled(true);
                } catch (SecurityException se) {
                    Log.w(TAG, "Could not set my location enabled", se);
                }

                mMap.getMap().getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getMap().getUiSettings().setMapToolbarEnabled(false);

                mMap.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {

                        // Get the distance in meters between the corners of the map bounds.
                        // We'll use this as the distance to fetch bathrooms for.
                        LatLngBounds bounds = mMap.getMap().getProjection().getVisibleRegion().latLngBounds;
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

                        if (mLastFetchLocation != null) {
                            Location.distanceBetween(
                                    mLastFetchLocation.latitude,
                                    mLastFetchLocation.longitude,
                                    cameraPosition.target.latitude,
                                    cameraPosition.target.longitude,
                                    results);
                            int distanceFromLastFetch = (int)results[0];
                            if (distanceFromLastFetch * 2 < mLastFetchDistance) {
                                Log.v(TAG, "Distance from last fetch point is less than half last fetch distance, skipping bathroom fetch");
                                return;
                            }
                        }
                        fetchBathrooms(cameraPosition.target.latitude, cameraPosition.target.longitude, distance);
                    }
                });

                mMap.getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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
                        centerMap(bathroom.getLatitude(), bathroom.getLongitude());
                        getCallbacks().onBathroomMarkerSelected(bathroom);
                        return true;
                    }
                });

                mMap.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        boolean selectionCleared = clearSelectedBathroom();
                        if (selectionCleared) {
                            getCallbacks().onBathroomMarkerSelected(null);
                        }
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

    public Location getCurrentLocation() {
        // TODO: Check permissions
        return LocationServices.FusedLocationApi.getLastLocation(mClient);
    }

    public void centerMap() {
        Location location = getCurrentLocation();
        if (location == null) {
            Toast.makeText(getActivity(), R.string.current_location_unavailable, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        centerMap(location.getLatitude(), location.getLongitude());
    }

    private void centerMap(double latitude, double longitude) {
        // Centre the map on the coordinates given.
        // This method also makes a choice on whether the zoom of the camera should be adjusted.
        // If the zoom is far enough away from 'city' level, then the zoom is restored to CLICK_BATHROOM_ZOOM_LEVEL.
        CameraPosition.Builder builder = new CameraPosition.Builder();

        builder.target(new LatLng(latitude, longitude));

        CameraPosition currentCameraPosition = mMap.getMap().getCameraPosition();

        if (currentCameraPosition.zoom > CLICKED_BATHROOM_ZOOM_LEVEL + 2 ||
                currentCameraPosition.zoom < CLICKED_BATHROOM_ZOOM_LEVEL - 2) {
            builder.zoom(CLICKED_BATHROOM_ZOOM_LEVEL);
        } else {
            builder.zoom(currentCameraPosition.zoom);
        }

        mMap.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
    }

    @MainThread
    public void notifyBathroomUpdated(Bathroom updatedBathroom) {
        // We need to update both of our hashmaps that contain Bathroom instances with the new bathroom
        // For each, we find the Bathroom instance that has the matching ID, and replace accordingly.
        Util.updateValueInMap(mMarkerMap, updatedBathroom, new Comparator<Bathroom>() {
            @Override
            public int compare(Bathroom lhs, Bathroom rhs) {
                return (lhs.getId().equals(rhs.getId())) ? 0 : 1;
            }
        });

        if (!mBathroomsOnMap.containsKey(updatedBathroom.getId())) {
            throw new IllegalArgumentException("Bathroom not found in bathroom map");
        }

        mBathroomsOnMap.put(updatedBathroom.getId(), updatedBathroom);
    }

    public List<GoogleMapCategorizer.CategoryInfo<String>> getCategories() {
        return mMap.getCategories();
    }

    public void showCategory(String category, boolean show) {
        Set<Marker> markers = mMap.showCategory(category, show);

        // Now perform second-order filtering: average rating
        for (Iterator<Marker> iterator = markers.iterator(); iterator.hasNext();) {
            ensureMarkerHiddenOnBelowMinimumRating(iterator.next());
        }
    }

    public float getMinimumRatingFilter() {
        return mMinimumRatingFilter;
    }

    public void setMinimumRatingFilter(float rating) {
        // This method itself doesn't re-iterate over the markers to ensure the right ones are visible.
        // It relies on calls to showCategory to do the re-iteration.
        // So, in order for this all to work correctly, showCategory needs to be called on all of the
        // categories after setMinimumRatingFilter is called.
        // This is a reasonable assumption given that all of the necessary information comes from
        // the CategoryFilterFragment.
        mMinimumRatingFilter = rating;
    }

    private void fetchBathrooms(double latitude, double longitude, int distance) {
        new FetchBathroomsTask(latitude, longitude, distance).execute();
    }

    private boolean clearSelectedBathroom() {
        boolean selectionCleared = false;
        if (mSelectedMarker != null) {
            mMap.removeMarker(mSelectedMarker);
            mSelectedMarker = null;
            selectionCleared = true;
        }
        return selectionCleared;
    }

    private void setSelectedBathroom(Bathroom bathroom) {
        clearSelectedBathroom();

        if (bathroom != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(Util.createBathroomLatLng(bathroom))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_poo));
            mSelectedMarker = mMap.addMarker(markerOptions, null);
        }
    }

    private void ensureMarkerHiddenOnBelowMinimumRating(Marker marker) {
        if (marker.isVisible()) {
            Bathroom bathroom = mMarkerMap.get(marker.getId());
            if (bathroom.getAverageRating() < mMinimumRatingFilter) {
                marker.setVisible(false);
            }
        }
    }

    private Callbacks getCallbacks() {
        return Util.cast(getActivity());
    }

    private class FetchBathroomsTask extends AsyncTask<Void,Void,List<Bathroom>> {
        private Exception mException;
        private double mLatitude;
        private double mLongitude;
        private int mDistance;

        public FetchBathroomsTask(double latitude, double longitude, int distance) {
            mLatitude = latitude;
            mLongitude = longitude;
            mDistance = distance;
        }

        @Override
        protected void onPreExecute() {
            getCallbacks().onFetchingBathrooms(true);
        }

        @Override
        protected List<Bathroom> doInBackground(Void... params) {
            try {
                List<Bathroom> bathrooms = new BathroomMaps().fetchBathrooms(mLatitude, mLongitude, mDistance);
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
                        Marker marker = mMap.addMarker(markerOptions, bathroom.getCategory());
                        mMarkerMap.put(marker.getId(), bathroom);
                        mBathroomsOnMap.put(bathroom.getId(), bathroom);
                        ensureMarkerHiddenOnBelowMinimumRating(marker);
                    } else {
                        Log.v(TAG, "Bathroom '" + bathroom.getName() + "' already on map");
                    }
                }
                mLastFetchLocation = new LatLng(mLatitude, mLongitude);
                mLastFetchDistance = mDistance;
            }
            getCallbacks().onFetchingBathrooms(false);
        }
    }
}
