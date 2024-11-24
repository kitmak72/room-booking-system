package com.kmak.roombooking.booking;

import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingRequest;
import com.kmak.roombooking.booking.model.BookingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}/status")
    public ResponseEntity<Booking> getBooking(@PathVariable Long bookingId) {
        var booking = bookingService.getBooking(bookingId);
        return booking.map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new")
    public BookingResponse newBooking(@RequestBody BookingRequest bookingRequest) {
        Long bookingId = bookingService.createNewBooking(bookingRequest.roomId(), bookingRequest.startTime(), bookingRequest.endTime());
        return new BookingResponse(bookingId);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<String> handleInvalidBooking(InvalidBookingException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
