package ar.com.nicolasquartieri.detail;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.local.PhotoInfoTable;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.PhotoInfo;
import ar.com.nicolasquartieri.remote.FlickrPhotoInfoApiService;
import ar.com.nicolasquartieri.ui.BaseFragment;
import ar.com.nicolasquartieri.ui.dialog.ImageDialog;
import ar.com.nicolasquartieri.widget.LoadingImageView;

/**
 * Display the information of the respective {@link PhotoInfo} & {@link Photo}.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrDetailFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int IMAGE_DIALOG_REQUEST_CODE = 1500;
    private static final String SUPPORT_DIALOG_TAG_NAME = "FLICKR_DETAIL_FRAGMENT_REQUEST";
    public static final String ARG_PHOTO = "ARG_PHOTO";

    private TextView mRealNameTextView;
    private TextView mTitleTextView;
    private LoadingImageView mFlickrImageView;
    private LoadingImageView mAvatarImageView;
    private TextView mDescriptionTextView;

    private Photo mPhoto;
    private PhotoInfo mPhotoInfo;

    public static Fragment newInstance(Photo photo) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PHOTO, photo);
        FlickrDetailFragment fragment = new FlickrDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhoto = getArguments().getParcelable(ARG_PHOTO);

        mRealNameTextView = (TextView) view.findViewById(R.id.realname_txt);
        mTitleTextView = (TextView) view.findViewById(R.id.title_txt);
        mFlickrImageView = (LoadingImageView) view.findViewById(R.id.flickr_img);
        mAvatarImageView = (LoadingImageView) view.findViewById(R.id.avatar_img);
        mDescriptionTextView = (TextView) view.findViewById(R.id.description_txt);

        if (mPhoto != null) {
            mFlickrImageView.setImageUrl(mPhoto.getImageURL());
            mFlickrImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageDialog imageDialog = ImageDialog.getImageDialog(mPhoto.getImageURL());
                    imageDialog.setTargetFragment(FlickrDetailFragment.this, IMAGE_DIALOG_REQUEST_CODE);
                    imageDialog.show(getFragmentManager(), SUPPORT_DIALOG_TAG_NAME);
                }
            });
        }

        if (mPhotoInfo != null) {
            mRealNameTextView.setText(mPhotoInfo.getOwner().getRealname());
            mTitleTextView.setText(mPhotoInfo.getTitle().getContent());
            mAvatarImageView.setImageUrl(mPhotoInfo.getAvatarImageURL());
            mDescriptionTextView.setText(mPhotoInfo.getDescription().getContent());
        }
    }

    @Override
    protected String onCreateLoadingResponseAction() {
        return FlickrPhotoInfoApiService.RESPONSE_ACTION;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        // Sync cause data from remote service.
        Intent intent = FlickrPhotoInfoApiService.newIntent(getActivity(), mPhoto);
        getActivity().startService(intent);
    }

    @Override
    protected void onInitializeLoader(LoaderManager manager) {
        super.onInitializeLoader(manager);
        initLoader(manager, R.id.loader_photos_detail, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), PhotoInfoTable.buildUri(), null,
                PhotoInfoTable.COLUMN_ID + "=?", new String[] { String.valueOf(mPhoto.getId()) }, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPhotoInfo = PhotoInfoTable.parseCursor(data);
        if (mPhotoInfo != null) {
            mRealNameTextView.setText(mPhotoInfo.getOwner().getRealname());
            mTitleTextView.setText(mPhotoInfo.getTitle().getContent());
            mAvatarImageView.setImageUrl(mPhotoInfo.getAvatarImageURL(), true);
            mDescriptionTextView.setText(mPhotoInfo.getDescription().getContent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Do nothing.
    }
}
