package com.rootwatchparty.railwayapi;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrainResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {

        @SerializedName("trains")
        private List<Train> trains;

        public List<Train> getTrains() {
            return trains;
        }
    }
}
