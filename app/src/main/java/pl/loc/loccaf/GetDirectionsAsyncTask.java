package pl.loc.loccaf;

import java.util.ArrayList;
import java.util.Map;


import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.nodes.Document;

class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object, Document> {

    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LONG = "destination_long";
    public static final String DIRECTIONS_MODE = "directions_mode";
    private MainFragment fragment;
    private String url;

    private Exception exception;


    public GetDirectionsAsyncTask(MainFragment fragment /*String url*/)
    {
        super();
        this.fragment = fragment;

        //  this.url = url;
    }

    public void onPreExecute() {
    }

    @Override
    public void onPostExecute(Document doc) {

        if (exception == null) {
            fragment.handleGetDirectionsResult(doc);
        } else {
            processException();
        }
    }

    @SafeVarargs
    @Override
    protected final Document doInBackground(Map<String, String>... params) {
        try {
            Map<String, String> paramMap = params[0];
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)), Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)), Double.valueOf(paramMap.get(DESTINATION_LONG)));
            GMapV2Direction md = new GMapV2Direction();
            Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
            return doc;
        }
        catch (Exception e){
            exception = e;
            processException();
            return null;
        }
    }

    private void processException() {
        Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.error_when_retrieving_data), Toast.LENGTH_SHORT).show();
    }

}