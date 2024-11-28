package com.kmak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Main {
    public record BookingRequest(long roomId, LocalDateTime startTime, LocalDateTime endTime) {
    }

    public record BookingResponse(Long bookingId, String message) {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        List<BookingRequest> firstBatch = List.of(
                new BookingRequest(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(2, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(3, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(4, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(5, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(6, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(7, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(8, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(9, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)),
                new BookingRequest(10, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3))
        );

        List<BookingRequest> secondBatch = List.of(
                new BookingRequest(2, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3)),
                new BookingRequest(3, LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(5)),
                new BookingRequest(4, LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(6)),
                new BookingRequest(5, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4)),
                new BookingRequest(6, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3)),
                new BookingRequest(10, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3))
        );

        try (HttpClient client = HttpClient.newHttpClient()) {
            var firstBatchFutures = firstBatch.stream()
                    .map(bookingRequest -> client.sendAsync(createBookingRequest(bookingRequest), HttpResponse.BodyHandlers.ofString()))
                    .toList();
            var combinedFirstBatch = CompletableFuture.allOf(firstBatchFutures.toArray(new CompletableFuture[0]));

            Thread.sleep(500);

            var secondBatchFutures = secondBatch.stream()
                    .map(bookingRequest -> client.sendAsync(createBookingRequest(bookingRequest), HttpResponse.BodyHandlers.ofString()))
                    .toList();
            var combinedSecondBatch = CompletableFuture.allOf(secondBatchFutures.toArray(new CompletableFuture[0]));

            combinedFirstBatch.get();
            combinedSecondBatch.get();

            List<BookingResponse> bookings = Stream.concat(firstBatchFutures.stream(), secondBatchFutures.stream())
                    .map(future -> {
                        try {
                            if (future.get().statusCode() >= 400) {
                                System.out.println("Booking failed: " + future.get().body());
                                return null;
                            }
                            return objectMapper.readValue(future.get().body(), BookingResponse.class);
                        } catch (InterruptedException | ExecutionException |
                                 JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            System.out.println("Booking Complete");
            bookings.forEach(System.out::println);

            System.out.println("Checking booking status");
            bookings.stream()
                    .map(bookingResponse -> client.sendAsync(bookingStatusRequest(bookingResponse.bookingId()), HttpResponse.BodyHandlers.ofString()))
                    .map(CompletableFuture::join)
                    .map(HttpResponse::body)
                    .forEach(System.out::println);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpRequest bookingStatusRequest(long bookingId) {
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/bookings/" + bookingId))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpRequest createBookingRequest(BookingRequest bookingRequest) {
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/api/bookings/new"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(bookingRequest)
                    ))
                    .build();
        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}