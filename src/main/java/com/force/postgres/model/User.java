package com.force.postgres.model;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "usr_fk_company_rule_uuid")
	private CompanyRule companyRule;

    @Column(name = "usr_tx_first_name", length =  100, nullable = false)
    private String firstName;

    @Column(name = "usr_tx_last_name", length =  200)
    private String lastName;

    @Column(name = "usr_tx_email", length =  255, nullable = false)
    private String email;

    @Column(name = "usr_tx_password_hash", length =  255)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "usr_lg_enabled")
    private Boolean enabled;

    @Column(name = "usr_tx_image_url", length =  255)
    private String imageUrl;

    @Column(name = "usr_tx_activation_key", length =  255)
    @JsonIgnore
    private String activationKey;

    @Column(name = "usr_lg_activated")
    private Boolean activated;

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

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @Builder.Default
    // @JsonIgnoreProperties("user")
    // private Set<UserRole> roles = new HashSet<>();

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @Builder.Default
    // @JsonIgnoreProperties("user")
    // private Set<UserPermission> permissions = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    

}
