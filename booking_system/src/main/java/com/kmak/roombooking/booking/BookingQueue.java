package com.kmak.roombooking.booking;

import com.kmak.roombooking.booking.model.Booking;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class BookingQueue {

    private static final Logger log = LoggerFactory.getLogger(BookingQueue.class);
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
        log.info("Adding booking {} to queue", booking.getBookingId());
        queue.add(booking);
    }

    public Booking consume() throws InterruptedException {
        return queue.take();
    }

}
