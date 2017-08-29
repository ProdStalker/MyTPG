/**
 * 
 */
package com.mytpg.engines.entities.vehicles.core;

import android.os.Parcel;
import android.os.Parcelable;

import com.mytpg.engines.entities.core.EntityWithName;

import org.json.JSONObject;

/**
 * @author stalker-mac
 *
 */
public class Vehicle extends EntityWithName {
	public enum VehicleType {None, Bus, Tramway, TrolleyBus}
	
	private VehicleType m_vehicleType = VehicleType.None;
	
	/**
	 * 
	 */
	public Vehicle() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 */
	public Vehicle(long ArgId) {
		super(ArgId);
	}

	/**
	 * @param ArgName
	 */
	public Vehicle(String ArgName) {
		super(ArgName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ArgId
	 * @param ArgName
	 */
	public Vehicle(long ArgId, String ArgName) {
		super(ArgId, ArgName);
		// TODO Auto-generated constructor stub
	}
	
	public Vehicle(Vehicle ArgVehicle) {
		setId(ArgVehicle.getId());
		setName(ArgVehicle.getName());
		setVehicleType(ArgVehicle.getVehicleType());
	}

    protected Vehicle(Parcel in) {
        super(in);
        m_vehicleType = (VehicleType) in.readValue(VehicleType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

	public VehicleType getVehicleType()
	{
		return m_vehicleType;
	}
	
	public void setVehicleType(VehicleType ArgVehicleType)
	{
		m_vehicleType = ArgVehicleType;
	}

	@Override
	public void fromJson(JSONObject ArgJsonObj) {
		// TODO Auto-generated method stub
		
	}



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeValue(m_vehicleType);
    }

    public static final Parcelable.Creator<Vehicle> CREATOR = new Parcelable.Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

}
