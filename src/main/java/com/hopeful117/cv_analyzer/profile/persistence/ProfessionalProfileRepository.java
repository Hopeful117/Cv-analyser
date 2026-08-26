package com.hopeful117.cv_analyzer.profile.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfileEntity, Long> {

    /**
     * Sémantique locale mono-utilisateur : l'application ne possède pas encore d'identité
     * (workspace/utilisateur). Ce point unique centralisera la future sélection par propriétaire
     * (ex. findByWorkspaceId) sans modifier le domaine ni les cas d'usage.
     */
    default Optional<ProfessionalProfileEntity> findLocalProfile() {
        return findAll().stream().findFirst();
    }
}
