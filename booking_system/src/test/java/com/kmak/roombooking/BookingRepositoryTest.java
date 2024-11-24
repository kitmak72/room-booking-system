package com.kmak.roombooking;

import com.kmak.roombooking.booking.BookingRepository;
import com.kmak.roombooking.booking.RoomRepository;
import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingStatus;
import com.kmak.roombooking.booking.model.Room;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;


    @Test
    public void testBookingSave_Success() {
//        Room room = new Room();
//        room.setRoomId(1L);
//        room.setRoomName("Room 1");
//        roomRepository.save(room);

        Booking booking = new Booking();
//        booking.setRoom(room);
        booking.setStartTime(LocalDateTime.now().plusHours(1));
        booking.setEndTime(LocalDateTime.now().plusHours(2));
        booking.setRequestTime(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.PENDING);
        var savedBooking = bookingRepository.save(booking);

        Assertions.assertNotNull(savedBooking);
        Assertions.assertNotNull(savedBooking.getBookingId());

    }
}
