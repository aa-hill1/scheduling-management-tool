package com.oopModel.comparator;

import com.oopModel.matrix.Location;

import java.util.Comparator;

public class LocationComparator implements Comparator<Location> {

    @Override
    public int compare(Location locationOne, Location locationTwo) {
        if (locationOne.getDistance() < locationTwo.getDistance()) {
            return -1;
        } else if (locationOne.getDistance() > locationTwo.getDistance()) {
            return 1;
        } else {
            return 0;
        }
    }
}
