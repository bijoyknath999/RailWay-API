package com.rootwatchparty.railwayapi;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Train {

    @SerializedName("trip_number")
    private String tripNumber;

    @SerializedName("departure_date_time")
    private String departureDateTime;

    @SerializedName("arrival_date_time")
    private String arrivalDateTime;

    @SerializedName("travel_time")
    private String travelTime;

    @SerializedName("origin_city_name")
    private String originCityName;

    @SerializedName("destination_city_name")
    private String destinationCityName;

    @SerializedName("seat_types")
    private List<SeatType> seatTypes;

    // Getters
    public String getTripNumber() {
        return tripNumber;
    }

    public String getDepartureDateTime() {
        return departureDateTime;
    }

    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public String getOriginCityName() {
        return originCityName;
    }

    public String getDestinationCityName() {
        return destinationCityName;
    }

    public List<SeatType> getSeatTypes() {
        return seatTypes;
    }
}
