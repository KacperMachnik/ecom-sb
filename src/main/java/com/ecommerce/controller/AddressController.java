package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.payload.dto.AddressDTO;
import com.ecommerce.service.AddressService;
import com.ecommerce.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class AddressController {

    private final AuthUtil authUtil;
    private final AddressService addressService;

    @Autowired
    public AddressController(AuthUtil authUtil, AddressService addressService) {
        this.authUtil = authUtil;
        this.addressService = addressService;
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        log.info("Creating address: {}", addressDTO);
        User user = authUtil.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses() {
        log.debug("Retrieving all addresses");
        List<AddressDTO> addressDTOS = addressService.getAddresses();
        return new ResponseEntity<>(addressDTOS, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long addressId) {
        log.debug("Retrieving address: {}", addressId);
        AddressDTO addressDTO = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        log.debug("Retrieving all addresses of user");
        User user = authUtil.loggedInUser();
        List<AddressDTO> addressDTOS = addressService.getUserAddresses(user);
        return new ResponseEntity<>(addressDTOS, HttpStatus.OK);
    }

    @PutMapping("addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @Valid @RequestBody AddressDTO addressDTO) {
        log.info("Updating address: {}", addressDTO);
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("addresses/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddressById(@PathVariable Long addressId) {
        log.info("Deleting address: {}", addressId);
        AddressDTO addressDTO = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }
}
