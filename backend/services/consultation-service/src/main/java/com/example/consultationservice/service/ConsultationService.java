package com.example.consultationservice.service;

import com.example.consultationservice.dto.ConsultationDTO;
import java.util.List;

public interface ConsultationService {
    ConsultationDTO createConsultation(ConsultationDTO consultationDTO);

    ConsultationDTO getConsultation(Long id);

    List<ConsultationDTO> getAllConsultations();

    ConsultationDTO updateConsultation(Long id, ConsultationDTO consultationDTO);

    void deleteConsultation(Long id);
}
