package ar.com.nicolasquartieri.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import ar.com.nicolasquartieri.BuildConfig;
import ar.com.nicolasquartieri.local.AppContentProvider;
import ar.com.nicolasquartieri.local.PhotoTable;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.Photos;
import ar.com.nicolasquartieri.ui.utils.EnvironmentUtils;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service in charge of retrieving the list of {@link Photo} base on the query text
 * passed. The intent required for this service must be created with
 * {@link FlickrSearchApiService#newIntent(Context, String)} and later on can be invoked
 * with {@link Context#startService(Intent)}. When the service finishes its work the
 * action {@link FlickrSearchApiService#RESPONSE_ACTION} is broadcast for all receivers to
 * be notified.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrSearchApiService extends ApiService {
    private final String TAG = FlickrSearchApiService.class.getSimpleName();

    /** Empty String */
    private static final String EMPTY_STRING = "";

    /** Api service id. */
    public static final String ID = "SEARCH_API";

    /** Api service broadcast action. */
    public static final String RESPONSE_ACTION = "SEARCH_API_RESPONSE";

    /** Query String */
    public static final String QUERY_STRING = "QUERY_STRING";
    /** Requested page */
    public static final String PAGE= "PAGE";

    /** Default constructor. */
    FlickrSearchApiService() {
    }

    /**
     * Creates a new {@link Intent} with the necessary information to invoke this service
     * in order to create a new anonymous user.
     *
     * @param context The new {@link Intent} context, cannot be null.
     * @param text The query, cannot be null.
     * @return A new {@link Intent} that can be used to invoke this service, never null
     */
    public static Intent newIntent(final Context context, String text) {
        return newIntent(context, text, 1);
    }

    /**
     * Creates a new {@link Intent} with the necessary information to invoke this service
     * in order to create a new anonymous user.
     *
     * @param context The new {@link Intent} context, cannot be null.
     * @param text The query, cannot be null.
     * @param page The number of the pagination, default is 1.
     * @return A new {@link Intent} that can be used to invoke this service, never null
     */
    public static Intent newIntent(final Context context, String text, int page) {
        Intent intent = new Intent(context, ApiIntentService.class);
        Bundle extras = new Bundle();
        extras.putString(ApiIntentService.EXTRA_API_SERVICE_ID,
                FlickrSearchApiService.ID);
        extras.putString(QUERY_STRING, text);
        extras.putInt(PAGE, page);
        intent.putExtras(extras);
        return intent;
    }

    @Override
    public String getResponseAction() {
        return RESPONSE_ACTION;
    }

    @Override
    protected Request onCreateRequest(Context context, Bundle args) throws Exception {
        FlickrSearchRequestBuilder builder = new FlickrSearchRequestBuilder();

        // Get Arguments.
        String text = args.getString(QUERY_STRING, EMPTY_STRING);
        int page = args.getInt(PAGE, 1);
        // Create URL
        FlickrSearchRequestBuilder.FlickrSearchRequest flickrSearchRequest = builder
                .query(text)
                .page(page)
                .build();

        // Create request
        return new Request.Builder().url(flickrSearchRequest.getUrl()).get().build();
    }

    @Override
    protected void onParseResponse(Context context, Bundle args, Response response)
            throws Exception {

        // Get body.
        String jsonString = getPhotoBody(response);

        // Get Photos.
        Photos photos;
        photos = parseObject(jsonString, "photos");
        // Store response.
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        // Delete previous data.
        operations.add(ContentProviderOperation.newDelete(PhotoTable.buildUri()).build());
        for (Photo photo : photos.getPhoto()) {
            // Store.
            addOperation(operations, photo);
        }

        ContentResolver resolver = context.getContentResolver();
        resolver.applyBatch(AppContentProvider.CONTENT_AUTHORITY, operations);
    }

    /**
     * Add a new {@link ContentProviderOperation} ready to be store.
     * @param operations the {@link ContentProviderOperation}, cannot be null.
     * @param photo the {@link Photo}, cannot be null.
     */
    private void addOperation(ArrayList<ContentProviderOperation> operations, Photo photo) {
        ContentProviderOperation operation = ContentProviderOperation
                .newInsert(PhotoTable.buildUri())
                .withValues(PhotoTable.toContentValues(photo))
                .build();
        operations.add(operation);
    }

    /**
     * Get the body of the {@link Response}.
     * @param response The response, can't be null.
     * @return The body.
     * @throws IOException
     */
    private String getPhotoBody(Response response) throws IOException {
        String body;
        if (response.body() == null) {
            return null;
        } else {
            body = response.body().string();
            // Log response.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, body);
            }
        }
        return body;
    }

    /**
	 * Parse and retrieve the object inside the Json String based on the desire name of
	 * the member.
	 * @param in The Json String to be parse.
	 * @param member The member of the Json String to be find.
	 * @return The parsed object.
	 */
    private Photos parseObject(String in, String member) {
        final Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(in, JsonObject.class);
        Photos photos = null;
        if (jsonObject != null && jsonObject.has(member)) {
            JsonObject element = jsonObject.getAsJsonObject(member);
            photos = gson.fromJson(element.toString(), Photos.class);
        }
        return photos;
    }

    /**
     * Builder for Search Request Service.
     */
    class FlickrSearchRequestBuilder
            implements RequestBuilder<FlickrSearchRequestBuilder.FlickrSearchRequest> {
        private final FlickrSearchRequest request;

        public FlickrSearchRequestBuilder() {
            request = new FlickrSearchRequest();
        }

        public FlickrSearchRequestBuilder query(String text) {
            request.setSearchText(text);
            return this;
        }

        public FlickrSearchRequestBuilder page(int page) {
            request.setPage(page);
            return this;
        }

        @Override
        public FlickrSearchRequest build() {
            return request;
        }

        class FlickrSearchRequest {
            private static final String SEARCH_QUERY_PARAM = "SEARCH_QUERY_PARAM";
            private static final String PAGE_QUERY_PARAM = "PAGE_QUERY_PARAM";
            private Uri.Builder uriBuilder;
            private Map<String, String> tokenMap;

            public FlickrSearchRequest() {
                this.tokenMap = new HashMap<>();
            }

            public void setSearchText(String text) {
                tokenMap.put(SEARCH_QUERY_PARAM, text);
            }

            public void setPage(int page) {
                tokenMap.put(PAGE_QUERY_PARAM, String.valueOf(page));
            }

            public String getUrl() {
                uriBuilder = Uri.parse(EnvironmentUtils.getBaseUrl()).buildUpon();
                uriBuilder.appendQueryParameter("method", "flickr.photos.search");
                uriBuilder.appendQueryParameter("api_key", EnvironmentUtils.getApiKey());
                uriBuilder.appendQueryParameter("text", tokenMap.get(SEARCH_QUERY_PARAM));
                uriBuilder.appendQueryParameter("page", tokenMap.get(PAGE_QUERY_PARAM));
                uriBuilder.appendQueryParameter("per_page", "30");
                uriBuilder.appendQueryParameter("format", "json");
                uriBuilder.appendQueryParameter("nojsoncallback", "1");
                return uriBuilder.build().toString();
            }
        }
    }
}
