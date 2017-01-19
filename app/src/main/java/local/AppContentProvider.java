package local;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Application {@link ContentProvider}.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class AppContentProvider extends ContentProvider {
    /** Log TAG. */
    private static final String TAG = AppContentProvider.class.getSimpleName();
    /** The authority for app contents. */
    public static final String CONTENT_AUTHORITY = "ar.com.nicolasquartieri.app.provider";
    /** Base URI to access provider's content. */
    protected static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /** Base content type. */
    protected static final String BASE_CONTENT_TYPE = "vnd.flickr.app.dir/vnd.appflickr.";
    /** Base item Content type. */
    protected static final String BASE_CONTENT_ITEM_TYPE = "vnd.flickr.app.item/vnd.appflickr.";
    /**
     * {@link UriMatcher} to determine what is requested to this
     * {@link ContentProvider}.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    /** URI ID to get all cache states. */
    private static final int CACHE_STATE = 100;
    /** URI ID to get a cache state. */
    private static final int CACHE_STATE_ID = 101;
    /** URI ID to get all photos. */
    private static final int PHOTO = 200;
    /** URI ID to get a photo. */
    private static final int PHOTO_ID = 201;
    /** URI ID to get all photos. */
    private static final int PHOTO_INFO = 300;
    /** URI ID to get a photo. */
    private static final int PHOTO_INFO_ID = 301;

    /** Local DB Helper */
    private AppDatabase mOpenHelper;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, CacheStateTable.TABLE, CACHE_STATE);
        matcher.addURI(authority, CacheStateTable.TABLE + "/*", CACHE_STATE);
        matcher.addURI(authority, PhotoTable.TABLE, PHOTO);
        matcher.addURI(authority, PhotoInfoTable.TABLE, PHOTO_INFO);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AppDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_STATE:
                return CacheStateTable.CONTENT_TYPE;
            case CACHE_STATE_ID:
                return CacheStateTable.CONTENT_ITEM_TYPE;
            case PHOTO:
                return PhotoTable.CONTENT_TYPE;
            case PHOTO_ID:
                return PhotoTable.CONTENT_ITEM_TYPE;
            case PHOTO_INFO:
                return PhotoInfoTable.CONTENT_TYPE;
            case PHOTO_INFO_ID:
                return PhotoInfoTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "uri=" + uri + " match=" + match + " proj="
                    + Arrays.toString(projection) + " selection=" + selection
                    + " args=" + Arrays.toString(selectionArgs) + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildExpandedSelection(uri, match);

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, projection, sortOrder);
        // Tell the cursor what uri to watch, so it knows when its source
        // data changes
        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "insert(uri=" + uri + ", values="
                    + values.toString() + ")");
        }

        // Get the database and run the insert
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case CACHE_STATE: {
                long id = db.insertOrThrow(CacheStateTable.TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return CacheStateTable.buildUri(Long.toString(id));
            }
            case PHOTO: {
                long id = db.insertOrThrow(PhotoTable.TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return PhotoTable.buildUri(Long.toString(id));
            }
            case PHOTO_INFO: {
                long id = db.insertOrThrow(PhotoInfoTable.TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return PhotoInfoTable.buildUri(Long.toString(id));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "delete(uri=" + uri + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildSimpleSelection(uri, match);

        // Get the database and run the delete
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = builder.where(selection, selectionArgs).delete(db);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Find matching path.
        final int match = sUriMatcher.match(uri);

        // Avoid the expensive string concatenation below if not loggable
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "update(uri=" + uri + ", values="
                    + values.toString() + ")");
        }

        // Create a selection builder from Uri.
        final SelectionBuilder builder = buildSimpleSelection(uri, match);

        // Get the database and run the update
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Transactional implementation of applyBatch.
     */
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        ContentProviderResult[] result = new ContentProviderResult[operations
                .size()];
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Begin a transaction
        db.beginTransaction();
        try {
            int i = 0;
            for (ContentProviderOperation operation : operations) {
                // Chain the result for back references
                result[i++] = operation.apply(this, result, i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support
     * {@link #insert}, {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case CACHE_STATE: {
                return builder.table(CacheStateTable.TABLE);
            }
            case CACHE_STATE_ID: {
                final String id = CacheStateTable.getId(uri);
                return builder.table(CacheStateTable.TABLE)
                    .where(BaseColumns._ID + "=?", id);
            }
            case PHOTO: {
                return builder.table(PhotoTable.TABLE);
            }
            case PHOTO_ID: {
                final String id = PhotoTable.getId(uri);
                return builder.table(PhotoTable.TABLE)
                        .where(PhotoTable.COLUMN_ID + "=?", id);
            }
            case PHOTO_INFO: {
                return builder.table(PhotoInfoTable.TABLE);
            }
            case PHOTO_INFO_ID: {
                final String id = PhotoInfoTable.getId(uri);
                return builder.table(PhotoInfoTable.TABLE)
                        .where(PhotoInfoTable.COLUMN_ID + "=?", id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for "
                        + match + ": " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        switch (match) {
            case CACHE_STATE:
            case CACHE_STATE_ID:
            case PHOTO:
            case PHOTO_ID:
            case PHOTO_INFO:
            case PHOTO_INFO_ID: {
                return buildSimpleSelection(uri, match);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
