/**
 * 
 */
package com.mytpg.engines.entities.stops;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONObject;

/**
 * @author stalker-mac
 *
 */
public class Mnemo extends EntityWithName {

	/**
	 * 
	 */
	public Mnemo() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 */
	public Mnemo(long ArgId) {
		super(ArgId);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgName
	 */
	public Mnemo(String ArgName) {
		super(ArgName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 * @param ArgName
	 */
	public Mnemo(long ArgId, String ArgName) {
		super(ArgId, ArgName);
		// TODO Auto-generated constructor stub
	}

	public Mnemo(Mnemo ArgMnemo) {
		setId(ArgMnemo.getId());
		setName(ArgMnemo.getName());
	}

    protected Mnemo(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
	public void fromJson(JSONObject ArgJsonObj) {
		if (ArgJsonObj == null)
		{
			return;
		}
		
	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Mnemo> CREATOR = new Parcelable.Creator<Mnemo>() {
        @Override
        public Mnemo createFromParcel(Parcel in) {
            return new Mnemo(in);
        }

        @Override
        public Mnemo[] newArray(int size) {
            return new Mnemo[size];
        }
    };

}
