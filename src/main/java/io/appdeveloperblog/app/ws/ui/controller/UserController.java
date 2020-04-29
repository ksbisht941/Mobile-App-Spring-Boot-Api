package io.appdeveloperblog.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.appdeveloperblog.app.ws.exceptions.UserServiceException;
import io.appdeveloperblog.app.ws.service.AddressService;
import io.appdeveloperblog.app.ws.service.UserService;
import io.appdeveloperblog.app.ws.shared.dto.AddressDto;
import io.appdeveloperblog.app.ws.shared.dto.UserDto;
import io.appdeveloperblog.app.ws.ui.model.request.UserDetailsRequestModel;
import io.appdeveloperblog.app.ws.ui.model.response.AddressesRest;
import io.appdeveloperblog.app.ws.ui.model.response.ErrorMessages;
import io.appdeveloperblog.app.ws.ui.model.response.OperationStatusModel;
import io.appdeveloperblog.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressesService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();
		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);
		return returnValue;
	}

	@PostMapping(
			consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
			)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {
		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createdUser = userService.createUser(userDto);
		returnValue = modelMapper.map(createdUser, UserRest.class);

		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updateUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updateUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(id);
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "10") int limit) {

		List<UserRest> returnValue = new ArrayList<UserRest>();
		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		return returnValue;
	}
	
	@GetMapping(path = "/{id}/addresses",
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue = new ArrayList<AddressesRest>();
		List<AddressDto> addressDto = addressesService.getAddresses(id);
		
		if(addressDto != null && !addressDto.isEmpty()) {
			java.lang.reflect.Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
			returnValue = new ModelMapper().map(addressDto, listType);			
		}
		
		return returnValue;
	}
	
	@GetMapping(path = "/{userId}/addresses/{addressId}",
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public AddressesRest getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDto addressDto = addressesService.getAddress(addressId);
		ModelMapper modelMapper = new ModelMapper();
		
//		Link addLink = linkTo(UserController.class).slash(userId).slash("addresses").slash(addressId).withSelfRel();
//		AddressesRest addressesRestModel = modelMapper.map(addressDto, AddressesRest.class);
//		addressesRestModel.add(addLink);
		return modelMapper.map(addressDto, AddressesRest.class);
	}
}
