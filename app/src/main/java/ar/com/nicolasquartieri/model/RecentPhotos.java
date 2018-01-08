package ar.com.nicolasquartieri.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class RecentPhotos implements Parcelable {
    private Photos photos;
    private String stat;

    protected RecentPhotos(Parcel in) {
        photos = (Photos) in.readValue(Photos.class.getClassLoader());
        stat = in.readString();
    }

    public Photos getPhotos() {
        return photos;
    }

    public void setPhotos(Photos photos) {
        this.photos = photos;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(photos);
        dest.writeString(stat);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RecentPhotos> CREATOR = new Parcelable.Creator<RecentPhotos>() {
        @Override
        public RecentPhotos createFromParcel(Parcel in) {
            return new RecentPhotos(in);
        }

        @Override
        public RecentPhotos[] newArray(int size) {
            return new RecentPhotos[size];
        }
    };
}
