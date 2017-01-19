package local;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Helper to store the cache state in the database.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class CacheStateTable {
    /** Table name. */
    public static final String TABLE = "cache_state";
    /** Url. */
    public static final String URL = "url";
    /** Last update time. */
    public static final String LAST_UPDATE = "last_update";

    /** Create table statement. */
    public static final String CREATE =
            "CREATE TABLE " + TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + URL + " TEXT NOT NULL, "
                    + LAST_UPDATE + " INTEGER NOT NULL, "
                    + "UNIQUE (" + URL + ") ON CONFLICT REPLACE);";

    /** Content Uri. */
    public static final Uri CONTENT_URI =
            AppContentProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE).build();
    /** Content type. */
    public static final String CONTENT_TYPE = AppContentProvider.BASE_CONTENT_TYPE + TABLE;
    /** Item Content type. */
    public static final String CONTENT_ITEM_TYPE = AppContentProvider.BASE_CONTENT_ITEM_TYPE
            + TABLE;

    /** Build {@link Uri} for request all entities. */
    public static Uri buildUri() {
        return CONTENT_URI.buildUpon().build();
    }

    /** Build {@link Uri} for requested entity. */
    public static Uri buildUri(String id) {
        return CONTENT_URI.buildUpon().appendPath(id).build();
    }

    /** Extract the id from given {@link Uri} */
    public static final String getId(Uri uri) {
        return uri.getPathSegments().get(1);
    }
}
