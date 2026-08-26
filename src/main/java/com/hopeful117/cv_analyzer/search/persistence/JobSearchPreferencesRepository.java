package com.hopeful117.cv_analyzer.search.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Sémantique locale mono-utilisateur, miroir de la couture du profil professionnel :
 * l'application ne possède pas encore d'identité (workspace/utilisateur). Ce point unique
 * centralisera la future sélection par propriétaire sans modifier le domaine ni les cas d'usage.
 */
public interface JobSearchPreferencesRepository extends JpaRepository<JobSearchPreferencesEntity, Long> {

    default java.util.Optional<JobSearchPreferencesEntity> findActivePreferences() {
        return findAll().stream().findFirst();
    }
}
