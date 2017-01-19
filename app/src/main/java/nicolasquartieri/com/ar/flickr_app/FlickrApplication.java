package nicolasquartieri.com.ar.flickr_app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrApplication extends Application {
	/** Shared preferences name */
	private static final String SHARED_PREFERENCES = "ar.com.nicolasquartieri.Preferences";

	/** Shared preferences. */
	private static SharedPreferences sharedPreferences;

	/** Singleton instance. */
	private static FlickrApplication mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		// Initialize shared preferences.
		sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
	}

	/**
	 * Retrieves the app shared preferences.
	 *
	 * @return The shared preferences to be used within the app, never null.
	 */
	public static SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	/**
	 * Retrieve the application in a static way.
	 *
	 * @return the context, never null.
	 */
	public static Context getAppContext() {
		return mInstance;
	}
}
