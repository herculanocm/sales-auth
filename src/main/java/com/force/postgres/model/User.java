package com.force.postgres.model;

import java.util.UUID;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "User")
@Table(name = "sales_user")
public class User {
    
    @Id
    @Column(name = "usr_pk_uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "usr_tx_first_name", length =  100, nullable = false)
    private String firstName;

    @Column(name = "usr_tx_last_name", length =  200)
    private String lastName;

    @Column(name = "usr_tx_email", length =  255, nullable = false)
    private String email;

    @Column(name = "usr_tx_password_hash", length =  255, nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "usr_lg_enabled")
    private Boolean enabled;

    @Column(name = "usr_tx_image_url", length =  255)
    private String imageUrl;

    @Column(name = "usr_tx_activation_key", length =  255)
    @JsonIgnore
    private String activationKey;

    @Column(name = "usr_tx_reset_key", length =  255)
    @JsonIgnore
    private String resetKey;

    @Column(name = "usr_dt_include", updatable = false, nullable = false)
    private LocalDateTime dtInclude;

    @Column(name = "usr_tx_user_include", length =  255, updatable = false, nullable = false)
    private String userInclude;

    @Column(name = "usr_dt_update", nullable = false)
    private LocalDateTime dtUpdate;

    @Column(name = "usr_tx_user_update", length =  255, nullable = false)
    private String userUpdate;
}
