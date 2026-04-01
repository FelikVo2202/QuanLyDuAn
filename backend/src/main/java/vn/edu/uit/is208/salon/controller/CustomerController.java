package vn.edu.uit.is208.salon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.edu.uit.is208.salon.entity.Customer;
import vn.edu.uit.is208.salon.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAll() {
        return customerService.getAllCustomers();
    }


    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer details) {
        Customer customer = customerService.getCustomerById(id);
        if (customer != null) {
            customer.setFirstName(details.getFirstName());
            customer.setLastName(details.getLastName());
            customer.setEmail(details.getEmail());
            customer.setPhoneNumber(details.getPhoneNumber());
            customer.setGender(details.getGender());
            return customerService.saveCustomer(customer);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "Đã xóa thành công khách hàng có ID: " + id;
    }
}