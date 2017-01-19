package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class Owner implements Parcelable {
    private String username;
    private String location;
    private String realname;
    private String nsid;
    private String iconserver;
    private String iconfarm;

    public Owner() {
    }

    public String getUsername() {
        return username;
    }

    public String getLocation() {
        return location;
    }

    public String getRealname() {
        return realname;
    }

    public String getNsid() {
        return nsid;
    }

    public String getIconserver() {
        return iconserver;
    }

    public String getIconfarm() {
        return iconfarm;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }

    public void setIconserver(String iconserver) {
        this.iconserver = iconserver;
    }

    public void setIconfarm(String iconfarm) {
        this.iconfarm = iconfarm;
    }

    protected Owner(Parcel in) {
        username = in.readString();
        location = in.readString();
        realname = in.readString();
        nsid = in.readString();
        iconfarm = in.readString();
        iconserver = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(location);
        dest.writeString(realname);
        dest.writeString(nsid);
        dest.writeString(iconfarm);
        dest.writeString(iconserver);
    }

    @SuppressWarnings("unused")
    public static final Creator<Owner> CREATOR = new Creator<Owner>() {
        @Override
        public Owner createFromParcel(Parcel in) {
            return new Owner(in);
        }

        @Override
        public Owner[] newArray(int size) {
            return new Owner[size];
        }
    };
}
