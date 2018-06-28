package pl.loc.loccaf;


import java.util.ArrayList;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.google.android.gms.maps.model.LatLng;

import android.util.Log;

class GMapV2Direction {
    public final static String MODE_DRIVING = "driving";
    public final static String MODE_WALKING = "walking";

    public GMapV2Direction() { }

    public Document getDocument(LatLng start, LatLng end, String mode) {
        String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=walking";

        try {
            return Jsoup.connect(url).parser(Parser.xmlParser()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDurationText (Document doc) {
        Elements nl1 = doc.getElementsByTag("duration");
        Element node1 = nl1.get(0);
        Elements nl2 = node1.getAllElements();
        Element node2 = nl2.get(getNodeIndex(nl2, "text"));
        Log.i("DurationText", node2.text());
        return node2.text();
    }

    public int getDurationValue (Document doc) {
        Elements nl1 = doc.getElementsByTag("duration");
        Element node1 = nl1.get(0);
        Elements nl2 = node1.getAllElements();
        Element node2 = nl2.get(getNodeIndex(nl2, "value"));
        Log.i("DurationValue", node2.text());
        return Integer.parseInt(node2.text());
    }

    public String getDistanceText (Document doc) {
        Elements nl1 = doc.getElementsByTag("distance");
        Element node1 = nl1.last();
        Elements nl2 = node1.getAllElements();
        Element node2 = nl2.get(getNodeIndex(nl2, "text"));
        Log.i("DistanceText", node2.text());
        return node2.text();
    }

    public int getDistanceValue (Document doc) {
        Elements nl1 = doc.getElementsByTag("distance");
        Element node1 = nl1.last();
        Elements nl2 = node1.getAllElements();
        Element node2 = nl2.get(getNodeIndex(nl2, "value"));
        Log.i("DistanceValue", node2.text());
        return Integer.parseInt(node2.text());
    }

    public String getStartAddress (Document doc) {
        Elements nl1 = doc.getElementsByTag("start_address");
        Element node1 = nl1.get(0);
        Log.i("StartAddress", node1.text());
        return node1.text();
    }

    public String getEndAddress (Document doc) {
        Elements nl1 = doc.getElementsByTag("end_address");
        Element node1 = nl1.get(0);
        Log.i("StartAddress", node1.text());
        return node1.text();
    }

    public String getCopyRights (Document doc) {
        Elements nl1 = doc.getElementsByTag("copyrights");
        Element node1 = nl1.get(0);
        Log.i("CopyRights", node1.text());
        return node1.text();
    }

    public ArrayList<LatLng> getDirection (Document doc) {
        Elements nl1;
        Elements nl2;
        Elements nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<>();
        nl1 = doc.getElementsByTag("step");
        if (nl1.size() > 0) {
            for (int i = 0; i < nl1.size(); i++) {
                Element node1 = nl1.get(i);
                nl2 = node1.getAllElements();

                Element locationNode = nl2.get(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getAllElements();
                Element latNode = nl3.get(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.text());
                Element lngNode = nl3.get(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.text());
                listGeopoints.add(new LatLng(lat, lng));

                locationNode = nl2.get(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getAllElements();
                latNode = nl3.get(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.text());
                for(int j = 0 ; j < arr.size() ; j++) {
                    listGeopoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
                }

                locationNode = nl2.get(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getAllElements();
                latNode = nl3.get(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.text());
                lngNode = nl3.get(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.text());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }

        return listGeopoints;
    }

    private int getNodeIndex(Elements nl, String nodename) {
        for(int i = 0 ; i < nl.size() ; i++) {
            if(nl.get(i).nodeName().equals(nodename))
                return i;
        }
        return -1;
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
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

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}