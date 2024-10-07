package com.rootwatchparty.railwayapi;


import com.google.gson.annotations.SerializedName;

public class SeatType {

    @SerializedName("type")
    private String type;

    @SerializedName("fare")
    private String fare;

    @SerializedName("seat_counts")
    private SeatCounts seatCounts;

    // Getters
    public String getType() {
        return type;
    }

    public String getFare() {
        return fare;
    }

    public SeatCounts getSeatCounts() {
        return seatCounts;
    }

    public static class SeatCounts {
        @SerializedName("online")
        private int online;

        @SerializedName("offline")
        private int offline;

        // Getters
        public int getOnline() {
            return online;
        }

        public int getOffline() {
            return offline;
        }
    }
}
