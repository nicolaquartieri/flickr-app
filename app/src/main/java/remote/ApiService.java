package remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import local.CacheStateTable;
import nicolasquartieri.com.ar.flickr_app.BuildConfig;
import nicolasquartieri.com.ar.flickr_app.FlickrApplication;
import okhttp3.Request;
import okhttp3.Response;
import remote.httpclient.HttpClientProvider;

/**
 * Api service which can be called through ApiIntentService.
 * By default all Api Services are asynchronous because it is expected that a
 * new {@link ApiIntentService} will be created to process a single ApiService
 * and mostly from the UI thread. Nevertheless an ApiService can be executed
 * synchronously by specifying so through the constructor
 * {@link ApiService#ApiService(boolean)}.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public abstract class ApiService {
    /** Log tag. */
    private static final String TAG = ApiService.class.getSimpleName();
    /** Cache TTL is 30 seconds. */
    private static final long CACHE_TTL = 30 * 1000;
    /** Http method GET. */
    private static final String METHOD_GET = "GET";
    /** Flag to force request avoiding cache, it should contain a Boolean. */
    public static final String EXTRA_FORCE_REQUEST = "EXTRA_FORCE_REQUEST";
    /** Response error. */
    public static final String EXTRA_RESPONSE_ERROR = "EXTRA_RESPONSE_ERROR";

    /** Whether the requests executed by this instance ar synchronous or not. */
    private boolean mIsSynchronous;

    /**
     * Default constructor for an asynchronous service.
     */
    ApiService() {
        mIsSynchronous = false;
    }

    /**
     * Constructor that allows specifying whether the service is synchronous
     * or not.
     *
     * @param isSynchronous Whether the execution of this service instance
     * is synchronous or not.
     */
    ApiService(final boolean isSynchronous) {
        mIsSynchronous = isSynchronous;
    }

    /**
     * Returns the intent action to notify the response.
     *
     * @return the intent action.
     */
    protected abstract String getResponseAction();

    /**
     * Execute the api service.
     *
     * @param context the context, cannot be null.
     * @param args the arguments, cannot be null.
     * @throws ApiErrorResponseException if the request was synchronous and the
     * obtained response was not successful.
     * @throws Exception if there is something wrong executing the api service.
     */
    public void execute(Context context, Bundle args) throws Exception {
        // Get arguments.
        boolean forceRequest = args.getBoolean(EXTRA_FORCE_REQUEST, false);
        // Create request.
        Request request = onCreateRequest(context, args);
        // Check cache.
        if (!forceRequest && checkCacheState(request)) {
            // Data in cache is fresh.
            // Implementation note: synchronous services can not yet make use
            // of the cache, because those services are expecting an immediate
            // result which we don't have and must be obtained from the db.
            if (!mIsSynchronous) {
                onNotifyFinish(context, args);
            }
            return;
        }
        // Execute request.
        Response response = HttpClientProvider.executeRequest(request);
        if (!response.isSuccessful()) {
            onServiceError(context, response);
            return;
        }

        // Parse response.
        onParseResponse(context, args, response);
        // Update cache.
        updateCacheState(request);
        if (!mIsSynchronous) {
            // Notify finish.
            onNotifyFinish(context, args);
        }
    }

    /**
     * Called to create the API service request.
     * @param context the context.
     * @param args the arguments.
     * @return a request.
     * @throws Exception if there something wrong creating the request.
     */
    protected abstract Request onCreateRequest(Context context, Bundle args) throws Exception;

    /**
     * Called to parse the API service response.
     * @param context the context.
     * @param args the arguments.
     * @param response a response.
     * @throws Exception if there something wrong parsing the response.
     */
    protected abstract void onParseResponse(Context context, Bundle args, Response response)
            throws Exception;

    /**
     * Called to notify that API service has finished its execution.
     * @param context the context.
     * @param args the arguments.
     * @throws Exception if there something wrong notifying.
     */
    protected void onNotifyFinish(Context context, Bundle args) {
        // Notify finish.
        Intent intent = new Intent(getResponseAction());
        intent.putExtras(args);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Called to handle error from API service.
     * @param context the context.
     * @param response the response.
     * @throws ApiErrorResponseException if the request is synchronous, otherwise
     * the error will be broadcast.
     */
    protected void onServiceError(Context context, Response response) {
        JsonReader reader = null;
        ApiErrorResponse apiErrorResponse = null;
        int code = response.code();
        if (code == HttpURLConnection.HTTP_UNAUTHORIZED
                || code == HttpURLConnection.HTTP_FORBIDDEN) {
            apiErrorResponse = new ApiErrorResponse(ApiErrorResponse.ERROR_RELOGIN,
                    response.code());
        } else {
            try {
                reader = new JsonReader(new InputStreamReader(response.body().byteStream()));
                Gson gson = new Gson();
                apiErrorResponse = gson.fromJson(reader, ApiErrorResponse.class);
                if (apiErrorResponse != null) {
                    apiErrorResponse.setStatusCode(response.code());
                }
                reader.close();
            } catch (IOException e) {
                Log.v(TAG, "Error parsing api error.", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.v(TAG, "Error closing body stream.", e);
                    }
                }
            }
        }

        if (apiErrorResponse == null) {
            apiErrorResponse = new ApiErrorResponse(ApiErrorResponse.ERROR_SERVICE,
                    response.code());
        }

        if (!mIsSynchronous) {
            //Notify service error.
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_RESPONSE_ERROR, apiErrorResponse);
            onNotifyFinish(context, args);
        } else {
            //Caller is locked waiting for a response.
            throw new ApiErrorResponseException(apiErrorResponse);
        }
    }

    /**
     * Called to handle connection errors.
     * @param context the context.
     * @param args the arguments.
     * @param e the exception.
     */
    public void onConnectionError(Context context, Bundle args, Exception e) {
        // Notify connection error.
        Intent intent = new Intent(getResponseAction());
        args.putParcelable(EXTRA_RESPONSE_ERROR, new ApiErrorResponse(
                ApiErrorResponse.ERROR_CONNECTION));
        intent.putExtras(args);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Called to handle application errors.
     * @param context the context.
     * @param args the arguments.
     * @param e the exception.
     */
    public void onApplicationError(Context context, Bundle args, Exception e) {
        // Notify application error.
        Intent intent = new Intent(getResponseAction());
        args.putParcelable(EXTRA_RESPONSE_ERROR, new ApiErrorResponse(
                ApiErrorResponse.ERROR_APPLICATION));
        intent.putExtras(args);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Verify cache state for given request.
     * @param request request to verify.
     * @return true if the data associated to the request is still fresh.
     */
    private boolean checkCacheState(Request request) {
        // Only GET request should be cache.
        if (!METHOD_GET.equals(request.method())) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = FlickrApplication.getAppContext().getContentResolver().query(
                    CacheStateTable.CONTENT_URI,
                    null,
                    CacheStateTable.URL + "=?",
                    new String[]{request.url().toString()},
                    null);
            if (cursor != null && cursor.moveToNext()) {
                long lastUpdate = cursor.getLong(
                        cursor.getColumnIndex(CacheStateTable.LAST_UPDATE));
                if (System.currentTimeMillis() - lastUpdate <= getCacheTTL()) {
                    return true;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Update cache state for given request.
     * @param request request to update.
     */
    private void updateCacheState(Request request) {
        ContentValues values = new ContentValues();
        values.put(CacheStateTable.URL, request.url().toString());
        values.put(CacheStateTable.LAST_UPDATE, System.currentTimeMillis());
        FlickrApplication.getAppContext().getContentResolver().insert(
                CacheStateTable.CONTENT_URI, values);
    }

    /**
     * Whether this service is running in a synchronous way.
     *
     * @return true if it is synchronous, false otherwise.
     */
    protected boolean isSynchronous() {
        return mIsSynchronous;
    }

    /**
	 * Allow access time-to-live value. Use this method for any service implementation who
	 * want to override this value.
	 * @return The TTL value. (default = 30000)
	 */
    protected long getCacheTTL() {
        return CACHE_TTL;
    }

    /**
     * This utility method allows to log the response and returns a new input stream.
     * In release builds it just returns the input stream from response.
     * @param response the response.
     * @return the input stream, can be null. The input stream must be closed after
     * consumption.
     * @throws IOException if there is something wrong getting the input stream.
     */
    protected InputStream getBody(Response response) throws IOException {
        InputStream bodyInputStream;
        if (response.body() == null) {
            return null;
        } else if (BuildConfig.DEBUG) {
            // Log response.
            String body = response.body().string();
            Log.d(TAG, body);
            bodyInputStream = new ByteArrayInputStream(body.getBytes());
        } else {
            bodyInputStream = response.body().byteStream();
        }
        return bodyInputStream;
    }

    /**
     * Parse an object from given input stream.
     * @param in the input stream.
     * @param clazz the class of array instances.
     * @param <T> the type.
     * @return a List with parsed elements.
     */
    protected <T> T parseObject(InputStream in, Class<T> clazz) {
        final Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        return gson.fromJson(reader, clazz);
    }

    /**
     * Parse an array from given input stream.
     * @param in the input stream.
     * @param clazz the class of array instances.
     * @param <T> the type.
     * @return a List with parsed elements.
     */
    protected <T> List<T> parseArray(InputStream in, Class<T> clazz) {
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        Gson gson = GsonHelper.getGson();
        Type listType = new ArrayApiResponseType<T>(clazz);
        ArrayApiResponse<T> response = gson.fromJson(reader, listType);
        return Arrays.asList(response.getElements());
    }

    /** This type allows to parse ArrayApiResponse objects with Gson. */
    class ArrayApiResponseType<T> implements ParameterizedType {
        /** Array item class. */
        private Class<T> mItemClazz;

        /**
         * Constructor.
         * @param itemClazz the array item class.
         */
        public ArrayApiResponseType(Class<T> itemClazz) {
            mItemClazz = itemClazz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{mItemClazz};
        }

        @Override
        public Type getRawType() {
            return ArrayApiResponse.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
