package io.appdeveloperblog.app.ws.service;

import java.util.List;

import io.appdeveloperblog.app.ws.shared.dto.AddressDto;

public interface AddressService {
	List<AddressDto> getAddresses(String userId);
	AddressDto getAddress(String addressId);
}
