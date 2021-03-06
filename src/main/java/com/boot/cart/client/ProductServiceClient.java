package com.boot.cart.client;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.boot.cart.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;


@Component
public class ProductServiceClient {

    @Autowired
    private RestTemplate productServiceRestTemplate;

    public List<ProductDTO> callGetAllProducts() {

        ProductDTO[] productArray = productServiceRestTemplate.getForEntity(Constants.GET_ALL_PRODUCTS, ProductDTO[].class)
                .getBody();

        return Arrays.asList(productArray);
    }

    public ProductDTO callGetProductByProductName(String productName) {

        return productServiceRestTemplate.getForEntity(Constants.GET_PRODUCT_BY_PRODUCT_NAME, ProductDTO.class, productName).getBody();
    }

    public void callUpdateProductByProductName(String productName, ProductDTO productDto) {
        productServiceRestTemplate.exchange(Constants.UPDATE_PRODUCT_BY_PRODUCT_NAME, HttpMethod.PUT,
                new HttpEntity<>(productDto), String.class, productName);
    }

    public List<ProductDTO> callGetAllProductsFromUser(String productNames, Boolean includeInactive ) {

        return Arrays.asList(Objects.requireNonNull(productServiceRestTemplate.getForEntity(Constants.GET_ALL_PRODUCTS_FOR_USER, ProductDTO[].class, productNames, includeInactive).getBody()));
    }
}