package com.force.postgres.model;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "GroupUser")
@Table(name = "group_user")
public class GroupUser {

    @Id
    @Column(name = "gru_pk_uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "gru_tx_name", length =  255)
    private String name;

    @Column(name = "gru_tx_description", length =  3000)
    private String description;

    @Column(name = "gru_lg_enabled")
    private Boolean enabled;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "gru_fk_company_rule_uuid")
	private CompanyRule companyRule;

    @Column(name = "gru_dt_include", updatable = false, nullable = false)
	private LocalDateTime dtInclude;

	@Column(name = "gru_tx_user_include", length =  255, updatable = false, nullable = false)
	private String userInclude;

    @Column(name = "gru_dt_update", nullable = false)
	private LocalDateTime dtUpdate;

	@Column(name = "gru_tx_user_update", length =  255, nullable = false)
	private String userUpdate;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupUser other = (GroupUser) obj;
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

    @Override
    public String toString() {
        return "GroupUser [id=" + id + ", name=" + name + ", description=" + description + ", enabled=" + enabled
                + ", companyRule=" + companyRule + ", dtInclude=" + dtInclude + ", userInclude=" + userInclude
                + ", dtUpdate=" + dtUpdate + ", userUpdate=" + userUpdate + "]";
    }
    
}
