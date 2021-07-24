package com.boot.cart.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;
import com.boot.services.dto.UserDTO;

public class UserServiceClient {

	@Autowired
	private RestTemplate restTemplate;

	public UserDTO callGetUserByUserName(String userName) {

		return restTemplate.getForEntity(Constants.GET_USER_BY_USER_NAME + userName, UserDTO.class).getBody();
	}

	public void callUpdateUser(String userName, UserDTO user) {
		restTemplate.exchange(Constants.UPDATE_USER + userName, HttpMethod.PUT, new HttpEntity<>(user), String.class);
	}
}