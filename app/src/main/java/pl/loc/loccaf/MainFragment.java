package pl.loc.loccaf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by Kamil on 08.02.2015.
 */

public class MainFragment extends Fragment {

    GoogleApiClient mGoogleApiClient;
    View rootView;
    JSONObject business_object;
    MainFragment fragment;
    Location location;
    PolylineOptions rectLine;
    private Tracker mTracker;
    private Document mDoc;


    @Override
       public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                Bundle savedInstanceState) {
        fragment = this;
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.setBackgroundResource(R.drawable.background_xml);
        ((MapView)rootView.findViewById(R.id.map)).onCreate(savedInstanceState);

        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Secrets.testDeviceID).build();
        mAdView.loadAd(adRequest);

        mTracker.setScreenName("Start");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        ImageButton button = (ImageButton)rootView.findViewById(R.id.coffee_button);
        mGoogleApiClient = ((MainActivity)getActivity()).getClient();
                   button.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           ConnectivityManager cm =
                                   (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                           NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                           boolean isConnected = activeNetwork != null &&
                                   activeNetwork.isConnectedOrConnecting();
                           rootView.findViewById(R.id.start).setVisibility(View.INVISIBLE);
                           rootView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                           mTracker.setScreenName("Progress");
                           mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                           if (mGoogleApiClient.isConnected()) {
                               location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                               if (location != null && isConnected) {
                                   Yelp yelp = new Yelp(getActivity(), fragment);
                                   runYelp(yelp, location.getLatitude(), location.getLongitude());
                               } else if (location == null) {
                                   Toast toast = Toast.makeText(getActivity(), R.string.location_error_message, Toast.LENGTH_SHORT);
                                   toast.show();
                                   rootView.findViewById(R.id.start).setVisibility(View.VISIBLE);
                                   rootView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                                   mTracker.send(new HitBuilders.EventBuilder().setCategory("Error")
                                           .setAction("No location").build());
                               } else if (!isConnected) {
                                   Toast toast = Toast.makeText(getActivity(), R.string.connection_error_message, Toast.LENGTH_SHORT);
                                   toast.show();
                                   rootView.findViewById(R.id.start).setVisibility(View.VISIBLE);
                                   rootView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                                   mTracker.send(new HitBuilders.EventBuilder().setCategory("Error")
                                           .setAction("No connection").build());
                               }
                           }
                       }
                   });

        if (business_object != null && mDoc != null){
            handleGetDirectionsResult(mDoc);
        }
        else if (business_object != null){
            rootView.findViewById(R.id.start).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            JSONObject location_object = (JSONObject)((JSONObject)business_object.get("location")).get("coordinate");
            findDirections(location.getLatitude(),
                    location.getLongitude(),
                    (double)location_object.get("latitude"), (double)location_object.get("longitude"), GMapV2Direction.MODE_WALKING);
        }
           return rootView;
       }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        super.onCreate(savedInstanceState);
    }

    public void handleGetDirectionsResult(Document doc)
    {
        mDoc = doc;
        GMapV2Direction mGMap = new GMapV2Direction();
        ArrayList<LatLng> directionPoints = mGMap.getDirection(doc);
        final MapView mMap = ((MapView)rootView.findViewById(R.id.map));
        mMap.getMap().getUiSettings().setMapToolbarEnabled(false);

        rectLine = new PolylineOptions().width(3).color(Color.RED);
        MapsInitializer.initialize(getActivity());
        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for(int i = 0 ; i < directionPoints.size() ; i++) {
            rectLine.add(directionPoints.get(i));
            bounds.include(directionPoints.get(i));
        }

        mMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.addPolyline(rectLine);
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                                     @Override
                                                     public void onMapLoaded() {
                                                         googleMap.setBuildingsEnabled(true);
                                                         googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));

                                                     }
                                                 }
                );
            }
        });
        rootView.findViewById(R.id.start).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.success).setVisibility(View.VISIBLE);
        ((TextView)rootView.findViewById(R.id.name_textView)).setText(business_object.get("name").toString());
        ((TextView)rootView.findViewById(R.id.distance_textView)).setText(mGMap.getDistanceText(doc));
        if((boolean)business_object.get("is_closed")){
            ((TextView)rootView.findViewById(R.id.open_textView)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ((TextView)rootView.findViewById(R.id.open_textView)).setText(getString(R.string.closed));
        }
        else{
            ((TextView)rootView.findViewById(R.id.open_textView)).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            ((TextView)rootView.findViewById(R.id.open_textView)).setText(getString(R.string.open));
        }
        if (business_object.get("image_url") != null) {
            Picasso.with(getActivity()).load(business_object.get("image_url").toString()).into((ImageView) rootView.findViewById(R.id.photo_imageView));
        }
        else{
            Picasso.with(getActivity()).load(R.drawable.button_background_png).into((ImageView) rootView.findViewById(R.id.photo_imageView));
        }
        Picasso.with(getActivity()).load(business_object.get("rating_img_url_large").toString()).into((ImageView) rootView.findViewById(R.id.rating_imageView));
        rootView.findViewById(R.id.yelp_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Action")
                        .setAction("Yelp").build());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(business_object.get("mobile_url").toString()));
                startActivity(browserIntent);
            }
        });
        ((Button)rootView.findViewById(R.id.navigate_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Action")
                        .setAction("Navigate").build());
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + ((JSONObject) ((JSONObject) business_object.get("location")).get("coordinate")).get("latitude")
                        + "," + ((JSONObject) ((JSONObject) business_object.get("location")).get("coordinate")).get("longitude") + "&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        mTracker.setScreenName("Success");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }

    public void runYelp(Yelp yelp, double latitude, double longitude){
        yelp.execute(latitude, longitude);
    }
    public void handleResults(String json){
        JSONObject result = (JSONObject)JSONValue.parse(json);
        if (((JSONArray)result.get("businesses")).size() < 1){
            Toast toast = Toast.makeText(getActivity(), R.string.no_poi_message, Toast.LENGTH_SHORT);
            toast.show();
            rootView.findViewById(R.id.start).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            mTracker.send(new HitBuilders.EventBuilder().setCategory("Error")
                    .setAction("No cafe").build());
            return;
        }
        business_object = (JSONObject)((JSONArray) result.get("businesses")).get(0);
        JSONObject location_object = (JSONObject)((JSONObject)business_object.get("location")).get("coordinate");
        findDirections(location.getLatitude(),
                location.getLongitude(),
                (double)location_object.get("latitude"), (double)location_object.get("longitude"), GMapV2Direction.MODE_WALKING);
    }

}