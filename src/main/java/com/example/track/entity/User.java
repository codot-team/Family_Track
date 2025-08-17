package com.example.track.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    private Long chatId;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Long parentChatId;

    @CreationTimestamp
    private LocalDateTime registerDate;

    @UpdateTimestamp
    private LocalDateTime lastActive;
}
