package io.github.msdottee.courier.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    @OneToMany
    private List<SshPublicKey> sshPublicKey;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return userName;
    }

    public List<SshPublicKey> getSshPublicKey() {
        return sshPublicKey;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public void setSshPublicKey(List<SshPublicKey> sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }
}
