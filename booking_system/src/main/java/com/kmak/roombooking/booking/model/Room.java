package com.kmak.roombooking.booking.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long id) {
        this.roomId = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String name) {
        this.roomName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomId, room.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
}
