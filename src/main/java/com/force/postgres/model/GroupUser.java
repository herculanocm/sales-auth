package com.force.postgres.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "GroupUser")
@Table(name = "group_user")
public class GroupUser {

    @Id
    @Column(name = "gru_pk_uuid")
    private UUID id;

    @Column(name = "gru_tx_name", length =  255)
    private String name;

    @Column(name = "gru_tx_description", length =  3000)
    private String description;

    @Column(name = "gru_lg_enabled")
    private Boolean enabled;

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
        return "GroupUser [id=" + id + ", name=" + name + ", description=" + description + ", enabled=" + enabled + "]";
    }

    
    
}
