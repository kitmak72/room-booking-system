package com.kmak.roombooking.booking;

import com.kmak.roombooking.booking.model.Booking;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class BookingQueue {

    private final BookingRepository bookingRepository;
    private final BlockingQueue<Booking> queue = new LinkedBlockingQueue<>();

    public BookingQueue(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostConstruct
    public void init() {
        queue.addAll(bookingRepository.findAllPendingBookings());
    }

    public void add(Booking booking) {
        queue.add(booking);
    }

    public Booking consume() throws InterruptedException {
        return queue.take();
    }

}
