package com.example.track.repository;

import com.example.track.entity.Role;
import com.example.track.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByChatId(Long chatId);
    long countByRole(Role role);
}
