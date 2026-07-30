package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class GoogleSheetRowMapper {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Map<String, Object> map(ApplicationSheetProjection p) {
        Map<String, Object> row = new LinkedHashMap<>();
        put(row, "ID", p.displayId());
        put(row, "Entreprise", p.companyName());
        put(row, "Ville", p.city());
        put(row, "Adresse", p.address());
        put(row, "Téléphone", p.phone());
        put(row, "Email", p.email());
        put(row, "Site", p.website());
        put(row, "Poste", p.jobTitle());
        put(row, "Lien offre", p.offerUrl());
        put(row, "Contrat", p.contractType());
        put(row, "Temps", p.workSchedule());
        put(row, "Télétravail", p.remoteMode());
        put(row, "Source", p.source());
        put(row, "CV envoyé", p.resumeSent() ? "Oui" : "Non");
        put(row, "LM envoyée", p.coverLetterSent() ? "Oui" : "Non");
        put(row, "Portfolio", p.portfolioSent() ? "Oui" : "Non");
        put(row, "Date candidature", p.appliedAt() == null ? "" : DATE.format(p.appliedAt()));
        put(row, "Relance prévue", p.followUpPlannedAt() == null ? "" : DATE.format(p.followUpPlannedAt()));
        put(row, "Dernière relance", p.lastFollowUpAt() == null ? "" : DATE.format(p.lastFollowUpAt()));
        put(row, "Statut", p.status());
        put(row, "Entretien", p.interview());
        put(row, "Décision", p.decision());
        put(row, "Salaire", p.salary());
        put(row, "Distance", p.distance());
        put(row, "Priorité", p.priority());
        put(row, "Notes", p.notes());
        put(row, "Career Intelligence ID", p.careerIntelligenceId());
        put(row, "Score IA", p.aiScore());
        put(row, "Version CV", p.resumeVersion());
        put(row, "Dernière synchronisation", p.lastSynchronizedAt() == null ? "" :
                DATE_TIME.withZone(ZoneId.systemDefault()).format(p.lastSynchronizedAt()));
        put(row, "Statut synchronisation", p.synchronizationStatus());
        return row;
    }

    private void put(Map<String, Object> target, String key, Object value) {
        target.put(key, value instanceof String text ? GoogleSheetFormulaEscaper.escape(text) :
                value == null ? "" : value);
    }
}
