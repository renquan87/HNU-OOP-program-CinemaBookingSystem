package com.cinema;

import com.cinema.ui.ConsoleUI;
import com.cinema.service.BookingService;
import com.cinema.service.CinemaManager;
import com.cinema.strategy.StandardPricing;

public class Main {
    public static void main(String[] args) {
        // Initialize BookingService first
        BookingService bookingService = BookingService.getInstance(new StandardPricing());
        
        // Then initialize CinemaManager
        CinemaManager cinemaManager = CinemaManager.getInstance();
        
        // Start the console UI
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}