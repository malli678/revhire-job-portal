package com.revhire.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revhire.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}