package ar.com.nicolasquartieri.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.ui.BaseActivity;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrDetailActivity extends BaseActivity {

    public static Intent getIntent(@NonNull final Context context, Photo photo) {
        final Intent intent = new Intent(context, FlickrDetailActivity.class);
        intent.putExtra(FlickrDetailFragment.ARG_PHOTO, photo);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            final Bundle bundle = (getIntent().getExtras() != null)
                    ? getIntent().getExtras() : new Bundle();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, FlickrDetailFragment.newInstance(
                    (Photo) bundle.getParcelable(FlickrDetailFragment.ARG_PHOTO)));
            transaction.commit();
        }
    }

    @Override
    protected String getCurrentTitle() {
        return getString(R.string.title_detail);
    }
}
