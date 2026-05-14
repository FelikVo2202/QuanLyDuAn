package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.SalonServiceDto;
import vn.edu.uit.is208.salon.mapper.SalonServiceMapper;
import vn.edu.uit.is208.salon.repository.SalonServiceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalonServiceService {
    private final SalonServiceRepository salonServiceRepository;
    private final SalonServiceMapper salonServiceMapper;

    public List<SalonServiceDto> getAllServices() {
        return salonServiceRepository.findAll().stream()
                .map(salonServiceMapper::toDto)
                .collect(Collectors.toList());
    }
}
