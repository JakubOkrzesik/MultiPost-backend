package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olx.*;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class OLXAdvertTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OlxService olxService;

    @Test
    void olxUserTokenTest() {
        // Before starting the test the user needs to have a valid olx access token already stored in the database
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        assertThat(olxService.getUserToken(user)).isNotNull();
    }

    @Test
    void olxAdvertCreationTest() throws IOException {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Image image1 = new Image("https://files.refurbed.com/ii/16-gb-weiss-1562656669.jpg?t=fitdesign&h=600&w=800");
        Image image2 = new Image("https://files.refurbed.com/ii/16-gb-weiss-1562656669.jpg?t=fitdesign&h=600&w=800");

        List<Image> images = new ArrayList<>();
        images.add(image1);
        images.add(image2);

        Contact contact = Contact.builder()
                .name("Maniek")
                .build();

        Price price = Price.builder()
                .value(900)
                .negotiable(true)
                .build();


        String id = olxService.getCategorySuggestion("Samsung Galaxy A-12");

        /*Advert advert = Advert.builder()
                .title("Samsung Galaxy S4")
                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent eleifend nunc quis orci condimentum, scelerisque pretium velit tincidunt. Nunc pellentesque convallis ante ut efficitur. Praesent vehicula ultricies lorem nec consequat. Curabitur urna dolor, viverra non nunc sed, lobortis ullamcorper arcu. Donec mollis dui leo. Aliquam eget faucibus mi. Sed feugiat, ligula at cursus facilisis, purus diam interdum quam, ut gravida libero urna non odio. Morbi hendrerit condimentum tincidunt. Cras at sapien eu metus pulvinar luctus. Sed sed nibh molestie, suscipit nisl.")
                .categoryId(id)
                .location(olxService.getLocation("50.049683", "19.944544"))
                .advertiserType("private")
                .contact(contact)
                .images(images)
                .price(price)
                .attributes(olxService.getCategoryAttributes(id))
                .build();

        JsonNode response = olxService.createAdvert(advert, user);
        System.out.println(response.get("data"));*/
    }


    @Test
    void gettingOLXAdvert() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Provide your desired ID
        String adId = "920445171";
        JsonNode response = olxService.getAdvert(adId, user).get("data");
        System.out.println(response);
        // Checking the mechanism for getting the ad
        assertThat(response.get("status").asText()).isEqualTo("active");
    }


    @Test
    void olxAdvertEditTest() {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Provide your desired ID
        String adId = "911458629";
        JsonNode response = olxService.getAdvert(adId, user).get("data");
        // Checking the mechanism for getting the ad
        assertThat(response.get("status").asText()).isEqualTo("active");

        // Trying to modify the advert
        // You have to pass all the required fields again to modify the advert

        ObjectNode updatedAd = (ObjectNode) response;
        updatedAd.remove("id");
        updatedAd.remove("status");
        updatedAd.remove("url");
        updatedAd.remove("created_at");
        updatedAd.remove("activated_at");
        updatedAd.remove("valid_to");
        updatedAd.remove("salary");
        JsonNode attributesNode = updatedAd.get("attributes");
        if (attributesNode.isArray()) {
            // Iterate over each attribute object
            for (JsonNode attributeNode : attributesNode) {
                // Remove the "values" field from each attribute object otherwise the advert won't pass
                ((ObjectNode) attributeNode).remove("values");
            }
        }
        updatedAd.put("title", "Telefon Samsung Galaxy A-12");

        JsonNode response2 = olxService.updateAdvert(updatedAd, adId, user).get("data");

        assertThat(response2).isNotNull();

    }

    @Test
    void olxPriceUpdateTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = olxService.updateAdvertPrice(6900, "920446688", user);
        System.out.println(response);

    }

    @Test
    void olxAdvertDeletionTest() {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Provide your desired ID
        String adId = "911458629";
        String command = "finish";

        ResponseEntity<Void> statusChangeResponse = olxService.changeAdvertStatus(adId, command, user);
        assertThat(statusChangeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> deletionResponse = olxService.deleteAdvert(adId, user);
        assertThat(deletionResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}