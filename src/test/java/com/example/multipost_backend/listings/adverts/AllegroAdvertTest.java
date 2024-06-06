package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.AllegroListingState;
import com.example.multipost_backend.listings.services.AllegroService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AllegroAdvertTest {

    @Autowired
    private AllegroService allegroService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    /*@Test
    void advertCreationTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ObjectNode json = objectMapper.createObjectNode();
        ObjectNode productSet = objectMapper.createObjectNode();
        ObjectNode product = objectMapper.createObjectNode();
        ArrayNode parameters = objectMapper.createArrayNode();
        ObjectNode innerParams = objectMapper.createObjectNode();
        innerParams.put("name", "Średnica obiektywu");
        innerParams.set("values", objectMapper.createArrayNode().add("3.4"));
        parameters.add(innerParams);
        String productId = "065bb735-4257-44d4-93f6-4f7decc71150";
        product.put("id", productId);
        product.set("parameters", parameters);
        productSet.set("product", product);
        json.set("productSet", objectMapper.createArrayNode().add(productSet));
        ObjectNode sellingMode = objectMapper.createObjectNode();
        ObjectNode price = objectMapper.createObjectNode();
        price.put("amount", "999.99");
        price.put("currency", "PLN");
        sellingMode.set("price", price);
        ObjectNode stock = objectMapper.createObjectNode();
        stock.put("available", 10);
        json.set("stock", stock);
        json.set("sellingMode", sellingMode);
        JsonNode response = allegroService.createAdvert(json, user);
        System.out.println(response);
    }*/

    // https://developer.allegro.pl/tutorials/jak-jednym-requestem-wystawic-oferte-powiazana-z-produktem-D7Kj9gw4xFA#jak-wystawic-oferte-z-produktem-za-pomoca-zasobu-sale-product-offers
    // posting advert guide

    @Test
    void advertCreationWithUserParams() {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ObjectNode json = objectMapper.createObjectNode();
        JsonNode productSearch = allegroService.allegroProductSearch("Iphone XS Max", "4").get("products");
        String productId = productSearch.get(0).get("id").asText();

        // Constructing the productSet array
        ObjectNode productSet = objectMapper.createObjectNode();
        ObjectNode product = objectMapper.createObjectNode();
        product.put("id", productId);
        product.put("name", "Iphone XS Max giga okazja");
        ArrayNode imagesArray = objectMapper.createArrayNode();
        imagesArray.add("https://merkandi.pl/download/849570/img-6138.jpg");
        imagesArray.add("https://img.merkandi.com/imgcache/resized/images/offer/2020/04/06//0ca7bbce-ab2c-4601-bf81-5665d91ec556-1586163450.jpg");
        product.set("images", imagesArray);

        ArrayNode parameters = objectMapper.createArrayNode();
        ObjectNode innerParams = objectMapper.createObjectNode();
        innerParams.put("name", "Średnica obiektywu");
        innerParams.set("values", objectMapper.createArrayNode().add("3.4"));
        parameters.add(innerParams);
        product.set("parameters", parameters);

        productSet.set("product", product);
        json.set("productSet", objectMapper.createArrayNode().add(productSet));

        // Constructing the sellingMode object
        ObjectNode sellingMode = objectMapper.createObjectNode();
        ObjectNode price = objectMapper.createObjectNode();
        price.put("amount", "6969");
        price.put("currency", "PLN");
        sellingMode.set("price", price);
        json.set("sellingMode", sellingMode);

        // Constructing the stock object
        ObjectNode stock = objectMapper.createObjectNode();
        stock.put("available", 1);
        json.set("stock", stock);

        /*JsonNode response = allegroService.createAdvert(json, user);
        System.out.println(response);*/
    }

    @Test
    void updatePrice() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = allegroService.updateAdvertPrice(6900, "7764007892", user);
        System.out.println(response);
    }

    @Test
    void delistAdvert() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = allegroService.changeAdvertStatus("7764007892", AllegroListingState.ENDED, user);
        System.out.println(response);
    }
}