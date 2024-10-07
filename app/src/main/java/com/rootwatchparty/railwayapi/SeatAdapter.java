package com.rootwatchparty.railwayapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.TrainViewHolder> {

    private final List<SeatType> seatTypeList;

    public SeatAdapter(List<SeatType> seatTypeList) {
        this.seatTypeList = seatTypeList;
    }

    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seat_item, parent, false);
        return new TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder holder, int position) {
        SeatType seatType = seatTypeList.get(position);
        holder.seat.setText(seatType.getType()+" : "+seatType.getSeatCounts().getOnline());
    }

    @Override
    public int getItemCount() {
        return seatTypeList.size();
    }

    static class TrainViewHolder extends RecyclerView.ViewHolder {
        TextView seat;

        TrainViewHolder(View itemView) {
            super(itemView);
            seat = itemView.findViewById(R.id.seat);
        }
    }
}
