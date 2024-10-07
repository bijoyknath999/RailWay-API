package com.rootwatchparty.railwayapi;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1.0/web/bookings/search-trips-v2")
    Call<TrainResponse> searchTrips(@Query("from_city") String fromCity,
                                    @Query("to_city") String toCity,
                                    @Query("date_of_journey") String doj,
                                    @Query("seat_class") String seatClass);
}
