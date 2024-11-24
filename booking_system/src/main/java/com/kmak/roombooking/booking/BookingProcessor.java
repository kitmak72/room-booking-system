package com.kmak.roombooking.booking;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BookingProcessor {

    private final BookingQueue bookingQueue;
    private final BookingService bookingService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Logger logger = LoggerFactory.getLogger(BookingProcessor.class);

    public BookingProcessor(BookingQueue bookingQueue, BookingService bookingService) {
        this.bookingQueue = bookingQueue;
        this.bookingService = bookingService;
    }

    @PostConstruct
    public void start() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var booking = bookingQueue.consume();
                    logger.info("Processing booking {}", booking.getBookingId());
                    bookingService.settlePendingBooking(booking);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }
}
