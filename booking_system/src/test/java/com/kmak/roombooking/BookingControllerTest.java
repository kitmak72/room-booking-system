package com.kmak.roombooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmak.roombooking.booking.BookingController;
import com.kmak.roombooking.booking.BookingService;
import com.kmak.roombooking.booking.InvalidBookingException;
import com.kmak.roombooking.booking.model.Booking;
import com.kmak.roombooking.booking.model.BookingRequest;
import com.kmak.roombooking.booking.model.BookingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetBooking_Success() throws Exception {
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setBookingId(bookingId);

        when(bookingService.getBooking(bookingId)).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(booking)));
    }

    @Test
    void testGetBooking_NotFound() throws Exception {
        long bookingId = 1L;

        when(bookingService.getBooking(bookingId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/{bookingId}", bookingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNewBooking_Success() throws Exception {
        Long roomId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(9);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1).withHour(10);
        BookingRequest bookingRequest = new BookingRequest(roomId, startTime, endTime);

        Long bookingId = 1L;
        when(bookingService.createNewBooking(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(bookingId);

        mockMvc.perform(post("/api/bookings/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(new BookingResponse(bookingId))));
    }

    @Test
    void testNewBooking_InvalidBooking() throws Exception {
        Long roomId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(9);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1).withHour(10);
        BookingRequest bookingRequest = new BookingRequest(roomId, startTime, endTime);

        when(bookingService.createNewBooking(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new InvalidBookingException("Invalid booking"));

        mockMvc.perform(post("/api/bookings/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid booking"));
    }
}
