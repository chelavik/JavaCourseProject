package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"place_id", "startTime", "endTime"})
})
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Кто бронировал
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // Что бронировал
    @ManyToOne(optional = false)
    @JoinColumn(name = "place_id")
    private Workspace workspace;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean cancelled;
}
