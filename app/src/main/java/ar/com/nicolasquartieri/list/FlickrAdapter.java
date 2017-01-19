package ar.com.nicolasquartieri.list;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.detail.FlickrDetailActivity;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.widget.LoadingImageView;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
class FlickrAdapter extends RecyclerView.Adapter<FlickrAdapter.PhotoHolder> {

	private final Fragment fragment;

	private List<Photo> photos;

	FlickrAdapter(final Fragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		View photoElementView = inflater.inflate(R.layout.photo_recycle_grid_element, parent,
				false);
		return new PhotoHolder(photoElementView);
	}

	@Override
	public void onBindViewHolder(PhotoHolder holder, int position) {
		final Photo photo = photos.get(position);

		holder.flickrImageView.setImageUrl(photo.getImageURL());
	}

	@Override
	public int getItemCount() {
		return photos != null && photos.size() > 0 ? photos.size() : 0;
	}

	public void setPhotos(List<Photo> photos) {
		if (photos != null) {
			this.photos = photos;
		} else {
			this.photos.clear();
		}
		notifyDataSetChanged();
	}

	public void addPhotos(List<Photo> photos) {
		int currentSize = this.photos.size();
		if (photos != null) {
			this.photos.addAll(photos);
		} else {
			this.photos.clear();
		}
		notifyItemRangeInserted(currentSize, this.photos.size() - 1);
	}

	class PhotoHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener {
		LoadingImageView flickrImageView;

		PhotoHolder(View view) {
			super(view);

			this.flickrImageView = (LoadingImageView) view.findViewById(R.id.flickr_img);

			view.setOnClickListener(this);
		}

		@Override
        public void onClick(View v) {
			Photo photo = photos.get(getAdapterPosition());
            if (photo != null) {
				Activity activity = fragment.getActivity();
				activity.startActivity(FlickrDetailActivity.getIntent(activity, photo));
            }
        }
	}
}
