package ar.com.nicolasquartieri.list;

import java.util.List;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.local.AppDatabase;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.remote.FlickrSearchApiService;
import ar.com.nicolasquartieri.remote.ResponseType;
import ar.com.nicolasquartieri.ui.BaseFragment;
import ar.com.nicolasquartieri.ui.utils.AnimationUtils;
import ar.com.nicolasquartieri.ui.utils.KeyboardUtils;
import ar.com.nicolasquartieri.widget.recyclerview.ItemOffsetDecoration;

/**
 * Display the list of {@link Photo} collected from the {@link FlickrSearchApiService}.
 *
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class FlickrListFragment extends BaseFragment {

    /** Recycler View */
    private RecyclerView mRecyclerView;
    /** Recycler View Adapter */
    private FlickrAdapter mAdapter;
    /** Nothing Layout */
    private LinearLayout mNothingLayout;
    /** Amount of {@link Photo} columns in the list */
    private static final int COLUMNS = 3;
    /** Query search */
    private String mQuery;
    /** Grid Layout Manager */
    private GridLayoutManager mGridlayoutManager;
    /** Linear Layout Manager */
    private LinearLayoutManager mLinearLayoutManager;
    /** Grid Endless Recycler View Scroll Listener */
    private EndlessRecyclerViewScrollListener mGridEndlessRecyclerViewScrollListener;
    /** Linear Endless Recycler View Scroll Listener */
    private EndlessRecyclerViewScrollListener mLinearEndlessRecyclerViewScrollListener;
    /** Loading Fetch Bar  */
    private View mFetchBar;
    /** Swipe To Refresh */
    private SwipeRefreshLayout mSwipeToRefreshLayout;
    /** View Model */
    private FlickrListViewModel flickListViewModel;

    /**
     * New {@link FlickrListFragment} instance.
     *
     * @return The fragment.
     */
    public static FlickrListFragment newInstance() {
        return new FlickrListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewModel();
    }

    /**
     * Initiate all related view components of this screen.
     * @param view The actual {@link View} of this screen.
     */
    private void initView(View view) {
        mNothingLayout = (LinearLayout) view.findViewById(R.id.nothing_layout);
        mFetchBar = view.findViewById(R.id.fetch_bar);
        // Swipe to Refresh layout.
        mSwipeToRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flickListViewModel.onPullRefresh();
            }
        });
        mRecyclerView = (RecyclerView) view.findViewById(R.id.photo_list);
        // Grid Layout Manager.
        mGridlayoutManager = new GridLayoutManager(getActivity(), COLUMNS,
                GridLayoutManager.VERTICAL, false);
        // Linear Layout Manager.
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        // EndlessRecyclerViewScrollListener for Grid Layout Manager.
        mGridEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(mGridlayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                onLoadMoreElements(page);
            }
        };
        // EndlessRecyclerViewScrollListener for Linear Layout Manager.
        mLinearEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                onLoadMoreElements(page);
            }
        };
        mRecyclerView.addOnScrollListener(mGridEndlessRecyclerViewScrollListener);
        mRecyclerView.addOnScrollListener(mLinearEndlessRecyclerViewScrollListener);
        mRecyclerView.setLayoutManager(mGridlayoutManager);
        mRecyclerView
                .addItemDecoration(new ItemOffsetDecoration(getActivity(), R.dimen.m0_125));
        // Avoid open multiple wallpaper screen at the same time. just take the first one.
        mRecyclerView.setMotionEventSplittingEnabled(false);
    }

    /**
     * Initiate all related view models of this screen.
     */
    private void initViewModel() {
        // 1. Create ViewModel.
        flickListViewModel = ViewModelProviders.of(this).get(FlickrListViewModel.class);
        // 2. Creates the observer.
        Observer<ResponseType<List<Photo>>> observer = new Observer<ResponseType<List<Photo>>>() {
            @Override
            public void onChanged(@Nullable ResponseType<List<Photo>> response) {
                List<Photo> photos = response.getPlayload();
                if (photos != null && !photos.isEmpty()) {
                    mNothingLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mNothingLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
                mAdapter.addPhotos(photos);
                onLoadingResponse(new Intent().putExtras(response.getArgs()));
                finishLoading();
            }
        };
        // 2. Subscribe the observer.
        flickListViewModel.getCurrentListPhoto().observe(this, observer);
    }

    // TODO
    /**
     * Get more {@link Photo} through the respective service.
     * @param page The requested page to deliver.
     */
    private void onLoadMoreElements(int page) {
        AnimationUtils.fadeInView(mFetchBar);
        int mCurrentPage = page + 1;
        flickListViewModel.onLoadMoreElements(mQuery, mCurrentPage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(
                Context.SEARCH_SERVICE);

        MenuItem searchMenuItem = menu.findItem(R.id.search_item);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startLoading();
                // Clean DB.
                AppDatabase.cleanDB(getActivity().getContentResolver());
                mGridEndlessRecyclerViewScrollListener.resetState();
                mLinearEndlessRecyclerViewScrollListener.resetState();
                // Call new Search.
                mQuery = query;
                // TODO
//                mCurrentService = FlickrSearchApiService.newIntent(getActivity(), mQuery);
//                getActivity().startService(mCurrentService);
                // Hide softKeyboard.
                KeyboardUtils.hideSoftKeyboard(getActivity(), searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_item:
                // Switch between Grid & Linear Layout.
                RecyclerView.LayoutManager layoutManager;
                mRecyclerView.setAdapter(null);
                if (mRecyclerView.getLayoutManager() == mLinearLayoutManager) {
                    // Grid Screen.
                    item.setIcon(R.mipmap.ic_view_list);
                    layoutManager = mGridlayoutManager;
                } else {
                    // Linear Screen.
                    item.setIcon(R.mipmap.ic_view_module);
                    layoutManager = mLinearLayoutManager;
                }
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setAdapter(mAdapter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLoadingFinished(Intent intent) {
        super.onLoadingFinished(intent);
        mSwipeToRefreshLayout.setRefreshing(false);
        AnimationUtils.fadeOutView(mFetchBar);
    }

    @Override
    protected void onCreateAdapter(Context context) {
        super.onCreateAdapter(context);
        if (mAdapter == null) {
            mAdapter = new FlickrAdapter(this);
        }
        mRecyclerView.setAdapter(mAdapter);
    }
}
