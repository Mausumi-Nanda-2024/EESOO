package com.eesoo.EESOO.user.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid" )
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "pin", nullable = false)
    private String pin;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number", unique = true , nullable = false)
    private String phoneNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "user_registered_at", nullable = false)
    private Instant userRegisteredAt;

   
}
