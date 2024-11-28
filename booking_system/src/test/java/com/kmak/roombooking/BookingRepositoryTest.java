package com.kmak.roombooking;

import com.kmak.roombooking.booking.BookingRepository;
import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingStatus;
import com.kmak.roombooking.booking.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setRoomName("Room 101");
        entityManager.persist(room);
    }

    @Test
    void testFindAllPendingBookings() {
        Booking booking1 = new Booking();
        booking1.setRoom(room);
        booking1.setStartTime(LocalDateTime.now().plusDays(1));
        booking1.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        booking1.setBookingStatus(BookingStatus.PENDING);
        booking1.setRequestTime(LocalDateTime.now());
        entityManager.persist(booking1);

        Booking booking2 = new Booking();
        booking2.setRoom(room);
        booking2.setStartTime(LocalDateTime.now().plusDays(2));
        booking2.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        booking2.setBookingStatus(BookingStatus.PENDING);
        booking2.setRequestTime(LocalDateTime.now().plusMinutes(1));
        entityManager.persist(booking2);

        var pendingBookings = bookingRepository.findAllPendingBookings();
        assertEquals(2, pendingBookings.size());
        assertEquals(booking1, pendingBookings.get(0));
        assertEquals(booking2, pendingBookings.get(1));
    }

    @Test
    void testExistsConflictingBookings() {
        Booking existingBooking = new Booking();
        existingBooking.setRoom(room);
        existingBooking.setStartTime(LocalDateTime.now().plusDays(1).withHour(9));
        existingBooking.setEndTime(LocalDateTime.now().plusDays(1).withHour(10));
        existingBooking.setBookingStatus(BookingStatus.ACCEPTED);
        entityManager.persist(existingBooking);

        boolean conflict = bookingRepository.existsConflictingBookings(room.getRoomId(),
                LocalDateTime.now().plusDays(1).withHour(9).plusMinutes(30),
                LocalDateTime.now().plusDays(1).withHour(10).plusMinutes(30));
        assertTrue(conflict);

        conflict = bookingRepository.existsConflictingBookings(room.getRoomId(),
                LocalDateTime.now().plusDays(1).withHour(10).plusMinutes(30),
                LocalDateTime.now().plusDays(1).withHour(11).plusMinutes(30));
        assertFalse(conflict);
    }
}