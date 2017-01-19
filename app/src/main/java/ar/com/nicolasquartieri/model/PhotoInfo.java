package ar.com.nicolasquartieri.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class PhotoInfo implements Parcelable {
    private String id;
    private String secret;
    private String server;
    private Integer farm;
    private Title title;
    private String datePost;
	private Description description;
	private Owner owner;

    public PhotoInfo() {
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public Integer getFarm() {
        return farm;
    }

    public Title getTitle() {
        return title;
    }

    public String getDatePost() {
        return datePost;
    }

    public Description getDescription() {
        return description;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setFarm(Integer farm) {
        this.farm = farm;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public void setDatePost(String datePost) {
        this.datePost = datePost;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    protected PhotoInfo(Parcel in) {
        id = in.readString();
        secret = in.readString();
        server = in.readString();
        farm = in.readByte() == 0x00 ? null : in.readInt();
        title = (Title) in.readValue(Title.class.getClassLoader());
        datePost = in.readString();
        description = (Description) in.readValue(Description.class.getClassLoader());
        owner = (Owner) in.readValue(Owner.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(secret);
        dest.writeString(server);
        if (farm == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(farm);
        }
        dest.writeValue(title);
        dest.writeString(datePost);
        dest.writeValue(description);
        dest.writeValue(owner);
    }

    @SuppressWarnings("unused")
    public static final Creator<PhotoInfo> CREATOR = new Creator<PhotoInfo>() {
        @Override
        public PhotoInfo createFromParcel(Parcel in) {
            return new PhotoInfo(in);
        }

        @Override
        public PhotoInfo[] newArray(int size) {
            return new PhotoInfo[size];
        }
    };

    public String getAvatarImageURL() {
//        http://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg
        String urlString = "http://farm{icon-farm}.staticflickr.com/{icon-server}/buddyicons/{nsid}.jpg";
        urlString = urlString.replace("{icon-farm}", getOwner().getIconfarm());
        urlString = urlString.replace("{icon-server}", getOwner().getIconserver());
        urlString = urlString.replace("{nsid}", getOwner().getNsid());
        return urlString;
    }
}
