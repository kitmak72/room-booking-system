package com.kmak.roombooking.booking.model;

public record BookingResponse(long bookingId, String message) {
    public BookingResponse(long bookingId) {
        this(bookingId, "Booking submitted");
    }
}
