package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olx.Advert;
import com.example.multipost_backend.listings.olx.AdvertiserType;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;


@SpringBootTest()
class AdvertControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OlxService olxService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void olxUserTokenTest() {
        // Before starting the test the user needs to have a valid olx access token already stored in the database
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        assertThat(olxService.getUserToken(user)).isNotNull();
    }

    @Test
    void postOLXAdvert() throws IOException {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Advert advert = Advert.builder()
                .title("Samsung Galaxy S4")
                .description("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent eleifend nunc quis orci condimentum, scelerisque pretium velit tincidunt. Nunc pellentesque convallis ante ut efficitur. Praesent vehicula ultricies lorem nec consequat. Curabitur urna dolor, viverra non nunc sed, lobortis ullamcorper arcu. Donec mollis dui leo. Aliquam eget faucibus mi. Sed feugiat, ligula at cursus facilisis, purus diam interdum quam, ut gravida libero urna non odio. Morbi hendrerit condimentum tincidunt. Cras at sapien eu metus pulvinar luctus. Sed sed nibh molestie, suscipit nisl.")
                .location(olxService.getLocation("50.049683", "19.944544"))
                .category_id(olxService.getCategorySuggestion("Samsung Galaxy S4"))
                .advertiserType(AdvertiserType.PRIVATE)
                .name("Maniek")
                .images(new String[]{"https://files.refurbed.com/ii/16-gb-weiss-1562656669.jpg?t=fitdesign&h=600&w=800", "https://files.refurbed.com/ii/16-gb-weiss-1562656934.jpg?t=fitdesign&h=600&w=800"})
                .price("500")
                .build();

        JsonNode response2 = olxService.createAdvert(objectMapper.valueToTree(advert), user);

        System.out.println(response2.asText());

        assertThat(response2.get("status").asText()).contains("active");
    }

    @Test
    void postAllegroAdvert() {

    }

    @Test
    void postEbayAdvert() {

    }
}