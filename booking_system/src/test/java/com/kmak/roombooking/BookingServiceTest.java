package com.kmak.roombooking;

import com.kmak.roombooking.booking.*;
import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingStatus;
import com.kmak.roombooking.booking.model.Room;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    @Mock
    private BookingQueue bookingQueue;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private BookingService bookingService;

    private AutoCloseable close;
    private final Clock fixedClock = Clock.fixed(LocalDateTime.of(2024, 11, 25, 9, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

    @BeforeEach
    public void before() {
        close = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void after() throws Exception {
        close.close();
    }

    @Test
    void testCreateNewBooking_Success() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().plusDays(1).withHour(9);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(10);

        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(roomRepository.getReferenceById(roomId)).thenReturn(new Room());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(1L);
            return booking;
        });
        Long bookingId = bookingService.createNewBooking(roomId, startTime, endTime);
        assertNotNull(bookingId);
        verify(bookingQueue, times(1)).add(any(Booking.class));
    }

    @Test
    void testCreateNewBooking_RoomNotFound() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().plusDays(1).withHour(9);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(10);

        when(roomRepository.existsById(roomId)).thenReturn(false);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testCreateNewBooking_InvalidTime() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().minusDays(1);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(10);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testCreateNewBooking_StartTimeAfterEndTime() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().plusDays(1).withHour(10);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(9);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testCreateNewBooking_NonWeekday() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().with(DayOfWeek.SATURDAY).withHour(9);
        LocalDateTime endTime = getFixedCurrentDateTime().with(DayOfWeek.SATURDAY).withHour(10);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testCreateNewBooking_BeforeBusinessHours() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().plusDays(1).withHour(7);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(9);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testCreateNewBooking_AfterBusinessHours() {
        Long roomId = 1L;
        LocalDateTime startTime = getFixedCurrentDateTime().plusDays(1).withHour(18).plusMinutes(1);
        LocalDateTime endTime = getFixedCurrentDateTime().plusDays(1).withHour(19);

        assertThrows(InvalidBookingException.class, () -> bookingService.createNewBooking(roomId, startTime, endTime));
    }

    @Test
    void testSettlePendingBooking_Accepted() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setRoom(new Room());
        booking.setStartTime(getFixedCurrentDateTime().plusDays(1).withHour(9));
        booking.setEndTime(getFixedCurrentDateTime().plusDays(1).withHour(10));

        when(bookingRepository.existsConflictingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);

        bookingService.settlePendingBooking(booking);

        assertEquals(BookingStatus.ACCEPTED, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testSettlePendingBooking_Rejected() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        Room room = new Room();
        room.setRoomId(1L);
        booking.setRoom(room);
        booking.setStartTime(getFixedCurrentDateTime().plusDays(1).withHour(9));
        booking.setEndTime(getFixedCurrentDateTime().plusDays(1).withHour(10));

        when(bookingRepository.existsConflictingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);

        bookingService.settlePendingBooking(booking);

        assertEquals(BookingStatus.REJECTED, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testGetBooking() {
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setBookingId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Optional<Booking> result = bookingService.getBooking(bookingId);

        assertTrue(result.isPresent());
        assertEquals(bookingId, result.get().getBookingId());
    }

    private LocalDateTime getFixedCurrentDateTime() {
        return LocalDateTime.now(fixedClock);
    }
}
