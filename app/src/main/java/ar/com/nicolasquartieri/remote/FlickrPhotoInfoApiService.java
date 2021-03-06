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
import ar.com.nicolasquartieri.local.PhotoInfoTable;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.PhotoInfo;
import ar.com.nicolasquartieri.ui.utils.EnvironmentUtils;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service in charge of retrieving the {@link PhotoInfo} of the selected {@link Photo}.
 * The intent required for this service must be created with
 * {@link FlickrPhotoInfoApiService#newIntent(Context, Photo)} and later on can be invoked
 * with {@link Context#startService(Intent)}. When the service finishes its work the
 * action {@link FlickrPhotoInfoApiService#RESPONSE_ACTION} is broadcast for all receivers
 * to be notified.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrPhotoInfoApiService extends ApiService {
    private final String TAG = FlickrPhotoInfoApiService.class.getSimpleName();

    /** Api service id. */
    public static final String ID = "PHOTO_INFO_API";

    /** Api service broadcast action. */
    public static final String RESPONSE_ACTION = "PHOTO_INFO_API_RESPONSE";

    /** Requested photo */
    public static final String ARG_PHOTO = "ARG_PHOTO";

    /** Default constructor. */
    FlickrPhotoInfoApiService() {
    }

    /**
     * Creates a new {@link Intent} with the necessary information to invoke this service
     * in order to create a new anonymous user.
     *
     * @param context The new {@link Intent} context, cannot be null.
     * @param photo
     * @return A new {@link Intent} that can be used to invoke this service, never null
     */
    public static Intent newIntent(final Context context, Photo photo) {
        Intent intent = new Intent(context, ApiIntentService.class);
        Bundle extras = new Bundle();
        extras.putString(ApiIntentService.EXTRA_API_SERVICE_ID,
                FlickrPhotoInfoApiService.ID);
        extras.putParcelable(ARG_PHOTO, photo);
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
        Photo photo = args.getParcelable(ARG_PHOTO);
        // Create URL
        FlickrSearchRequestBuilder.FlickrSearchRequest flickrSearchRequest = builder
                .setPhotoId(photo.getId())
                .setSecret(photo.getSecret())
                .build();

        // Create request
        return new Request.Builder().url(flickrSearchRequest.getUrl()).get().build();
    }

    @Override
    protected void onParseResponse(Context context, Bundle args, Response response)
            throws Exception {

        // Get body.
        String jsonString = getPhotoInfoBody(response);

        // Get Photos.
        PhotoInfo photoInfo;
        photoInfo = parseObject(jsonString, "photo");
        // Store response.
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        // Delete previous data.
        operations.add(ContentProviderOperation.newDelete(PhotoInfoTable.buildUri()).build());
        // Store.
        addOperation(operations, photoInfo);

        ContentResolver resolver = context.getContentResolver();
        resolver.applyBatch(AppContentProvider.CONTENT_AUTHORITY, operations);
    }

    /**
     * Add a new {@link ContentProviderOperation} ready to be store.
     * @param operations the {@link ContentProviderOperation}, cannot be null.
     * @param photoInfo the {@link PhotoInfo}, cannot be null.
     */
    private void addOperation(ArrayList<ContentProviderOperation> operations, PhotoInfo photoInfo) {
        ContentProviderOperation operation = ContentProviderOperation
                .newInsert(PhotoInfoTable.buildUri())
                .withValues(PhotoInfoTable.toContentValues(photoInfo))
                .build();
        operations.add(operation);
    }

    /**
     * Get the body of the {@link Response}.
     * @param response The response, can't be null.
     * @return The body.
     * @throws IOException
     */
    private String getPhotoInfoBody(Response response) throws IOException {
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
    private PhotoInfo parseObject(String in, String member) {
        final Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(in, JsonObject.class);
        PhotoInfo photoInfo = null;
        if (jsonObject != null && jsonObject.has(member)) {
            JsonObject element = jsonObject.getAsJsonObject(member);
            photoInfo = gson.fromJson(element.toString(), PhotoInfo.class);
        }

        return photoInfo;
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

        public FlickrSearchRequestBuilder setPhotoId(String text) {
            request.setPhotoId(text);
            return this;
        }

        public FlickrSearchRequestBuilder setSecret(String text) {
            request.setSecret(text);
            return this;
        }

        @Override
        public FlickrSearchRequest build() {
            return request;
        }

        class FlickrSearchRequest {
            private static final String SEARCH_PHOTO_ID_PARAM = "SEARCH_PHOTO_ID_PARAM";
            private static final String SEARCH_SECRET_PARAM = "SEARCH_SECRET_PARAM";
            private Uri.Builder uriBuilder;
            private Map<String, String> tokenMap;

            public FlickrSearchRequest() {
                this.tokenMap = new HashMap<>();
            }

            public void setPhotoId(String photoId) {
                tokenMap.put(SEARCH_PHOTO_ID_PARAM, photoId);
            }

            public void setSecret(String secret) {
                tokenMap.put(SEARCH_SECRET_PARAM, secret);
            }

            public String getUrl() {
                uriBuilder = Uri.parse(EnvironmentUtils.getBaseUrl()).buildUpon();
                uriBuilder.appendQueryParameter("method", "flickr.photos.getInfo");
                uriBuilder.appendQueryParameter("api_key", EnvironmentUtils.getApiKey());
                uriBuilder.appendQueryParameter("photo_id", tokenMap.get(SEARCH_PHOTO_ID_PARAM));
                uriBuilder.appendQueryParameter("secret", tokenMap.get(SEARCH_SECRET_PARAM));
                uriBuilder.appendQueryParameter("format", "json");
                uriBuilder.appendQueryParameter("nojsoncallback", "1");
                return uriBuilder.build().toString();
            }
        }
    }
}
