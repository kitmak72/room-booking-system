package com.kmak.roombooking.booking.model;

import java.time.LocalDateTime;

public record BookingRequest(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
}