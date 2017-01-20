package ar.com.nicolasquartieri.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import ar.com.nicolasquartieri.model.Description;
import ar.com.nicolasquartieri.model.Owner;
import ar.com.nicolasquartieri.model.Photo;
import ar.com.nicolasquartieri.model.PhotoInfo;
import ar.com.nicolasquartieri.model.Title;

/**
 * Helper to store and retrieve the {@link Photo} in the database.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class PhotoInfoTable {
    /** Table name. */
    public static final String TABLE = "photo_info";
    /** Id column. */
    public static final String COLUMN_ID = "id";
    /** farm column. */
    public static final String COLUMN_USERNAME = "username";
    /** farm column. */
    public static final String COLUMN_REALNAME = "realname";
    /** server column. */
    public static final String COLUMN_DESCRIPTION = "description";
    /** secret column. */
    public static final String COLUMN_TITLE = "title";
    /** secret column. */
    public static final String COLUMN_POST_DATE = "post_date";
    /** secret column. */
    public static final String COLUMN_ICON_FARM = "icon_farm";
    /** secret column. */
    public static final String COLUMN_ICON_SERVER = "icon_server";
    /** secret column. */
    public static final String COLUMN_NSID = "nsid";
    /** Create table statement. */
    public static final String CREATE =
            "CREATE TABLE " + TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ID + " TEXT NOT NULL, "
                    + COLUMN_USERNAME + " TEXT NULL, "
                    + COLUMN_REALNAME + " TEXT NULL, "
                    + COLUMN_DESCRIPTION + " TEXT NULL, "
                    + COLUMN_TITLE + " TEXT NULL, "
                    + COLUMN_POST_DATE + " TEXT NULL, "
                    + COLUMN_ICON_FARM + " TEXT NOT NULL, "
                    + COLUMN_ICON_SERVER + " TEXT NOT NULL, "
                    + COLUMN_NSID + " TEXT NOT NULL, "
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
     * @param photoInfo the user from which the ContentValues should be created,
     * cannot be null.
     * @return a {@link ContentValues} with {@link Photo} data,
     * never null.
     */
    public static ContentValues toContentValues(final PhotoInfo photoInfo) {
        // Create content values.
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, photoInfo.getId());
        values.put(COLUMN_DESCRIPTION, photoInfo.getDescription().getContent());
        values.put(COLUMN_POST_DATE, photoInfo.getDatePost());
        values.put(COLUMN_REALNAME, photoInfo.getOwner().getRealname());
        values.put(COLUMN_USERNAME, photoInfo.getOwner().getUsername());
        values.put(COLUMN_TITLE, photoInfo.getTitle().getContent());
        values.put(COLUMN_ICON_FARM, photoInfo.getOwner().getIconfarm());
        values.put(COLUMN_ICON_SERVER, photoInfo.getOwner().getIconserver());
        values.put(COLUMN_NSID, photoInfo.getOwner().getNsid());
        return values;
    }

    /**
     * Creates a {@link Photo} from given {@link Cursor}. This operation does not
     * close the cursor.
     * @param cursor the cursor from which the User will be created, can be null.
     * @return a {@link Photo} with {@link Cursor} data, null if the given
     * cursor is null or empty.
     */
    public static PhotoInfo parseCursor(Cursor cursor) {
        if (cursor == null || (cursor.getPosition() == -1 && !cursor.moveToNext())) {
            return null;
        }
        //TODO Refactor this with builder.
        PhotoInfo photoInfo = new PhotoInfo();
        photoInfo.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
        Description description = new Description();
        description.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
        photoInfo.setDescription(description);
        Title title = new Title();
        title.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        photoInfo.setTitle(title);
        photoInfo.setDatePost(cursor.getString(cursor.getColumnIndex(COLUMN_POST_DATE)));
        Owner owner = new Owner();
        owner.setNsid(cursor.getString(cursor.getColumnIndex(COLUMN_NSID)));
        owner.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)));
        owner.setRealname(cursor.getString(cursor.getColumnIndex(COLUMN_REALNAME)));
        owner.setIconfarm(cursor.getString(cursor.getColumnIndex(COLUMN_ICON_FARM)));
        owner.setIconserver(cursor.getString(cursor.getColumnIndex(COLUMN_ICON_SERVER)));
        photoInfo.setOwner(owner);
        return photoInfo;
    }
}
