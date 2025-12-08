package com.cinema.strategy;

import com.cinema.model.Show;
import com.cinema.model.Seat;
import com.cinema.model.VIPSeat;
import com.cinema.model.DiscountSeat;

public class StandardPricing implements PricingStrategy {
    
    @Override
    public double calculatePrice(Show show, Seat seat) {
        double basePrice = show.getBasePrice();
        
        // 根据座位类型调整价格
        if (seat instanceof VIPSeat) {
            // VIP座位价格为基准价格的2倍
            basePrice *= 2.0;
        } else if (seat instanceof DiscountSeat) {
            // 优惠座位价格为基准价格的80%
            basePrice *= 0.8;
        }
        // 普通座位保持基准价格不变
        
        // Weekend pricing (20% increase)
        if (isWeekend(show.getStartTime().getDayOfWeek().getValue())) {
            basePrice *= 1.2;
        }
        
        // Evening pricing (after 6 PM, 15% increase)
        if (show.getStartTime().getHour() >= 18) {
            basePrice *= 1.15;
        }
        
        return basePrice;
    }
    
    private boolean isWeekend(int dayOfWeek) {
        // In Java DayOfWeek: 1=Monday, 7=Sunday
        return dayOfWeek == 6 || dayOfWeek == 7;
    }
}