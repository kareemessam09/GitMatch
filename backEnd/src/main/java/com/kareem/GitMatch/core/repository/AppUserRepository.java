package com.kareem.GitMatch.core.repository;

import com.kareem.GitMatch.core.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByGithubUsername(String githubUsername);

    boolean existsByGithubUsername(String githubUsername);

    Optional<AppUser> findByEmail(String email);
}
