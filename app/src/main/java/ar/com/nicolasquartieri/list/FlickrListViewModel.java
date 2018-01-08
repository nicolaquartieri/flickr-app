package ar.com.nicolasquartieri.list;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ar.com.nicolasquartieri.manager.ManagerService;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.Photos;
import ar.com.nicolasquartieri.model.RecentPhotos;
import ar.com.nicolasquartieri.remote.ApiErrorResponse;
import ar.com.nicolasquartieri.remote.ResponseType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlickrListViewModel extends ViewModel implements Callback<RecentPhotos> {
    private MutableLiveData<ResponseType<List<Photo>>> currentPhotos;
    private ManagerService managerService = ManagerService.getManagerService();

    public FlickrListViewModel() {
        managerService.callFlickrLatestPhotoService(1, null,this);
    }

    public void onPullRefresh() {
        managerService.callFlickrLatestPhotoService(1, null, this);
    }

    public void onLoadMoreElements(String mQuery, int mCurrentPage) {
        managerService.callFlickrLatestPhotoService(mCurrentPage, mQuery, this);
    }

    public MutableLiveData<ResponseType<List<Photo>>> getCurrentListPhoto() {
        if (currentPhotos == null) {
            currentPhotos = new MutableLiveData<>();
        }

        return currentPhotos;
    }

    private void setCurrentListPhoto(List<Photo> photos) {
        setCurrentListPhoto(photos, null);
    }

    private void setCurrentListPhoto(List<Photo> photos, @Nullable final ApiErrorResponse apiErrorResponse) {
        if (this.currentPhotos == null) {
            this.currentPhotos = new MutableLiveData<>();
        }

        this.currentPhotos.setValue(new ResponseType<List<Photo>>(photos, apiErrorResponse));
    }

    @Override
    public void onResponse(Call<RecentPhotos> call, Response<RecentPhotos> response) {
        List<Photo> photoList;
        if (response.isSuccessful()) {
            RecentPhotos recentPhotos = response.body();
            Photos photos = recentPhotos.getPhotos();
            if (photos != null) {
                photoList = photos.getPhoto();
                if (photoList != null && !photoList.isEmpty()) {
                    setCurrentListPhoto(photoList);
                }
            }
        } else {
            setCurrentListPhoto(null, new ApiErrorResponse(ApiErrorResponse.ERROR_SERVICE,
                    response.code()));
        }
    }

    @Override
    public void onFailure(Call<RecentPhotos> call, Throwable t) {
        setCurrentListPhoto(new ArrayList<Photo>(), new ApiErrorResponse(
                ApiErrorResponse.ERROR_CONNECTION));
    }
}
