package io.github.msdottee.courier.repository;

import io.github.msdottee.courier.entity.SshPublicKey;
import io.github.msdottee.courier.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SshPublicKeyRepository extends JpaRepository<SshPublicKey, Long> {

    List<SshPublicKey> findByUser(User user);
}
