package com.force.postgres.model;

import java.time.LocalDateTime;
import java.util.UUID;

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
@Entity(name = "CompanyRuleKey")
@Table(name = "company_rule_key")
public class CompanyRuleKey {

    @Id
    @Column(name = "crk_pk_uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "crk_tx_public_key", length =  4000)
    private String publicKey;

    @Column(name = "crk_tx_private_key", length =  4000)
    private String privateKey;

    @Column(name = "crk_tx_type_key", length =  255)
    private String tipoKey;

    @Column(name = "crk_dt_include", updatable = false, nullable = false)
	private LocalDateTime dtInclude;

	@Column(name = "crk_tx_user_include", length =  255, updatable = false, nullable = false)
	private String userInclude;

    @Column(name = "crk_dt_update", nullable = false)
	private LocalDateTime dtUpdate;

	@Column(name = "crk_tx_user_update", length =  255, nullable = false)
	private String userUpdate;

    @Column(name = "crk_fk_company_rule", nullable = false)
    private CompanyRule companyRule;


    public CompanyRuleKey(UUID id) {
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
        CompanyRuleKey other = (CompanyRuleKey) obj;
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
