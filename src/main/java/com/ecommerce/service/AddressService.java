package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.payload.dto.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateAddress(Long addressId, @Valid AddressDTO addressDTO);

    AddressDTO deleteAddress(Long addressId);
}
