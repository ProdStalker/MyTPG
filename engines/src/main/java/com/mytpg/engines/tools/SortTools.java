package com.mytpg.engines.tools;

import android.location.Location;

import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.entities.core.EntityWithNameAndLocation;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by stalker-mac on 16.11.14.
 */
public abstract class SortTools {
    public enum FilterType {AZ, ZA}
    public enum FilterDistanceType {Close, Far}

    public static void sortEntityWithName(List<? extends EntityWithName> ArgEntityWithNameList, final FilterType ArgFilterType)
    {
        Collections.sort(ArgEntityWithNameList, new Comparator<EntityWithName>() {

            @Override
            public int compare(EntityWithName lhs, EntityWithName rhs) {
                String name = TextTools.removeAccent(lhs.getName());
                String name2 = TextTools.removeAccent(rhs.getName());

                if (name.length() == 1 && Character.isDigit(name.charAt(0)))
                {
                    name = "0" + name;
                }

                if (name2.length() == 1 && Character.isDigit(name2.charAt(0)))
                {
                    name2 = "0" + name;
                }

                int result = name.compareTo(name2);
                if (ArgFilterType == FilterType.ZA) {
                    result *= -1;
                }

                return result;
            }
        });
    }

    public static void sortEntityWithNameAndLocationByDistance(List<? extends EntityWithNameAndLocation> ArgEntityWithNameAndLocationList, final FilterDistanceType ArgFilterDistanceType, final Location argLoc)
    {
        if (argLoc == null)
        {
            return;
        }

        Collections.sort(ArgEntityWithNameAndLocationList, new Comparator<EntityWithNameAndLocation>() {

            @Override
            public int compare(EntityWithNameAndLocation lhs, EntityWithNameAndLocation rhs) {
                float distance = argLoc.distanceTo(lhs.getLocation());
                float distance2 = argLoc.distanceTo(rhs.getLocation());

                int result;
                if (distance < distance2)
                {
                    result = -1;
                }
                else if (distance == distance2)
                {
                    result = 0;
                }
                else
                {
                    result = 1;
                }

                if (ArgFilterDistanceType == FilterDistanceType.Far) {
                    result *= -1;
                }

                return result;
            }
        });
    }

    public static void sortStopsByDistance(Location argLoc, SortTools.FilterDistanceType argFilterDistanceType, List<Stop> argStops) {
        List<PhysicalStop> physicalStops = new ArrayList<>();

        for (Stop stop : argStops)
        {
            physicalStops.addAll(stop.getPhysicalStops());
            stop.getPhysicalStops().clear();
        }

        SortTools.sortEntityWithNameAndLocationByDistance(physicalStops, argFilterDistanceType, argLoc);

        for (PhysicalStop physicalStop : physicalStops)
        {
            for (Stop stop : argStops)
            {
                if (physicalStop.getStopId() == stop.getId())
                {
                    stop.getPhysicalStops().add(physicalStop);
                }
            }
        }
    }
}
