package com.mytpg.engines.entities.core;

import android.location.Location;
import android.os.Parcel;

import com.mytpg.engines.settings.LocationSettings;

/**
 * Created by stalker-mac on 08.11.14.
 */
public abstract class EntityWithNameAndLocation extends EntityWithName {
    private Location mLoc = new Location(LocationSettings.LOCATION_PROVIDER);

    /**
     *
     */
    public EntityWithNameAndLocation() {

        super();
    }

    /**
     *
     * @param ArgId
     */
    public EntityWithNameAndLocation(long ArgId) {

        super(ArgId);
    }

    /**
     *
     * @param ArgName
     */
    public EntityWithNameAndLocation(String ArgName){
        super(ArgName);
    }

    /**
     *
     * @param ArgId
     * @param ArgName
     */
    public EntityWithNameAndLocation(long ArgId, String ArgName){
        super(ArgId, ArgName);
    }

    public EntityWithNameAndLocation(Location argLoc)
    {
        super();
        setLocation(argLoc);
    }

    public EntityWithNameAndLocation(EntityWithNameAndLocation argEntityWithNameAndLocation)
    {
        super(argEntityWithNameAndLocation);
        setLocation(new Location(argEntityWithNameAndLocation.getLocation()));
    }

    protected EntityWithNameAndLocation(Parcel in) {
        super(in);
        mLoc = (Location) in.readValue(Location.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Location getLocation()
    {
        return mLoc;
    }

    public void setLocation(Location argLoc)
    {
        if (argLoc == null)
        {
            argLoc = new Location(LocationSettings.LOCATION_PROVIDER);
        }
        mLoc = argLoc;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);

        dest.writeValue(mLoc);
    }


}
