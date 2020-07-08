package io.github.msdottee.courier.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SshPublicKey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sshKey;

    public Long getId() {
        return id;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setKey(String sshKey) {
        this.sshKey = sshKey;
    }
}