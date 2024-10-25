package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.OlxListingState;
import com.example.multipost_backend.listings.dbModels.UserKeysRepository;
import com.example.multipost_backend.listings.olx.*;
import com.example.multipost_backend.listings.olx.advertClasses.Location;
import com.example.multipost_backend.listings.olx.advertClasses.OlxAdvert;
import com.example.multipost_backend.listings.olx.advertClasses.Price;
import com.example.multipost_backend.listings.olx.advertClasses.SimplifiedOlxAdvert;
import com.example.multipost_backend.listings.olx.authentication.OlxClientRequest;
import com.example.multipost_backend.listings.olx.authentication.OlxRefreshRequest;
import com.example.multipost_backend.listings.olx.authentication.OlxTokenRequest;
import com.example.multipost_backend.listings.olx.authentication.OlxTokenResponse;
import com.example.multipost_backend.listings.sharedApiModels.GrantCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Service
@AllArgsConstructor
public class OlxService {

    // Consists mostly of requests made to the OLX API and helper functions

    private final WebClient OlxClient;
    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;
    private final EnvService envService;
    private final GeneralService generalService;
    private final ObjectMapper objectMapper;

    // constant usage of JsonNode needs evaluation
    public JsonNode createAdvert(JsonNode newAdvert, User user) {
        return OlxClient.post()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(user));
                })
                .bodyValue(newAdvert)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }


    public OlxObjectWrapperClass<SimplifiedOlxAdvert> getSimpleAdvert(String advertID, User user) {
        /*return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*/

        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxObjectWrapperClass<SimplifiedOlxAdvert>>() {
                })
                .block();
    }

    public OlxObjectWrapperClass<OlxAdvert> getModifiableOlxAdvert(String advertID, User user) {
        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxObjectWrapperClass<OlxAdvert>>() {
                })
                .block();
    }

    /*public OlxListWrapperClass<SimplifiedOlxAdvert> getAdverts(User user) {
        *//*return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();*//*

        return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<SimplifiedOlxAdvert>>() {
                })
                .block();
    }*/

    public JsonNode updateAdvert(OlxAdvert updatedAdvert, String advertID, User user) {
        return OlxClient.put()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .bodyValue(updatedAdvert)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode updateAdvertPrice(int newPrice, String advertID, User user) {
        OlxAdvert advert = getModifiableOlxAdvert(advertID, user).getData();
        assert advert!=null;

        advert.setPrice(Price.builder()
                .value(newPrice)
                .negotiable(false)
                .build());

        return updateAdvert(advert, advertID, user);
    }

    public ResponseEntity<Void> changeAdvertStatus(String advertID, String command, User user) {

        String uri = String.format("/partner/adverts/%s/commands?command", advertID);

        return OlxClient.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .body(fromFormData("command", command).with("is_success", "false"))
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    public void deleteAdvert(String advertID, User user) {
        OlxClient.delete()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    // OLX requires location to be acquired from their api - retrieving user specified coordinates from Google Maps api
    // and parsing them into the request body
    // needs reevaluation
    public Location getLocation(String lat, String lon) throws JsonProcessingException {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String data = OlxClient.get()
                .uri(String.format("/partner/locations?latitude=%s&longitude=%s", lat, lon))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode node = objectMapper.readTree(data).get("data").get(0);

        JsonNode cityNode = node.get("city");
        JsonNode districtNode = node.get("district");

        Location.LocationBuilder locationBuilder = Location.builder()
                .city_id(cityNode.get("id").asText());

        if (districtNode != null && !districtNode.isNull()) {
            locationBuilder.district_id(districtNode.get("id").asText());
        }

        return locationBuilder.build();
    }

    // Title is provided to extract a category ID
    public String getCategorySuggestion(String title) {

        // Not the most efficient way of going about it needs tweaking
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*String data = OlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode jsonData = objectMapper.readTree(data);*/

        OlxListWrapperClass<Category> response = OlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<Category>>() {
                })
                .block();

        assert response != null;
        return response.getData().get(0).getId();
    }

    public List<CategoryAttribs> getCategoryAttributes(String id) {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String uri = String.format("/partner/categories/%s/attributes", id);

        /*JsonNode rootNode = OlxClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        assert rootNode != null;

        List<JsonNode> attribList = new ArrayList<>();

        for (JsonNode node : rootNode.get("data")) {
            if (node.get("validation").get("required").asBoolean()) {
                attribList.add(node);
            }
        }*/

        OlxListWrapperClass<CategoryAttribs> result = OlxClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<CategoryAttribs>>() {
                })
                .block();

        List<CategoryAttribs> attribList = new ArrayList<>();

        assert result != null;
        for (CategoryAttribs attribs: result.getData()) {
            if (attribs.getValidation().isRequired()) {
                attribList.add(attribs);
            }
        }

        return attribList;
    }


    // Method provides the two always required headers for OLX Api requests
    public HttpHeaders getUserHeaders(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", getUserToken(user)));
        headers.set("Version", "2.0");
        return headers;
    }

    // Retrieves the access token, refresh token and access token expiration date after
    // getting auth code from olx
    public GrantCodeResponse getOlxToken(String code) {
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxTokenRequest.otRequestBuilder()
                        .grant_type("authorization_code")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .code(code)
                        .scope("v2 read write")
                        .build()
                )
                .retrieve()
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }


    // Retrieving the olx user token from the database. If the token is expired a request is made
    // with the refresh token to update the user token
    public String getUserToken(User user) {
        // Check if user is in cache and if the token is expired

        UserAccessKeys keys = user.getKeys();
        if (keys.getOlxAccessToken() != null) {
            if (generalService.isTokenExpired(keys.getOlxTokenExpiration())) {

                OlxTokenResponse response;
                // check if we're dealing with a user token or an application token
                if (keys.getOlxRefreshToken()==null) {
                    response = getApplicationToken();
                    keys.setOlxAccessToken(response.getAccess_token());
                    keys.setOlxTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                } else {
                    response = updateUserToken(keys.getOlxRefreshToken());
                    keys.setOlxAccessToken(response.getAccess_token());
                    keys.setOlxRefreshToken(response.getRefresh_token());
                    keys.setOlxTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                }

                userKeysRepository.save(keys);
                user.setKeys(keys);
                userRepository.save(user);
                return keys.getOlxAccessToken();
            }
            return keys.getOlxAccessToken();
        }
        // User does not have OLX credentials set up
        return "User needs to be signed in to the OLX Api again";
    }

    // Method makes request with the refresh token to update the user token
    private OlxTokenResponse updateUserToken(String refreshToken) {
        return OlxClient.post()
                .uri("/open/oauth/token").accept(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxRefreshRequest.orRequestBuilder()
                        .grant_type("refresh_token")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .refresh_token(refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(OlxTokenResponse.class)
                .block();
    }


    // Function that differentiates from getOlxToken is needed because of the differences in request body
    public OlxTokenResponse getApplicationToken() {
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxClientRequest.ocRequestBuilder()
                        .grant_type("client_credentials")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .scope("v2 read write")
                        .build())
                .retrieve()
                .bodyToMono(OlxTokenResponse.class)
                .block();
    }

    public OlxListingState mapStateToEnum(String state) {
        return switch (state.toUpperCase()) {
            case "NEW" -> OlxListingState.NEW;
            case "ACTIVE" -> OlxListingState.ACTIVE;
            case "LIMITED" -> OlxListingState.LIMITED;
            case "REMOVED_BY_USER" -> OlxListingState.REMOVED_BY_USER;
            case "OUTDATED" -> OlxListingState.OUTDATED;
            case "UNCONFIRMED" -> OlxListingState.UNCONFIRMED;
            case "UNPAID" -> OlxListingState.UNPAID;
            case "MODERATED" -> OlxListingState.MODERATED;
            case "BLOCKED" -> OlxListingState.BLOCKED;
            case "DISABLED" -> OlxListingState.DISABLED;
            case "REMOVED_BY_MODERATOR" -> OlxListingState.REMOVED_BY_MODERATOR;
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };
    }

}
