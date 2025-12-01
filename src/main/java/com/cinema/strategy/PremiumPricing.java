package com.cinema.strategy;

import com.cinema.model.Show;
import com.cinema.model.Seat;
import com.cinema.model.VIPSeat;

public class PremiumPricing implements PricingStrategy {
    
    @Override
    public double calculatePrice(Show show, Seat seat) {
        double basePrice = show.getBasePrice();
        
        // VIP seats get premium pricing
        if (seat instanceof VIPSeat) {
            basePrice *= 2.0;
        }
        
        // Weekend pricing (30% increase)
        if (isWeekend(show.getStartTime().getDayOfWeek().getValue())) {
            basePrice *= 1.3;
        }
        
        // Evening pricing (after 6 PM, 25% increase)
        if (show.getStartTime().getHour() >= 18) {
            basePrice *= 1.25;
        }
        
        // Prime time pricing (7-9 PM, additional 10%)
        if (show.getStartTime().getHour() >= 19 && show.getStartTime().getHour() <= 21) {
            basePrice *= 1.1;
        }
        
        return basePrice;
    }
    
    private boolean isWeekend(int dayOfWeek) {
        // In Java DayOfWeek: 1=Monday, 7=Sunday
        return dayOfWeek == 6 || dayOfWeek == 7;
    }
}