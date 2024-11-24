package com.kmak.roombooking.booking.model;

public record BookingResponse(Long bookingId, String message) {
    public BookingResponse(Long bookingId) {
        this(bookingId, "Booking submitted");
    }
}
