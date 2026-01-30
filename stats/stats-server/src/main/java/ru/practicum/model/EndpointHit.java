package ru.practicum.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "hit_id")
    private Long id;

    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;
}
