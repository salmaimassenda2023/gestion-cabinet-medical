package com.example.consultationservice.service.impl;

import com.example.consultationservice.dto.ConsultationDTO;
import com.example.consultationservice.entity.Consultation;
import com.example.consultationservice.mapper.ConsultationMapper;
import com.example.consultationservice.repository.ConsultationRepository;
import com.example.consultationservice.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;

    @Override
    public ConsultationDTO createConsultation(ConsultationDTO consultationDTO) {
        Consultation consultation = consultationMapper.toEntity(consultationDTO);
        // Ensure bidirectional relationships if needed, e.g. setConsultation(this) for
        // children
        // For simplicity in this generated code, we assume children are handled or
        // don't need back-ref for save if cascade works.
        // Ideally we iterate children and set 'consultation' reference.
        if (consultation.getExamensCliniques() != null) {
            consultation.getExamensCliniques().forEach(c -> c.setConsultation(consultation));
        }
        if (consultation.getOrdonnancesMedicaments() != null) {
            consultation.getOrdonnancesMedicaments().forEach(c -> c.setConsultation(consultation));
        }
        if (consultation.getOrdonnancesExamens() != null) {
            consultation.getOrdonnancesExamens().forEach(c -> c.setConsultation(consultation));
        }
        if (consultation.getFactures() != null) {
            consultation.getFactures().forEach(c -> c.setConsultation(consultation));
        }

        Consultation saved = consultationRepository.save(consultation);
        return consultationMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationDTO getConsultation(Long id) {
        return consultationRepository.findById(id)
                .map(consultationMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Consultation not found with id: " + id)); // ideally custom
                                                                                                   // exception
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> getAllConsultations() {
        return consultationRepository.findAll().stream()
                .map(consultationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationDTO updateConsultation(Long id, ConsultationDTO consultationDTO) {
        // Simple update implementation: fetch, update fields, save.
        // For complex nested updates, this needs more care.
        // Here we just re-save the new state as a new version or similar.
        // The safest simple way is usually to update the root fields and let children
        // be managed separately or replaced.

        Consultation existing = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with id: " + id));

        Consultation updated = consultationMapper.toEntity(consultationDTO);
        updated.setIdConsultation(existing.getIdConsultation()); // ensure ID matches

        // Relink children for CascadeType.ALL to work correctly on replacement
        if (updated.getExamensCliniques() != null) {
            updated.getExamensCliniques().forEach(c -> c.setConsultation(updated));
        }
        if (updated.getOrdonnancesMedicaments() != null) {
            updated.getOrdonnancesMedicaments().forEach(c -> c.setConsultation(updated));
        }
        if (updated.getOrdonnancesExamens() != null) {
            updated.getOrdonnancesExamens().forEach(c -> c.setConsultation(updated));
        }
        if (updated.getFactures() != null) {
            updated.getFactures().forEach(c -> c.setConsultation(updated));
        }

        Consultation saved = consultationRepository.save(updated);
        return consultationMapper.toDTO(saved);
    }

    @Override
    public void deleteConsultation(Long id) {
        consultationRepository.deleteById(id);
    }
}
