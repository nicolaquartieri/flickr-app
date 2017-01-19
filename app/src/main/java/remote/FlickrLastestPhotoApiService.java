package remote;

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

import local.AppContentProvider;
import local.PhotoTable;
import model.Photo;
import model.Photos;
import nicolasquartieri.com.ar.flickr_app.BuildConfig;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service in charge of retrieving the current user. The intent required for this service
 * must be created with {@link FlickrLastestPhotoApiService#newIntent(Context)} and later on can
 * be invoked with {@link Context#startService(Intent)}. When the service finishes its
 * work the action {@link FlickrLastestPhotoApiService#RESPONSE_ACTION} is broadcast for all
 * receivers to be notified.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrLastestPhotoApiService extends ApiService {
    private final String TAG = FlickrLastestPhotoApiService.class.getSimpleName();

    /** Api service id. */
    public static final String ID = "LATEST_PHOTO_API";

    /** Api service broadcast action. */
    public static final String RESPONSE_ACTION = "SEARCH_API_RESPONSE";

    public static final String PAGE= "PAGE";

    /** Default constructor. */
    FlickrLastestPhotoApiService() {
    }

    /**
     * Creates a new {@link Intent} with the necessary information to invoke this service
     * in order to create a new anonymous user.
     *
     * @param context The new {@link Intent} context, cannot be null.
     * @return A new {@link Intent} that can be used to invoke this service, never null
     */
    public static Intent newIntent(final Context context) {
        return newIntent(context, 1);
    }

    /**
     * Creates a new {@link Intent} with the necessary information to invoke this service
     * in order to create a new anonymous user.
     *
     * @param context The new {@link Intent} context, cannot be null.
     * @param page The number of the pagination, default is 1.
     * @return A new {@link Intent} that can be used to invoke this service, never null
     */
    public static Intent newIntent(final Context context, int page) {
        Intent intent = new Intent(context, ApiIntentService.class);
        Bundle extras = new Bundle();
        extras.putString(ApiIntentService.EXTRA_API_SERVICE_ID,
                FlickrLastestPhotoApiService.ID);
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
        int page = args.getInt(PAGE, 1);
        //TODO check this.
        // Create URL
        FlickrSearchRequestBuilder.FlickrSearchRequest flickrSearchRequest = builder
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
     */
    class FlickrSearchRequestBuilder
            implements RequestBuilder<FlickrSearchRequestBuilder.FlickrSearchRequest> {

        private final FlickrSearchRequest request;

        public FlickrSearchRequestBuilder() {
            request = new FlickrSearchRequest();
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
            private static final String PAGE_QUERY_PARAM = "PAGE_QUERY_PARAM";
            private Uri.Builder uriBuilder;
            private Map<String, String> tokenMap;

            public FlickrSearchRequest() {
                this.tokenMap = new HashMap<>();
            }

            public void setPage(int page) {
                tokenMap.put(PAGE_QUERY_PARAM, String.valueOf(page));
            }

            public String getUrl() {
                uriBuilder = Uri.parse("https://api.flickr.com/services/rest/").buildUpon();
                uriBuilder.appendQueryParameter("method", "flickr.photos.getRecent");
                uriBuilder.appendQueryParameter("api_key", "67694921845e1e630e1be511d82a6f53");
                uriBuilder.appendQueryParameter("page", tokenMap.get(PAGE_QUERY_PARAM));
                uriBuilder.appendQueryParameter("per_page", "30");
                uriBuilder.appendQueryParameter("format", "json");
                uriBuilder.appendQueryParameter("nojsoncallback", "1");
                return uriBuilder.build().toString();
            }
        }
    }
}
