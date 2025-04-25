package com.ecommerce.service;


import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.payload.dto.AddressDTO;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public AddressServiceImpl(ModelMapper modelMapper, AddressRepository addressRepository, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        log.info("Creating new address");
        Address address = modelMapper.map(addressDTO, Address.class);
        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        log.debug("Retrieving all addresses");
        List<Address> addressList = addressRepository.findAll();
        return addressList.stream()
                .map(add -> modelMapper.map(add, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        log.debug("Retrieving address by id");
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.debug("Address not found");
                    return new ResourceNotFoundException("Address", "id", addressId);
                });
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        log.debug("Retrieving all user addresses");
        List<Address> addressList = user.getAddresses();
        return addressList.stream()
                .map(add -> modelMapper.map(add, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        log.debug("Updating address {}", addressId);
        Address address = addressRepository.findById(addressId).orElseThrow(() -> {
            log.debug("Address not found");
            return new ResourceNotFoundException("Address", "id", addressId);
        });
        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }
        if (addressDTO.getState() != null) {
            address.setState(addressDTO.getState());
        }
        if (addressDTO.getCountry() != null) {
            address.setCountry(addressDTO.getCountry());
        }
        if (addressDTO.getZipCode() != null) {
            address.setZipCode(addressDTO.getZipCode());
        }
        if (addressDTO.getStreet() != null) {
            address.setStreet(addressDTO.getStreet());
        }
        Address updatedAddress = addressRepository.save(address);
        User user = address.getUser();
        user.getAddresses().removeIf(add -> add.getAddressId().equals(addressId));
        user.getAddresses().add(address);
        userRepository.save(user);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        log.info("Deleting address with id [{}]", addressId);
        Address address = addressRepository.findById(addressId).orElseThrow(() -> {
            log.info("Address not found");
            return new ResourceNotFoundException("Address", "id", addressId);
        });
        User user = address.getUser();
        user.getAddresses().removeIf(add -> add.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(address);
        return modelMapper.map(address, AddressDTO.class);
    }
}
