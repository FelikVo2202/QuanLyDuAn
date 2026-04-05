package vn.edu.uit.is208.salon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomerRequest {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ không được quá 50 ký tự")
    private String lastName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15, message = "Số điện thoại không quá 15 số")
    private String phoneNumber;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String email;

    @Size(max = 10, message = "Giới tính không quá 10 ký tự")
    private String gender;
}