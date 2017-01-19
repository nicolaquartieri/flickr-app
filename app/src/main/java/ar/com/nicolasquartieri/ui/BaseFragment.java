package ar.com.nicolasquartieri.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.remote.ApiErrorResponse;
import ar.com.nicolasquartieri.remote.ApiService;
import ar.com.nicolasquartieri.ui.utils.AnimationUtils;

public class BaseFragment extends Fragment {
    /** Fragment loading state. */
    private static final String STATE_LOADING_STATE = "STATE_LOADING_STATE";
    /** Error message state. */
    private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";
    /** Pending response state. */
    private static final String STATE_PENDING_RESPONSE = "STATE_PENDING_RESPONSE";
    /** Pending user logger response state. */
    private static final String STATE_PENDING_USER_LOGGED = "STATE_PENDING_USER_LOGGED";

    /** Idle loading state. */
    private static final int LOADING_STATE_IDLE = 0;
    /** Running loading state. */
    private static final int LOADING_STATE_RUNNING = 1;
    /** Finished loading state. */
    private static final int LOADING_STATE_FINISHED = 2;
    /** Error loading state. */
    private static final int LOADING_STATE_ERROR = 3;

    /** Flag which indicates that response is loading. */
    private static final String EXTRA_IS_LOADING = "EXTRA_IS_LOADING";

    /** Fragment loading state. */
    private int mLoadingState = LOADING_STATE_IDLE;
    /** Error message. */
    private String mErrorMessage;
    /** Pending loading response. */
    private Intent mPendingResponse;
    /** Receiver to handle loading response. */
    private BroadcastReceiver mLoadingResponseReceiver;

    /** Flag which indicates that a user logged response is pending. */
    private boolean mPendingUserLogged = false;
    /** Receiver to handle user login. */
    private BroadcastReceiver mUserLoggedReceiver;

    /** Loading view. */
    private View mLoadingView;
    /** Loading error view. */
    private View mLoadingErrorView;
    /** Loading Error message view. */
    private TextView mLoadingErrorMessageView;
    /** Loading retry button. */
    private View mRetryButton;
    /** Error message for none loading errors, null unless an error is displayed. */
    private Snackbar mErrorMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set retain instance if it is not a nested fragment.
        // Nested fragment inherit retain instance form its parent.
        if (getParentFragment() == null) {
            setRetainInstance(true);
        }

        if (savedInstanceState != null) {
            mLoadingState = savedInstanceState.getInt(STATE_LOADING_STATE);
            mErrorMessage = savedInstanceState.getString(STATE_ERROR_MESSAGE);
            mPendingResponse = (Intent) savedInstanceState.getParcelable(STATE_PENDING_RESPONSE);
            mPendingUserLogged = savedInstanceState.getBoolean(STATE_PENDING_USER_LOGGED, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingView = view.findViewById(R.id.loading);
        mLoadingErrorView = view.findViewById(R.id.loading_error);
        mLoadingErrorMessageView = (TextView) view.findViewById(R.id.error_message);
        mRetryButton = view.findViewById(R.id.error_retry_button);
        // Setup retry button.
        if (mRetryButton != null) {
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRetryLoading();
                }
            });
        }
        // Recover error message.
        if (mLoadingErrorMessageView != null && mErrorMessage != null) {
            mLoadingErrorMessageView.setText(mErrorMessage);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateAdapter(getActivity().getApplicationContext());
        String action = onCreateLoadingResponseAction();
        onInitializeLoader(getLoaderManager());
        if (!TextUtils.isEmpty(action)) {
            registerLoadingResponseAction(action);
        }
        if (mUserLoggedReceiver == null) {
            mUserLoggedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Check if there is not an error.
                    if (intent.getExtras() != null) {
                        ApiErrorResponse apiErrorResponse = intent.getParcelableExtra(
                                ApiService.EXTRA_RESPONSE_ERROR);
                        if (apiErrorResponse != null) {
                            // If there was an error just ignore the response, we are only want to
                            // know if user has been changed.
                            return;
                        }
                    }
                    // Check if fragment is resumed.
                    if (!isResumed()) {
                        mPendingUserLogged = true;
                        return;
                    }
                    // Execute callback.
                    onUserLogged();
                }
            };
        }
        if (mLoadingState == LOADING_STATE_IDLE) {
            onStartLoading();
        }
    }

    /**
     * Called when adapter should be created if fragment use one.
     * @param context the context.
     */
    protected void onCreateAdapter(Context context) {
    }

    /**
     * Called when loader should be initialized if fragment use one.
     * @param manager the loader manager.
     */
    protected void onInitializeLoader(LoaderManager manager) {
    }

    /** Called to create the loading action receiver. */
    protected String onCreateLoadingResponseAction() {
        return null;
    }

    /** Called to start loading data from ar.com.nicolasquartieri.remote services. */
    protected void onStartLoading() {
        startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recover any pending response.
        if (mPendingResponse != null) {
            onLoadingResponse(mPendingResponse);
            mPendingResponse = null;
        }
        // Recover pending user logged response.
        if (mPendingUserLogged) {
            mPendingUserLogged = false;
            onUserLogged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_LOADING_STATE, mLoadingState);
        outState.putString(STATE_ERROR_MESSAGE, mErrorMessage);
        outState.putParcelable(STATE_PENDING_RESPONSE, mPendingResponse);
        outState.putBoolean(STATE_PENDING_USER_LOGGED, mPendingUserLogged);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoadingView = null;
        mLoadingErrorView = null;
        mRetryButton = null;
    }

    @Override
    public void onDestroy() {
        if (mUserLoggedReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                    mUserLoggedReceiver);
        }
        if (mLoadingResponseReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                    mLoadingResponseReceiver);
        }
        super.onDestroy();
    }

    /**
     * Register the a loading response action to default loading receiver.
     * @param action the action.
     */
    protected void registerLoadingResponseAction(String action) {
        if (mLoadingResponseReceiver == null) {
            mLoadingResponseReceiver  = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onLoadingResponse(intent);
                }
            };
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mLoadingResponseReceiver, new IntentFilter(action));
    }

    /**
     * Called when an API response is received.
     * @param intent the intent.
     */
    public void onLoadingResponse(Intent intent) {
        boolean isLoading = intent.getBooleanExtra(EXTRA_IS_LOADING, true);
        onLoadingResponse(intent, isLoading);
    }

    /**
     * Called when an API response is received.
     * @param intent the intent.
     */
    public boolean onLoadingResponse(Intent intent, boolean isLoading) {
        // Check if fragment is in foreground.
        if (!isResumed()) {
            intent.putExtra(EXTRA_IS_LOADING, isLoading);
            mPendingResponse = intent;
            return false;
        }

        if (intent.getExtras() != null) {
            ApiErrorResponse apiErrorResponse = intent.getParcelableExtra(
                    ApiService.EXTRA_RESPONSE_ERROR);
            if (apiErrorResponse != null) {
                switch (apiErrorResponse.getErrorType()) {
                    case ApiErrorResponse.ERROR_SERVICE:
                        if (isLoading) {
                            onLoadingServiceError(intent, apiErrorResponse);
                        } else {
                            showErrorMessage(apiErrorResponse.getErrorMessage());
                        }
                        break;
                    case ApiErrorResponse.ERROR_CONNECTION:
                        if (isLoading) {
                            onLoadingConnectionError(intent);
                        } else {
                            showErrorMessage(R.string.no_connection);
                        }
                        break;
                    case ApiErrorResponse.ERROR_APPLICATION:
                    default:
                        if (isLoading) {
                            onLoadingApplicationError(intent);
                        } else {
                            showErrorMessage(R.string.ops);
                        }
                        break;
                }
                return false;
            } else if (isLoading) {
                onLoadingFinished(intent);
                return false;
            }
        } else if (isLoading) {
            onLoadingFinished(intent);
            return false;
        }
        return true;
    }

    /**
     * Called when a connection error response id received.
     * @param intent the intent.
     */
    public void onLoadingConnectionError(Intent intent) {
        finishLoading();
        showLoadingError(R.string.no_connection);
    }

    /**
     * Called when an application error response id received.
     * @param intent the intent.
     */
    public void onLoadingApplicationError(Intent intent) {
        finishLoading();
        showLoadingError(R.string.ops);
    }

    /**
     * Called when an api service error response id received.
     * @param intent the intent.
     * @param apiErrorResponse the api service error.
     */
    public void onLoadingServiceError(Intent intent, ApiErrorResponse apiErrorResponse) {
        finishLoading();
        showLoadingError(apiErrorResponse.getErrorMessage());
    }

    public void onLoadingFinished(Intent intent) {
        finishLoading();
    }

    /** This is called when user request a retry loading. */
    protected void onRetryLoading() {
        onStartLoading();
    }

    /** This is called when user logged in. */
    protected void onUserLogged() {
    }

    /** Initialize a loader. */
    public void initLoader(LoaderManager manager, int id, Bundle args,
            LoaderManager.LoaderCallbacks callbacks) {
        if (manager.getLoader(id) != null) {
            manager.restartLoader(id, args, callbacks);
        } else {
            manager.initLoader(id, args, callbacks);
        }
    }

    /** Set the loading start to running and update view. */
    public void startLoading() {
        startLoading(false);
    }

    /**
     * Set the loading start to running and update view.
     * @param animate indicate if use fade in animation.
     */
    public void startLoading(boolean animate) {
        if (mLoadingState != LOADING_STATE_RUNNING) {
            mLoadingState = LOADING_STATE_RUNNING;
            updateLoadingView(animate);
        }
    }

    /** Set the loading start to idle and update view. */
    public void finishLoading() {
        finishLoading(true);
    }

    /**
     * Set the loading start to idle and update view.
     * @param animate indicate if use fade in animation.
     */
    public void finishLoading(boolean animate) {
        if (mLoadingState == LOADING_STATE_RUNNING) {
            mLoadingState = LOADING_STATE_FINISHED;
            updateLoadingView(animate);
        }
    }

    /**
     * Update view base on the loading state of the fragment.
     * In other words, it shows or hide loading view.
     */
    protected void updateLoadingView(boolean animate) {
        if (mLoadingView != null) {
            AnimationUtils.cancelAnimation(mLoadingView);
            switch (mLoadingState) {
                case LOADING_STATE_IDLE:
                case LOADING_STATE_FINISHED:
                    if (mLoadingErrorView != null) {
                        mLoadingErrorView.setVisibility(View.GONE);
                    }
                    if (animate) {
                        AnimationUtils.fadeOutView(mLoadingView);
                    } else {
                        mLoadingView.setVisibility(View.GONE);
                    }
                    break;
                case LOADING_STATE_RUNNING:
                    if (mLoadingErrorView != null) {
                        mLoadingErrorView.setVisibility(View.GONE);
                    }
                    if (animate) {
                        AnimationUtils.fadeInView(mLoadingView);
                    } else {
                        mLoadingView.setVisibility(View.VISIBLE);
                    }
                    break;
                case LOADING_STATE_ERROR:
                    mLoadingView.clearAnimation();
                    mLoadingView.setVisibility(View.GONE);
                    if (mLoadingErrorView != null) {
                        mLoadingErrorView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    /**
     * Shows a loading error.
     * @param resId the error message.
     */
    protected void showLoadingError(@StringRes int resId) {
        showLoadingError(resId, true);
    }

    /**
     * Shows a loading error.
     * @param resId the error message.
     * @param canRetry indicates if retry button should be shown.
     */
    protected void showLoadingError(@StringRes int resId, boolean canRetry) {
        if (mLoadingErrorMessageView != null) {
            mErrorMessage = getResources().getString(resId);
            mLoadingErrorMessageView.setText(mErrorMessage);
        }
        if (mRetryButton != null) {
            mRetryButton.setVisibility(canRetry ? View.VISIBLE : View.GONE);
        }
        if (mLoadingState != LOADING_STATE_ERROR) {
            mLoadingState = LOADING_STATE_ERROR;
            updateLoadingView(true);
        }
    }

    /**
     * Shows a {@link Snackbar} with given resource string.
     * @param resId The id of the resource to be shown.
     */
    protected void showErrorMessage(@StringRes final int resId) {
        showErrorMessage(getView().getResources().getString(resId));
    }

    /**
     * Shows a {@link Snackbar} with given resource string.
     * @param message The message to be shown.
     */
    protected void showErrorMessage(String message) {
        View rootView = getView();
        if(rootView.getParent() != null) {
            rootView = (View) rootView.getParent();
        }
        mErrorMessageView = Snackbar
                .make(rootView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Nothing to do. only to show a dismiss button.
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark));
        mErrorMessageView.show();
    }

    /** Dismiss the error message if there is one. */
    protected void dismissErrorMessage() {
        if (mErrorMessageView != null) {
            mErrorMessageView.dismiss();
            mErrorMessageView = null;
        }
    }

    /**
     * Indicates if loading is running.
     * @return true if loading is running.
     */
    protected boolean isLoadingRunning() {
        return mLoadingState == LOADING_STATE_RUNNING;
    }

    /**
     * Indicates if loading has finished.
     * @return true if loading has finished.
     */
    protected boolean isLoadingFinished() {
        return mLoadingState == LOADING_STATE_FINISHED;
    }

    /**
     * Set a title on actual {@link ActionBar}.
     * @param title The title, can be null.
     */
    protected void setActionBarTitle(String title) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            if (TextUtils.isEmpty(title)) {
                actionBar.setDisplayShowTitleEnabled(false);
            } else {
                actionBar.setDisplayShowTitleEnabled(true);
            }
        }
    }
}