package com.cinema.strategy;

import com.cinema.model.Show;
import com.cinema.model.Seat;
import com.cinema.model.VIPSeat;
import com.cinema.model.DiscountSeat;

public class StandardPricing implements PricingStrategy {

    @Override
    public double calculatePrice(Show show, Seat seat) {
        double basePrice;

        // 根据座位类型使用对应的价格
        if (seat instanceof VIPSeat) {
            basePrice = show.getVipPrice();
        } else if (seat instanceof DiscountSeat) {
            basePrice = show.getDiscountPrice();
        } else {
            basePrice = show.getBasePrice();
        }

        // Weekend pricing (20% increase)
        if (isWeekend(show.getStartTime().getDayOfWeek().getValue())) {
            basePrice *= 1.2;
        }

        // Evening pricing (after 6 PM, 15% increase)
        if (show.getStartTime().getHour() >= 18) {
            basePrice *= 1.15;
        }

        // 🔴 关键修复：将结果四舍五入到小数点后两位
        // 步骤：乘以 100，四舍五入取整，再除以 100
        long roundedPrice = Math.round(basePrice * 100);
        return roundedPrice / 100.0;
    }
    
    private boolean isWeekend(int dayOfWeek) {
        // In Java DayOfWeek: 1=Monday, 7=Sunday
        return dayOfWeek == 6 || dayOfWeek == 7;
    }
}