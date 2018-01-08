package ar.com.nicolasquartieri.manager;

import android.support.annotation.NonNull;

import java.util.List;

import ar.com.nicolasquartieri.list.FlickrListService;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.RecentPhotos;
import ar.com.nicolasquartieri.ui.utils.EnvironmentUtils;
import ar.com.nicolasquartieri.ui.utils.RetrofitProvider;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Manage the service layer for all of the available services of the app.
 *
 * @author Nicolas Quartieri (nicolas.quartieri@gmail.com)
 */
public class ManagerService {
    private static volatile ManagerService instance;
    private FlickrListService flickrListService;

    private ManagerService() {
        // Create the Services.
        Retrofit retrofit = RetrofitProvider.getRetrofitClient();
        flickrListService = retrofit.create(FlickrListService.class);
    }

    public static ManagerService getManagerService() {
        // Prevent race-condition on creation from multiple places.
        if (instance == null) {
            synchronized (ManagerService.class) {
                if (instance == null) {
                    instance = new ManagerService();
                }
            }
        }
        return instance;
    }

    /**
     * Get the list of available list of {@link ar.com.nicolasquartieri.model.Photo}.
     *
     * @param callback the list is returned in this {@link Callback}
     */
    public void callFlickrLatestPhotoService(int page, String query,
                                             @NonNull Callback<RecentPhotos> callback) {
        flickrListService.getLatestPhotos(EnvironmentUtils.getApiKey(), String.valueOf(page),
                query).enqueue(callback);
    }
}
