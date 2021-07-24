package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.repository.CartRepository;
import com.boot.services.dto.CartDTO;
import com.boot.services.dto.ProductDTO;
import com.boot.services.mapper.CartMapper;
import com.boot.services.mapper.ProductMapper;
import com.boot.services.mapper.UserMapper;
import com.boot.services.model.Cart;
import com.boot.services.model.Product;
import com.boot.services.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CartService {

	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	ProductServiceClient productServiceClient;
	
	@Autowired
	UserServiceClient userServiceClient;

	public CartDTO addProductToCart(String userName, String productName, int quantity)
			throws InvalidInputDataException, EntityNotFoundException {
		log.info("addProductToCart - process started");
		Product product;
		try {
			product = ProductMapper.DtoToProductEntity(productServiceClient.callGetProductByProductName(productName));
		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
		}
		if (quantity <= 0) {
			throw new InvalidInputDataException("Your Value has to be above 0!");
		}

		if (product.getProductStock() == 0) {
			throw new InvalidInputDataException("We are sorry, but currently: " + productName + " is out of order!");
		}

		if (product.getProductStock() < quantity) {
			throw new InvalidInputDataException("You can not add more than: " + product.getProductStock() + " "
					+ productName + " Products to your shopping cart!");
		}
		User user;
		try {
			user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByUserName(userName));
		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
		}
		Cart cart = cartRepository.findByUser(user);

		List<Product> productList;
		double productTotal = 0;
		if (cart == null) {
			cart = new Cart();
			productList = new ArrayList<>();
		} else {
			productList = cart.getProductList();
		}

		cart.setUser(user);

		for (int i = 0; i < quantity; i++) {
			productList.add(product);
			productTotal = product.getProductPrice() + productTotal;
		}

		cart.setProductList(productList);
		cart.setLastUpdatedOn(LocalDateTime.now());
		cart.setTotal(cart.getTotal() + productTotal);

		product.setProductStock(product.getProductStock() - quantity);
		productServiceClient.callUpdateProductByProductName(productName, ProductMapper.ProductEntityToDto(product));

		cartRepository.save(cart);

		return CartMapper.cartEntityToDto(cart);

	}

	public CartDTO removeProductFromCart(String userName, String productName, int quantity)
			throws InvalidInputDataException, EntityNotFoundException {

		Product product;
		try {
			product = ProductMapper.DtoToProductEntity(productServiceClient.callGetProductByProductName(productName));
		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
		}

		User user;
		try {
			user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByUserName(userName));
		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
		}
		Cart cart = cartRepository.findByUser(user);
		List<Product> productList = cart.getProductList();

		Long productsInCart = productList.stream().filter(p -> p.getProductName().equals(productName)).count();

		if (quantity <= 0) {
			throw new InvalidInputDataException("Your Value has to be above 0!");
		}

		if (productsInCart == 0) {
			throw new InvalidInputDataException(
					"You currently have " + productsInCart + " Products: " + productName + " in cart!");
		}

		if (Math.toIntExact(productsInCart) < quantity) {
			throw new InvalidInputDataException("You cannot remove more than " + productsInCart + " " + productName
					+ " Products from the shopping cart!");
		} else {
			double productTotal = 0;

			for (int i = 0; i < quantity; i++) {
				productList.remove(product);
				productTotal = product.getProductPrice() + productTotal;
			}

			cart.setUser(user);

			cart.setProductList(productList);
			cart.setLastUpdatedOn(LocalDateTime.now());
			cart.setTotal(cart.getTotal() - productTotal);

			product.setProductStock(product.getProductStock() + quantity);

			cartRepository.save(cart);
			productServiceClient.callUpdateProductByProductName(productName, ProductMapper.ProductEntityToDto(product));

			return CartMapper.cartEntityToDto(cart);
		}
	}

	public void deleteCartByUserName(String userName) throws InvalidInputDataException, EntityNotFoundException {

		User user;
		try {
			user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByUserName(userName));
		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
		}

		if (cartRepository.findByUser(user) != null) {
			Cart cart = cartRepository.findByUser(user);

			List<Product> prodList = cart.getProductList();

			Iterator<Product> iter = prodList.iterator();

			while (iter.hasNext()) {
				Product product = iter.next();
				iter.remove();
				log.info(product.getProductName() + " - succesfully deleted from Product List");
				product.setProductStock(product.getProductStock() + 1);
				log.info(product.getProductName() + " Productstock succesfully updated! currently "
						+ product.getProductStock() + " products in stock!");

				productServiceClient.callUpdateProductByProductName(product.getProductName(),
						ProductMapper.ProductEntityToDto(product));

			}
			cartRepository.delete(cart);
			log.info("Cart succesfully deleted!");
		}
		log.info("Cart is empty (null)!");
	}

	public CartDTO getCartByUserName(String userName) throws EntityNotFoundException {
		User user;
		try {
			user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByUserName(userName));

		} catch (HttpClientErrorException.NotFound e) {
			throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
		}
		Cart cart = cartRepository.findByUser(user);
		return CartMapper.cartEntityToDto(cart);
	}

	public Set<CartDTO> getAllCarts() throws EntityNotFoundException {
		if (cartRepository.findAll().isEmpty()) {
			throw new EntityNotFoundException("No cart found in the Database!");
		}
		List<Cart> allCarts = cartRepository.findAll();
		return CartMapper.cartEntityToDtoList(allCarts);
	}

	public Map<String, Integer> getMostPopularProducts() throws EntityNotFoundException {

		Set<CartDTO> allCarts = getAllCarts();
		HashMap<String, Integer> map = new HashMap<>();

		for (CartDTO cart : allCarts) {

			for (ProductDTO product : cart.getProductList()) {

				if (!map.containsKey(product.getProductName())) {
					map.put(product.getProductName(), 1);
				} else {
					map.put(product.getProductName(), map.get(product.getProductName()) + 1);
				}
			}
		}

		Map<String, Integer> topTenPoducts = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(10)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return topTenPoducts;
	}

	public int getNumberOfActiveCarts() throws EntityNotFoundException {

		return getAllCarts().size();
	}

	public long getNumberOfUsersWithRequestedProductInCart(String productName) throws EntityNotFoundException {

		List<ProductDTO> result = getAllCarts().stream()
				.flatMap(
						s -> s.getProductList().stream().filter(p -> p.getProductName().equals(productName)).distinct())
				.collect(Collectors.toList());

		return result.size();
	}

}