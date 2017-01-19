package remote;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * IntentService which executes api services.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class ApiIntentService extends IntentService {
    /** Log tag. */
    private static final String TAG = ApiIntentService.class.getSimpleName();
    /** Api service id to execute. */
    public static final String EXTRA_API_SERVICE_ID = "EXTRA_API_SERVICE_ID";
    /** Map with supported API services. */
    private HashMap<String, ApiService> mApiServices;

    /** Constructor. */
    public ApiIntentService() {
        super(ApiIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApiServices = new HashMap<String, ApiService>();
        mApiServices.put(FlickrSearchApiService.ID, new FlickrSearchApiService());
        mApiServices.put(FlickrPhotoInfoApiService.ID, new FlickrPhotoInfoApiService());
        mApiServices.put(FlickrLastestPhotoApiService.ID, new FlickrLastestPhotoApiService());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get extras.
        String apiServiceId = intent.getStringExtra(EXTRA_API_SERVICE_ID);

        // Get api service.
        ApiService apiService = mApiServices.get(apiServiceId);
        if (apiService == null) {
            Log.e(TAG, "Invalid API service ID: " + apiServiceId);
            return;
        }

        // Execute api service.
        try {
            apiService.execute(this, intent.getExtras());
        } catch (ConnectException | UnknownHostException | InterruptedIOException e ) {
            Log.i(TAG, "Error executing request", e);
            apiService.onConnectionError(this, intent.getExtras(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error executing request", e);
            apiService.onApplicationError(this, intent.getExtras(), e);
        }
    }
}
