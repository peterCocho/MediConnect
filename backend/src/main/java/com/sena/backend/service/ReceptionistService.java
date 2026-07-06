package com.sena.backend.service;

import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.domain.receptionist.ReceptionistResponse;
import com.sena.backend.domain.receptionist.UpdateReceptionistRequest;
import org.springframework.data.domain.Page;

public interface ReceptionistService {
    void createReceptionist(CreateReceptionistRequest req);
    ReceptionistResponse getReceptionistById(Long id);
    Page<ReceptionistResponse> getAllReceptionists(int page, int size, String sortBy);
    void updateReceptionist(Long id, UpdateReceptionistRequest req);
    void toggleReceptionistStatus(Long id, boolean status);
}