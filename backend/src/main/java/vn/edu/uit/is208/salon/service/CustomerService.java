package vn.edu.uit.is208.salon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.uit.is208.salon.dto.CreateCustomerRequest;
import vn.edu.uit.is208.salon.dto.CustomerResponse;
import vn.edu.uit.is208.salon.dto.UpdateCustomerRequest;
import vn.edu.uit.is208.salon.entity.Customer;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.CustomerMapper;
import vn.edu.uit.is208.salon.repository.CustomerRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = getCustomer(id);
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = getCustomer(id);
        customerMapper.updateEntityFromDto(request, customer);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomer(id);
        if (!customer.getAppointments().isEmpty()) {
            throw new BusinessRuleException("Cannot delete this customer because they have existing appointments");
        }
        customerRepository.delete(customer);
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }
}