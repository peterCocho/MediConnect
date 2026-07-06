package com.sena.backend.specification;

import com.sena.backend.entity.Doctor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DoctorSpecification {

    public static Specification<Doctor> withDynamicFilters(String fullName, String specialty, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Prevención de NullPointerException y protección case-insensitive
            if (fullName != null && !fullName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        "%" + fullName.trim().toLowerCase() + "%"
                ));
            }

            if (specialty != null && !specialty.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("specialty")),
                        "%" + specialty.trim().toLowerCase() + "%"
                ));
            }

            // Búsqueda estricta por boolean
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
