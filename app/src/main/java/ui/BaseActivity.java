package ui;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.FrameLayout;

import nicolasquartieri.com.ar.flickr_app.R;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class BaseActivity extends AppCompatActivity {
	protected CoordinatorLayout mCoordinatorLayout;
	protected AppBarLayout mAppBarLayout;
	protected Toolbar mToolbar;
	protected FrameLayout mContentView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_main);

		mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
		mContentView = (FrameLayout) findViewById(R.id.main_container);
		mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);

		mAppBarLayout.setBackgroundResource(android.R.color.white);
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setActionBarTitle(getCurrentTitle());
		// TODO
		// setActionBarIcon(R.mipmap.ic_launcher);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAppBarLayout = null;
		mContentView = null;
		mCoordinatorLayout = null;
	}

	protected String getCurrentTitle() {
		return null;
	}

	protected void setActionBarTitle(String title) {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(title);
		if (TextUtils.isEmpty(title)) {
			actionBar.setDisplayShowTitleEnabled(false);
		}
		else {
			actionBar.setDisplayShowTitleEnabled(true);
		}
	}

	public void setActionBarIcon(@DrawableRes int drawable) {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(drawable);
	}
}
