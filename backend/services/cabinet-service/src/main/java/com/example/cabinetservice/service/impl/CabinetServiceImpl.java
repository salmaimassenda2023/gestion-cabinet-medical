package com.example.cabinetservice.service.impl;

import com.example.cabinetservice.dto.*;
import com.example.cabinetservice.entity.AbonnementCabinet;
import com.example.cabinetservice.entity.Cabinet;
import com.example.cabinetservice.entity.PaiementAbonnement;
import com.example.cabinetservice.entity.ServiceConsultation;
import com.example.cabinetservice.enums.AbonnementStatus;
import com.example.cabinetservice.enums.PaiementStatus;
import com.example.cabinetservice.exception.ResourceNotFoundException;
import com.example.cabinetservice.mapper.CabinetMapper;
import com.example.cabinetservice.repository.AbonnementRepository;
import com.example.cabinetservice.repository.CabinetRepository;
import com.example.cabinetservice.repository.PaiementRepository;
import com.example.cabinetservice.repository.ServiceConsultationRepository;
import com.example.cabinetservice.service.CabinetService;
import com.example.cabinetservice.utilisateur.UtilisateurResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CabinetServiceImpl implements CabinetService {

    private final CabinetRepository cabinetRepository;
    private final AbonnementRepository abonnementRepository;
    private final ServiceConsultationRepository serviceConsultationRepository;
    private final PaiementRepository paiementRepository;
    private final CabinetMapper cabinetMapper;
    private final com.example.cabinetservice.utilisateur.MedecinClient medecinClient;
    private Long userId;

    @Override
    @Transactional
    public CabinetResponseDTO createCabinet(CabinetCreateDTO dto) {
        log.info("Creating new cabinet: {}", dto.getNom());

        // 0. Verify Medecin Exists in User Service
        if (dto.getMedecinId() != null) {
            try {
                medecinClient.getUtilisateurById(dto.getMedecinId());
            } catch (Exception e) {
                log.error("Error verifying medecin id: {}", dto.getMedecinId(), e);
            }
        }

        // 1. Create Cabinet (INACTIF par défaut)
        Cabinet cabinet = cabinetMapper.toEntity(dto);
        cabinet.setActif(false); // ← FORCER À FALSE
        cabinet = cabinetRepository.save(cabinet);

        // 2. Create and Link Abonnement 
        if (dto.getAbonnement() != null) {
            AbonnementCabinet abonnement = cabinetMapper.toEntity(dto.getAbonnement());
            abonnement.setCabinet(cabinet);
            abonnement.setStatut(AbonnementStatus.ACTIF); 
            abonnement.setDateDebut(LocalDateTime.now());

            if ("ANNUEL".equalsIgnoreCase(abonnement.getTypePeriode().name())) {
                abonnement.setDateFin(LocalDateTime.now().plusYears(1));
            } else {
                abonnement.setDateFin(LocalDateTime.now().plusMonths(1));
            }

            abonnementRepository.save(abonnement);
            cabinet.setAbonnement(abonnement);
        }

        // 3. Create Default Service
        if (dto.getServiceConsultationGenerale() != null) {
            ServiceConsultation service = cabinetMapper.toEntity(dto.getServiceConsultationGenerale());
            service.setCabinet(cabinet);
            service.setObligatoire(true);
            serviceConsultationRepository.save(service);
        }

        // 4. Sync User Service (Link Cabinet to Medecin)
        if (dto.getMedecinId() != null) {
            try {
                medecinClient.updateCabinetId(dto.getMedecinId(), cabinet.getId());
                log.info("Synced cabinet id {} to medecin {}", cabinet.getId(), dto.getMedecinId());
            } catch (Exception e) {
                log.error("Failed to sync cabinet id to medecin service", e);
            }
        }

        // 5. RETOURNER LA RÉPONSE COMPLÈTE AVEC LES SERVICES
        CabinetResponseDTO response = cabinetMapper.toDto(cabinet);

        // Charger les services
        List<ServiceConsultationDTO> services = serviceConsultationRepository.findByCabinetId(cabinet.getId())
                .stream()
                .map(cabinetMapper::toDto)
                .collect(Collectors.toList());
        response.setServices(services);

        return response;
    }

    @Override
    public CabinetResponseDTO getCabinetByMedecinId(Long medecinId) {
        Cabinet cabinet = cabinetRepository.findByMedecinId(medecinId)
                .orElseThrow(() -> new RuntimeException("Cabinet not found for medecin with id: " + medecinId));
        return cabinetMapper.toDto(cabinet);
    }

    @Override
    public CabinetResponseDTO getCabinet(Long id) {
        Cabinet cabinet = cabinetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet not found with id: " + id));

        CabinetResponseDTO response = cabinetMapper.toDto(cabinet);

        List<ServiceConsultationDTO> services = serviceConsultationRepository.findByCabinetId(id).stream()
                .map(cabinetMapper::toDto)
                .collect(Collectors.toList());
        response.setServices(services);

        return response;
    }

    @Override
    @Transactional
    public CabinetResponseDTO updateCabinet(Long id, CabinetUpdateDTO dto) {
        Cabinet cabinet = cabinetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet not found with id: " + id));

        if (dto.getNom() != null)
            cabinet.setNom(dto.getNom());
        if (dto.getSpecialite() != null)
            cabinet.setSpecialite(dto.getSpecialite());
        if (dto.getAdresse() != null)
            cabinet.setAdresse(dto.getAdresse());
        if (dto.getTel() != null)
            cabinet.setTel(dto.getTel());
        if (dto.getLogo() != null)
            cabinet.setLogo(dto.getLogo());
        if (dto.getMaxPatientsJour() != null)
            cabinet.setMaxPatientsJour(dto.getMaxPatientsJour());
        if (dto.getDureeConsultation() != null)
            cabinet.setDureeConsultation(dto.getDureeConsultation());

        Cabinet updatedCabinet = cabinetRepository.save(cabinet);
        return cabinetMapper.toDto(updatedCabinet);
    }

    @Override
    public void deleteCabinet(Long id) {
        if (!cabinetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cabinet not found with id: " + id);
        }
        cabinetRepository.deleteById(id);
    }

    @Override
    public Boolean isCabinetActive(Long id) {
        Cabinet cabinet = cabinetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet not found with id: " + id));
        return cabinet.getActif();
    }

    @Override
    @Transactional
    public ServiceConsultationDTO addService(Long cabinetId, ServiceConsultationDTO dto) {
        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet not found with id: " + cabinetId));

        ServiceConsultation service = cabinetMapper.toEntity(dto);
        service.setCabinet(cabinet);

        ServiceConsultation savedService = serviceConsultationRepository.save(service);
        return cabinetMapper.toDto(savedService);
    }

    @Override
    public ServiceConsultationDTO getServiceById(Long cabinetId, Long serviceId) {
        log.info("Recherche du service avec ID: {} pour le cabinet ID: {}", serviceId, cabinetId);

        // Find the service
        ServiceConsultation service = serviceConsultationRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service non trouvé avec l'ID: " + serviceId));

        // Verify the service belongs to this cabinet
        if (!service.getCabinet().getId().equals(cabinetId)) {
            throw new ResourceNotFoundException(
                    "Service " + serviceId + " n'appartient pas au cabinet " + cabinetId);
        }

        log.info("Service trouvé: {} (Cabinet ID: {})",
                service.getNomService(), cabinetId);

        return cabinetMapper.toDto(service);
    }

    @Override
    public List<ServiceConsultationDTO> getServices(Long cabinetId) {
        if (!cabinetRepository.existsById(cabinetId)) {
            throw new ResourceNotFoundException("Cabinet not found with id: " + cabinetId);
        }
        return serviceConsultationRepository.findByCabinetId(cabinetId).stream()
                .map(cabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CabinetResponseDTO> getAllCabinets() {
        return cabinetRepository.findAll().stream()
                .map(cabinetMapper::toDto)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void handleSuccessfulPayment(Long abonnementId, Double amount, String stripePaymentId) {
        log.info("Processing successful payment for abonnement: {}", abonnementId);

        AbonnementCabinet abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found with id: " + abonnementId));

        // Create Payment Record
        PaiementAbonnement paiement = PaiementAbonnement.builder()
                .abonnement(abonnement)
                .montant(amount)
                .statut(PaiementStatus.VALIDE)
                .datePaiement(LocalDateTime.now())
                .build();
        paiementRepository.save(paiement);

        // Activate Abonnement
        abonnement.setStatut(AbonnementStatus.ACTIF);

        // Update Dates if not already set or expired
        LocalDateTime now = LocalDateTime.now();
        if (abonnement.getDateDebut() == null || abonnement.getDateFin() == null
                || abonnement.getDateFin().isBefore(now)) {
            abonnement.setDateDebut(now);
            if ("ANNUEL".equalsIgnoreCase(abonnement.getTypePeriode().name())) {
                abonnement.setDateFin(now.plusYears(1));
            } else {
                abonnement.setDateFin(now.plusMonths(1));
            }
        } else {
            // Extension case
            if ("ANNUEL".equalsIgnoreCase(abonnement.getTypePeriode().name())) {
                abonnement.setDateFin(abonnement.getDateFin().plusYears(1));
            } else {
                abonnement.setDateFin(abonnement.getDateFin().plusMonths(1));
            }
        }

        abonnementRepository.save(abonnement);

        // Reactivate Cabinet
        if (abonnement.getCabinet() != null) {
            abonnement.getCabinet().setActif(true);
            cabinetRepository.save(abonnement.getCabinet());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public CabinetResponseDTO getCabinetByUserId(Long userId) {
        log.info("Getting cabinet for user ID: {}", userId);

        try {
            // First, get user information to check their role
            UtilisateurResponse user = medecinClient.getUtilisateurById(userId);
            log.info("User found: {} {}, Role: {}", user.getPrenom(), user.getNom(), user.getRole());

            Cabinet cabinet = null;

            // Check user role and find cabinet accordingly
            if ("MEDECIN".equalsIgnoreCase(user.getRole())) {
                // For MEDECIN: use medecinId query
                cabinet = cabinetRepository.findByMedecinId(userId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Cabinet not found for medecin with id: " + userId));
            } else if ("SECRETAIRE".equalsIgnoreCase(user.getRole())) {
                // For SECRETAIRE: check if user has cabinet_id
                if (user.getIdCabinet() != null) {
                    cabinet = cabinetRepository.findById(user.getIdCabinet())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Cabinet not found with id: " + user.getIdCabinet()));
                } else {
                    // If secretary doesn't have cabinet_id, find any active cabinet
                    List<Cabinet> allCabinets = cabinetRepository.findAll();

                    if (allCabinets.isEmpty()) {
                        throw new ResourceNotFoundException("No cabinets found in system");
                    }

                    // Try to find a cabinet with active abonnement
                    cabinet = allCabinets.stream()
                            .filter(c -> c.getAbonnement() != null &&
                                    "ACTIF".equals(c.getAbonnement().getStatut().name()))
                            .findFirst()
                            .orElse(allCabinets.get(0)); // Fallback to first cabinet

                    log.warn("Secretary {} doesn't have cabinet_id, using cabinet: {}",
                            userId, cabinet.getNom());
                }
            } else {
                throw new ResourceNotFoundException(
                        "User role " + user.getRole() + " not supported for cabinet access");
            }

            if (cabinet == null) {
                throw new ResourceNotFoundException(
                        "Cabinet not found for user with id: " + userId);
            }

            log.info("Found cabinet: {} (ID: {}) for user: {}",
                    cabinet.getNom(), cabinet.getId(), userId);

            return convertCabinetToDto(cabinet);

        } catch (Exception e) {
            log.error("Error getting cabinet for user ID: {}", userId, e);
            throw new ResourceNotFoundException(
                    "Failed to get cabinet for user ID: " + userId + ". Error: " + e.getMessage());
        }
    }

    @Override
    public CabinetResponseDTO getCabinetForUser(Long userId) {
        return getCabinetByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponseDTO> getAllAbonnements() {
        return abonnementRepository.findAll().stream()
                .map(abonnement -> {
                    String cabinetNom = "Unknown";
                    String cabinetLogo = null;
                    if (abonnement.getCabinet() != null) {
                        cabinetNom = abonnement.getCabinet().getNom();
                        cabinetLogo = abonnement.getCabinet().getLogo();
                    }

                    return AbonnementResponseDTO.builder()
                            .idAbonnement(abonnement.getIdAbonnement())
                            .dateDebut(abonnement.getDateDebut())
                            .dateFin(abonnement.getDateFin())
                            .statut(abonnement.getStatut())
                            .montant(abonnement.getMontant())
                            .typePeriode(abonnement.getTypePeriode().name())
                            .cabinetNom(cabinetNom)
                            .cabinetLogo(cabinetLogo)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert cabinet to DTO with services
     */
    private CabinetResponseDTO convertCabinetToDto(Cabinet cabinet) {
        CabinetResponseDTO response = cabinetMapper.toDto(cabinet);

        // Populate services
        List<ServiceConsultationDTO> services = serviceConsultationRepository
                .findByCabinetId(cabinet.getId())
                .stream()
                .map(cabinetMapper::toDto)
                .collect(Collectors.toList());
        response.setServices(services);

        return response;
    }
}
