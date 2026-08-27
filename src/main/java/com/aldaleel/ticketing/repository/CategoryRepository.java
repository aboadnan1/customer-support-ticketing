package com.aldaleel.ticketing.repository;

import com.aldaleel.ticketing.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}