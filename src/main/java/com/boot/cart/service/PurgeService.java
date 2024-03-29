package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.boot.cart.model.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.repository.CartRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PurgeService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductServiceClient productServiceClient;

//    @Scheduled(cron = "${cron.expression}", zone = "Europe/Paris")
//    public void emptyProductsFromCart() {
//        //I think that this should be linked with user session. Maybe the user has not done any operation on the cart but he is having a hard time choosing the next product.
//        Set<Cart> cartSet = cartRepository.findByLastUpdatedOnBefore(LocalDateTime.now().minusHours(3));
//
//        for (Cart cart : cartSet) {
//
//            List<Product> prodList = cart.getProductList();
//
//            Iterator<Product> iter = prodList.iterator();
//
//            while (iter.hasNext()) {
//                Product product = iter.next();
//                iter.remove();
//                log.info( "{} succesfully deleted from Product List",product.getProductName());
//                product.setProductStock(product.getProductStock() + 1);
//                log.info("{}  Productstock succesfully updated! currently {} products in stock!",product.getProductName(),product.getProductStock());
//
//                //you could optimize this a little by doing one operation for multiple products of same type from the same cart
//                productServiceClient.callUpdateProductByProductName(product.getProductName(),
//                        ProductMapper.ProductEntityToDto(product));
//
//            }
//            cartRepository.delete(cart);
//            log.info("Cart succesfully deleted!");
//        }

 //   }

}
