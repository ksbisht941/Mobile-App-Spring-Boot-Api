package io.appdeveloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.appdeveloperblog.app.ws.exceptions.UserServiceException;
import io.appdeveloperblog.app.ws.io.entity.UserEntity;
import io.appdeveloperblog.app.ws.io.repositories.UserRepository;
import io.appdeveloperblog.app.ws.service.UserService;
import io.appdeveloperblog.app.ws.shared.Utils;
import io.appdeveloperblog.app.ws.shared.dto.AddressDto;
import io.appdeveloperblog.app.ws.shared.dto.UserDto;
import io.appdeveloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("Record already exists");
		
		for (int i = 0; i < user.getAddresses().size(); i++) {
			AddressDto addressDto = user.getAddresses().get(i);
			addressDto.setUserDetails(user);
			addressDto.setAddressId(utils.generateAddressId(20));
			user.getAddresses().set(i, addressDto);
		}
 
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);
		
		
		String publicUserId = utils.generateUserId(15);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

		UserEntity storedUserDetail = userRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(storedUserDetail, UserDto.class);
		
		return returnValue;
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User with ID: " + userId + " not found");
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
//		return null;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		
		UserEntity updatedUserDetails = userRepository.save(userEntity);
		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		
		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> returnValue = new ArrayList<>();
		
		if (page>0) page = page-1;
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> userPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = userPage.getContent();
		
		for(UserEntity userEntity: users) {
			UserDto userDto= new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}
		
		return returnValue;
	}

}
