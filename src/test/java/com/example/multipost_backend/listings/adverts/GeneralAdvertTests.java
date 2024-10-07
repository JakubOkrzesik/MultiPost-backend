package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.restauth.AuthenticationRequest;
import com.example.multipost_backend.listings.dbModels.Listing;
import com.example.multipost_backend.listings.dbModels.ListingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GeneralAdvertTests {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ListingRepository listingRepository;
    private String jwtToken;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        AuthenticationRequest authRequest = new AuthenticationRequest("test@user.com", "1234");
        String authRequestJson = objectMapper.writeValueAsString(authRequest);

        System.out.println(authRequestJson);

        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        this.jwtToken = objectMapper.readTree(responseContent).get("token").asText();
    }

    @Test
    void bothPlatformsAdvertPostTest() throws Exception {

        File advertFile = new File("src/test/java/com/example/multipost_backend/listings/adverts/JsonTestFiles/advert1.json");

        JsonNode advertBody = objectMapper.readTree(advertFile);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/advert/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advertBody.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void allegroAdvertPost() throws Exception {
        File advertFile = new File("src/test/java/com/example/multipost_backend/listings/adverts/JsonTestFiles/advert2.json");

        JsonNode advertBody = objectMapper.readTree(advertFile);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/advert/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advertBody.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void olxAdvertPost() throws Exception {
        File advertFile = new File("src/test/java/com/example/multipost_backend/listings/adverts/JsonTestFiles/advert3.json");

        JsonNode advertBody = objectMapper.readTree(advertFile);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/advert/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(advertBody.toString()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getUserAdvertsTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/advert/user_adverts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void priceChangeTest() throws AdvertNotFoundException, Exception {
        List<Listing> userAdverts = listingRepository.findAllByUserId(402)
                .orElseThrow(() -> new AdvertNotFoundException("Adverts not found"));

        int listingId = userAdverts.get(0).getId();
        String url = String.format("/api/v1/advert/%d/price?newprice=6500", listingId);
        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void advertDeletionTest() throws AdvertNotFoundException, Exception {
        List<Listing> userAdverts = listingRepository.findAllByUserId(402)
                .orElseThrow(() -> new AdvertNotFoundException("Adverts not found"));

        int listingId = userAdverts.get(0).getId();
        String url = String.format("/api/v1/advert/delete/%d", listingId);
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
