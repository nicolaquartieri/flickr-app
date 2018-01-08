package ar.com.nicolasquartieri.list;

import java.util.List;

import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.RecentPhotos;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FlickrListService {
    @GET("services/rest?method=flickr.photos.getRecent&per_page=30&format=json&nojsoncallback=1")
    Call<RecentPhotos> getLatestPhotos(@Query("api_key") String apiKey,
                                       @Query("page") String page,
                                       @Query("text") String text);
}
