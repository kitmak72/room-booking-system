package com.kmak.roombooking.booking;

import com.kmak.roombooking.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' ORDER BY b.requestTime")
    List<Booking> findAllPendingBookings();

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.room.roomId = :roomId AND b.bookingStatus = 'ACCEPTED' " +
            "AND ((:startTime < b.endTime AND :endTime > b.startTime))")
    boolean existsConflictingBookings(@Param("roomId") Long roomId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
}
