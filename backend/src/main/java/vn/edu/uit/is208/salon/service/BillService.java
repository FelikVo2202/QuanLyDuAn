package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.mapper.BillMapper;
import vn.edu.uit.is208.salon.repository.*;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillService {
    private final BillRepository billRepository;
    private final BillMapper billMapper;

    public Page<BillDto> getAllBills(
            int page, int size, String paymentStatus, Long customerId, LocalDate startDate, LocalDate endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("billDate").descending());
        return billRepository.findAll(pageable).map(billMapper::toDto);
    }
}
