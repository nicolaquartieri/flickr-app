package ar.com.nicolasquartieri.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class Photos implements Parcelable {
    private Integer page;
    private Integer pages;
    private Integer perpage;
    private String total;
    private List<Photo> photo = null;

    public Integer getPage() {
        return page;
    }

    public Integer getPages() {
        return pages;
    }

    public Integer getPerpage() {
        return perpage;
    }

    public String getTotal() {
        return total;
    }

    public List<Photo> getPhoto() {
        return photo;
    }

    protected Photos(Parcel in) {
        page = in.readByte() == 0x00 ? null : in.readInt();
        pages = in.readByte() == 0x00 ? null : in.readInt();
        perpage = in.readByte() == 0x00 ? null : in.readInt();
        total = in.readString();
        if (in.readByte() == 0x01) {
            photo = new ArrayList<Photo>();
            in.readList(photo, Photo.class.getClassLoader());
        } else {
            photo = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (page == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(page);
        }
        if (pages == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(pages);
        }
        if (perpage == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(perpage);
        }
        dest.writeString(total);
        if (photo == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(photo);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<Photos> CREATOR = new Creator<Photos>() {
        @Override
        public Photos createFromParcel(Parcel in) {
            return new Photos(in);
        }

        @Override
        public Photos[] newArray(int size) {
            return new Photos[size];
        }
    };
}
