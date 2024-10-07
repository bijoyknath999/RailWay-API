package com.rootwatchparty.railwayapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.TrainViewHolder> {

    private final List<Train> trainList;
    private Context context;

    public TrainAdapter(List<Train> trainList, Context context) {
        this.trainList = trainList;
        this.context = context;
    }

    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.train_item, parent, false);
        return new TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder holder, int position) {
        Train train = trainList.get(position);
        holder.tripNumber.setText(train.getTripNumber());
        holder.departureTime.setText(train.getDepartureDateTime());
        holder.arrivalTime.setText(train.getArrivalDateTime());
        holder.travelTime.setText(train.getTravelTime());
        holder.originCity.setText(train.getOriginCityName());
        holder.destinationCity.setText(train.getDestinationCityName());

        holder.seatView.setLayoutManager(new LinearLayoutManager(context));
        SeatAdapter seatAdapter = new SeatAdapter(train.getSeatTypes());
        holder.seatView.setAdapter(seatAdapter);
        seatAdapter.notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return trainList.size();
    }

    static class TrainViewHolder extends RecyclerView.ViewHolder {
        TextView tripNumber, departureTime, arrivalTime, travelTime, originCity, destinationCity;
        RecyclerView seatView;

        TrainViewHolder(View itemView) {
            super(itemView);
            tripNumber = itemView.findViewById(R.id.trip_number);
            departureTime = itemView.findViewById(R.id.departure_time);
            arrivalTime = itemView.findViewById(R.id.arrival_time);
            travelTime = itemView.findViewById(R.id.travel_time);
            originCity = itemView.findViewById(R.id.origin_city);
            destinationCity = itemView.findViewById(R.id.destination_city);
            seatView = itemView.findViewById(R.id.seatList);
        }
    }
}
