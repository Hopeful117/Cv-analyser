package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    @Query("""
            select c from CompanyEntity c
            where lower(c.name) = lower(:name)
              and lower(coalesce(c.city, '')) = lower(:city)
            order by c.id
            """)
    List<CompanyEntity> findExactCandidates(@Param("name") String name, @Param("city") String city);
    List<CompanyEntity> findAllByOrderByNameAsc();
}
