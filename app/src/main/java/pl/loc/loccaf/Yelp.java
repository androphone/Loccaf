package pl.loc.loccaf;

/*
 Example code based on code from Nicholas Smith at http://imnes.blogspot.com/2011/01/how-to-use-yelp-v2-from-java-including.html
 For a more complete example (how to integrate with GSON, etc) see the blog post above.
 */

import android.content.Context;
import android.os.AsyncTask;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Example for accessing the Yelp API.
 */
public class Yelp extends AsyncTask<Double, String, String>{

    OAuthService service;
    Token accessToken;
    MainFragment fragment;

  /**
   * Setup the Yelp API OAuth credentials.
   * OAuth credentials are available from the developer site, under Manage API access (version 2 API).

   */
  public Yelp(Context context, MainFragment current) {
      fragment = current;
      this.service = new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(Secrets.consumer_key).apiSecret(Secrets.consumer_secret).build();
      this.accessToken = new Token(Secrets.token, Secrets.token_secret);
  }

  /**
   * Search with term and location.
   *
   * @param latitude  Latitude
   * @param longitude Longitude
   * @return JSON string response
   */
  public String search(double latitude, double longitude) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
    request.addQuerystringParameter("category_filter", fragment.getActivity().getString(R.string.categories));
    request.addQuerystringParameter("ll", latitude + "," + longitude);
    request.addQuerystringParameter("limit", "1");
    request.addQuerystringParameter("sort", "1");
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

    @Override
    protected String doInBackground(Double... params) {
      OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
      request.addQuerystringParameter("category_filter", fragment.getActivity().getString(R.string.categories));
      request.addQuerystringParameter("ll", params[0] + "," + params[1]);
      request.addQuerystringParameter("limit", "1");
        request.addQuerystringParameter("sort", "1");
        request.addHeader("Accept", "*/*");
      this.service.signRequest(this.accessToken, request);
      Response response = request.send();
      return response.getBody();
    }
    @Override
    protected void onPostExecute(String string){
      fragment.handleResults(string);
    }
}