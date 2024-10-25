package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroProduct;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.allegro.CategoryResponse;
import com.example.multipost_backend.listings.allegro.ProductWrapper;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.AllegroListingState;
import com.example.multipost_backend.listings.dbModels.UserKeysRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
public class AllegroService {

    // Consists mostly of requests made to the Allegro API and helper functions

    private final WebClient AllegroClient;
    private final GeneralService generalService;
    private final EnvService envService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserKeysRepository userKeysRepository;

    public AllegroTokenResponse getAllegroToken(String code) {
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post() // The allegro api does not work with the .bodyValue method inside webclient or im doing sth wrong (probably the case)
                .uri(String.format("/auth/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s/allegro-auth-callback", code, envService.getREDIRECT_URI()))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post()
                .uri("/auth/oauth/token?grant_type=client_credentials")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    private AllegroTokenResponse updateUserToken(String allegroRefreshToken) {
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post()
                .uri(String.format("/auth/oauth/token?grant_type=refresh_token&refresh_token=%s&redirect_uri=%s/api/v1/auth/allegro", allegroRefreshToken, envService.getREDIRECT_URI()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public ResponseEntity<JsonNode> createAdvert(JsonNode data, User user) {
        return AllegroClient.post()
                .uri("/sale/product-offers")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();
    }

    /* method checks for allegro advert status
    public JsonNode getAdvertStatus(String locationUrl, User user) {
        return AllegroClient.mutate().baseUrl(locationUrl).build().get()
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }*/

    public JsonNode getAdvert(String advertId, User user) {
        return AllegroClient.get()
                .uri(String.format("sale/product-offers/%s", advertId))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getAdverts(User user) {
        return AllegroClient.get()
                .uri("sale/offers")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode editAllegroOffer(JsonNode data, String advertId, User user) {
        return AllegroClient.patch()
                .uri(String.format("sale/product-offers/%s", advertId))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode updateAdvertPrice(int newPrice, String allegroId, User user) {
        ObjectNode updatedAdvertData = objectMapper.createObjectNode();
        ObjectNode sellingMode = objectMapper.createObjectNode();
        ObjectNode price = objectMapper.createObjectNode();
        price.put("amount", newPrice);
        price.put("currency", "PLN");
        sellingMode.set("price", price);
        updatedAdvertData.set("sellingMode", sellingMode);
        return editAllegroOffer(updatedAdvertData, allegroId, user);
    }

    public JsonNode changeAdvertStatus(String advertId, AllegroListingState listingState, User user) {
        ObjectNode updatedAdvertData = objectMapper.createObjectNode();
        ObjectNode publication = objectMapper.createObjectNode();
        publication.put("status", listingState.toString());
        updatedAdvertData.set("publication", publication);

        return editAllegroOffer(updatedAdvertData, advertId, user);
    }

    public CategoryResponse getCategorySuggestion(String suggestion) {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*return AllegroClient.get()
                .uri(String.format("sale/matching-categories?name=%s", suggestion))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*/

        return AllegroClient.get()
                .uri(String.format("sale/matching-categories?name=%s", suggestion))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(CategoryResponse.class)
                .block();
    }

    public ProductWrapper allegroProductSearch(String suggestion, String categoryID) {

        String url = String.format("/sale/products?phrase=%s&language=pl-PL&category.id=%s", suggestion, categoryID);

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*/

        return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(ProductWrapper.class)
                .block();
    }

    public ProductWrapper allegroGTINProductSearch(long GTIN) {

        String url = String.format("/sale/products?phrase=%s&language=pl-PL&mode=GTIN", GTIN);

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*/

        return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(ProductWrapper.class)
                .block();
    }

    public AllegroProduct getProduct(String ID) {
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*return AllegroClient.get()
                .uri(String.format("sale/products/%s", ID))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*/

        return AllegroClient.get()
                .uri(String.format("sale/products/%s", ID))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(AllegroProduct.class)
                .block();
    }

    public JsonNode getParams (String ID) {
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(String.format("sale/categories/%s/parameters", ID))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public String getUserToken(User user) {

        UserAccessKeys keys = user.getKeys();
        if (keys.getAllegroAccessToken() != null) {
            if (generalService.isTokenExpired(keys.getAllegroTokenExpiration())) {

                AllegroTokenResponse response;

                // check if we're dealing with a user token or an application token
                if (keys.getAllegroRefreshToken()==null) {
                    response = getClientToken();
                    keys.setAllegroAccessToken(response.getAccess_token());
                    keys.setAllegroTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                } else {
                    response = updateUserToken(keys.getAllegroRefreshToken());
                    // Updating the database
                    keys.setAllegroAccessToken(response.getAccess_token());
                    keys.setAllegroRefreshToken(response.getRefresh_token());
                    keys.setAllegroTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                }

                userKeysRepository.save(keys);
                user.setKeys(keys);
                userRepository.save(user);
                return keys.getAllegroAccessToken();
            }

            return keys.getAllegroAccessToken();
        }
        // User does not have Allegro credentials set up
        return "User needs to be signed in to the Allegro Api again";
    }

    // Creating Allegro headers
    private HttpHeaders getAllegroHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()));
        return headers;
    }
    // Mapping current state of an Allegro advert
    public AllegroListingState mapStateToEnum(String state) {
        return switch (state.toUpperCase()) {
            case "ACTIVE" -> AllegroListingState.ACTIVE;
            case "INACTIVE" -> AllegroListingState.INACTIVE;
            case "ACTIVATING" -> AllegroListingState.ACTIVATING;
            case "ENDED" -> AllegroListingState.ENDED;
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };
    }

}
