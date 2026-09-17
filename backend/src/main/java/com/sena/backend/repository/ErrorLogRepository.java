package com.sena.backend.repository;

import com.sena.backend.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    // Devuelve todos los errores ordenados del más reciente al más antiguo
    List<ErrorLog> findAllByOrderByTimestampDesc();

}