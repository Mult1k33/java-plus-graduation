package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.yandex.practicum.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"requester_id", "event_id"},
                name = "uq_requester_event")}
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private RequestStatus status;

    public void confirmed() {
        this.status = RequestStatus.CONFIRMED;
    }

    public void rejected() {
        this.status = RequestStatus.REJECTED;
    }

    public void canceled() {
        this.status = RequestStatus.CANCELED;
    }
}
