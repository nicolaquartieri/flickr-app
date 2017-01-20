package ar.com.nicolasquartieri.local;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import ar.com.nicolasquartieri.model.Photo;

/**
 * Helper to store and retrieve the {@link Photo} in the database.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class PhotoTable {
    /** Table name. */
    public static final String TABLE = "photo";
    /** Id column. */
    public static final String COLUMN_ID = "id";
    /** farm column. */
    public static final String COLUMN_FARM = "farm";
    /** server column. */
    public static final String COLUMN_SERVER = "server";
    /** secret column. */
    public static final String COLUMN_SECRET = "secret";
    /** Create table statement. */
    public static final String CREATE =
            "CREATE TABLE " + TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ID + " TEXT NOT NULL, "
                    + COLUMN_FARM + " TEXT NOT NULL, "
                    + COLUMN_SERVER + " TEXT NOT NULL, "
                    + COLUMN_SECRET + " TEXT NOT NULL, "
                    + "UNIQUE (" + COLUMN_ID + ") ON CONFLICT REPLACE);";

    /** Content Uri. */
    public static final Uri CONTENT_URI = AppContentProvider.BASE_CONTENT_URI.buildUpon()
            .appendPath(TABLE).build();
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

    /**
     * Create a {@link ContentValues} from given {@link Photo}.
     *
     * @param photo the user from which the ContentValues should be created,
     * cannot be null.
     * @return a {@link ContentValues} with {@link Photo} data,
     * never null.
     */
    public static ContentValues toContentValues(final Photo photo) {
        // Create content values.
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, photo.getId());
        values.put(COLUMN_FARM, photo.getFarm());
        values.put(COLUMN_SECRET, photo.getSecret());
        values.put(COLUMN_SERVER, photo.getServer());
        return values;
    }

    /**
     * Create a {@link List} of {@link Photo} from given {@link Cursor}.
     * @param cursor the cursor.
     * @return a {@link List} of {@link Photo} with {@link Cursor} data.
     */
    public static List<Photo> parseAllCursor(Cursor cursor) {
        ArrayList<Photo> list = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            //TODO Improve this.
            Photo photo = new Photo();
            photo.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            photo.setFarm(cursor.getInt(cursor.getColumnIndex(COLUMN_FARM)));
            photo.setSecret(cursor.getString(cursor.getColumnIndex(COLUMN_SECRET)));
            photo.setServer(cursor.getString(cursor.getColumnIndex(COLUMN_SERVER)));
            list.add(photo);
        }
        return list;
    }
}
