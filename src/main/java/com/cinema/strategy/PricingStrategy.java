package com.cinema.strategy;

import com.cinema.model.Show;
import com.cinema.model.Seat;

public interface PricingStrategy {
    double calculatePrice(Show show, Seat seat);
}