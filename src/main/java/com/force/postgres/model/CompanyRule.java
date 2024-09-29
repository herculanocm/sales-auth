package com.force.postgres.model;

import java.util.UUID;
import java.time.LocalDateTime;

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
@Entity(name = "CompanyRule")
@Table(name = "company_rule")
public class CompanyRule {
    
    @Id
    @Column(name = "cpr_pk_uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cpr_tx_name", length =  255)
    private String name;

    @Column(name = "cpr_tx_cgc", length =  20)
    private String cgc;

    @Column(name = "cpr_lg_enabled")
    private Boolean enabled;

    @Column(name = "cpr_dt_include", updatable = false, nullable = false)
	private LocalDateTime dtInclude;

	@Column(name = "cpr_tx_user_include", length =  255, updatable = false, nullable = false)
	private String userInclude;

    @Column(name = "cpr_dt_update", nullable = false)
	private LocalDateTime dtUpdate;

	@Column(name = "cpr_tx_user_update", length =  255, nullable = false)
	private String userUpdate;

    public CompanyRule(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompanyRule other = (CompanyRule) obj;
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
