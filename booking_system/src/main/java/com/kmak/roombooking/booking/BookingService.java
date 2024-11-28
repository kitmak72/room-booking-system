package com.kmak.roombooking.booking;

import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingQueue bookingQueue;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final TransactionTemplate transactionTemplate;

    public BookingService(BookingQueue bookingQueue, BookingRepository bookingRepository, RoomRepository roomRepository, PlatformTransactionManager transactionManager) {
        this.bookingQueue = bookingQueue;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Optional<Booking> getBooking(long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public Long createNewBooking(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        validateBookingTime(startTime, endTime);
        Booking newBooking = transactionTemplate.execute(status -> {
            if (!roomRepository.existsById(roomId)) {
                throw new InvalidBookingException("Room not found");
            }
            Booking booking = new Booking();
            booking.setRoom(roomRepository.getReferenceById(roomId));
            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
            booking.setBookingStatus(BookingStatus.PENDING);
            return bookingRepository.save(booking);
        });
        bookingQueue.add(newBooking);
        return newBooking.getBookingId();
    }

    @Transactional
    public void settlePendingBooking(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            return;
        }
        if (bookingRepository.existsConflictingBookings(booking.getRoom().getRoomId(), booking.getStartTime(), booking.getEndTime())) {
            booking.setBookingStatus(BookingStatus.REJECTED);
        } else {
            booking.setBookingStatus(BookingStatus.ACCEPTED);
        }
        bookingRepository.save(booking);
    }

    private void validateBookingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new InvalidBookingException("Start time cannot be after end time");
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("Start time: {}, End time: {}, Now: {}", startTime, endTime, now);
        if (startTime.isBefore(now) || endTime.isBefore(now)) {
            throw new InvalidBookingException("Booking times must be in the future");
        }

        if (!isWeekday(startTime) && !isWeekday(endTime) ||
                !isWithinBusinessHours(startTime) &&
                        !isWithinBusinessHours(endTime)
        ) {
            throw new InvalidBookingException("Booking must be within business hours");
        }

    }

    private boolean isWithinBusinessHours(LocalDateTime dateTime) {
        LocalTime startBusinessHours = LocalTime.of(8, 0);
        LocalTime endBusinessHours = LocalTime.of(18, 0);
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(startBusinessHours) && time.isBefore(endBusinessHours);
    }

    private boolean isWeekday(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return !day.equals(DayOfWeek.SATURDAY) && !day.equals(DayOfWeek.SUNDAY);
    }

}
