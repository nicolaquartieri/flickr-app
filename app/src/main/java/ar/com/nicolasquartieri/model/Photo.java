package ar.com.nicolasquartieri.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class Photo implements Parcelable {
	private String id;
	private String secret;
	private String server;
	private Integer farm;
	private String title;
	private Integer ispublic;
	private Integer isfriend;
	private Integer isfamily;

	public Photo() {
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

	public String getTitle() {
		return title;
	}

	public Integer getIspublic() {
		return ispublic;
	}

	public Integer getIsfriend() {
		return isfriend;
	}

	public Integer getIsfamily() {
		return isfamily;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public void setFarm(Integer farm) {
		this.farm = farm;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImageURL() {
		String urlString = "http://farm{farm-id}.static.flickr.com/{server}/{id}_{secret}.jpg";
		urlString = urlString.replace("{farm-id}", farm.toString());
		urlString = urlString.replace("{server}", server);
		urlString = urlString.replace("{id}", id);
		urlString = urlString.replace("{secret}", secret);
		return urlString;
	}

	protected Photo(Parcel in) {
		id = in.readString();
		secret = in.readString();
		server = in.readString();
		farm = in.readByte() == 0x00 ? null : in.readInt();
		title = in.readString();
		ispublic = in.readByte() == 0x00 ? null : in.readInt();
		isfriend = in.readByte() == 0x00 ? null : in.readInt();
		isfamily = in.readByte() == 0x00 ? null : in.readInt();
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
		dest.writeString(title);
		if (ispublic == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeInt(ispublic);
		}
		if (isfriend == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeInt(isfriend);
		}
		if (isfamily == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeInt(isfamily);
		}
	}

	@SuppressWarnings("unused")
	public static final Creator<Photo> CREATOR = new Creator<Photo>() {
		@Override
		public Photo createFromParcel(Parcel in) {
			return new Photo(in);
		}

		@Override
		public Photo[] newArray(int size) {
			return new Photo[size];
		}
	};
}
