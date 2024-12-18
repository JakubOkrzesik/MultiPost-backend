package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.OlxListingState;
import com.example.multipost_backend.listings.olxModels.*;
import com.example.multipost_backend.listings.olxModels.advertClasses.Location;
import com.example.multipost_backend.listings.olxModels.advertClasses.OlxAdvert;
import com.example.multipost_backend.listings.olxModels.advertClasses.Price;
import com.example.multipost_backend.listings.olxModels.advertClasses.SimplifiedOlxAdvert;
import com.example.multipost_backend.listings.olxModels.authentication.OlxClientRequest;
import com.example.multipost_backend.listings.olxModels.authentication.OlxRefreshRequest;
import com.example.multipost_backend.listings.olxModels.authentication.OlxTokenRequest;
import com.example.multipost_backend.listings.olxModels.authentication.OlxTokenResponse;
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
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OlxService {

    // Consists mostly of requests made to the OLX API and helper functions

    private final RestClient OlxClient;
    private final EnvService envService;
    private final GeneralService generalService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserKeysService userKeysService;

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
                .body(newAdvert)
                .retrieve()
                .body(JsonNode.class);
    }


    public OlxObjectWrapperClass<SimplifiedOlxAdvert> getSimpleAdvert(String advertID, User user) {
        /*return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(JsonNode.class)
                ;*/

        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(new ParameterizedTypeReference<OlxObjectWrapperClass<SimplifiedOlxAdvert>>() {
                });
    }

    public OlxObjectWrapperClass<OlxAdvert> getModifiableOlxAdvert(String advertID, User user) {
        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(new ParameterizedTypeReference<OlxObjectWrapperClass<OlxAdvert>>() {
                });
    }

    /*public OlxListWrapperClass<SimplifiedOlxAdvert> getAdverts(User user) {
        *//*return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(JsonNode.class)
                ;*//*

        return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(new ParameterizedTypeReference<OlxListWrapperClass<SimplifiedOlxAdvert>>() {
                })
                ;
    }*/

    public JsonNode updateAdvert(OlxAdvert updatedAdvert, String advertID, User user) {
        return OlxClient.put()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .body(updatedAdvert)
                .retrieve()
                .body(JsonNode.class);
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

        Map<String, String> formData = new HashMap<>();
        formData.put("command", command);
        formData.put("is_success", "false");

        return OlxClient.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .body(formData)
                .retrieve()
                .toBodilessEntity();
    }


    public void deleteAdvert(String advertID, User user) {
        OlxClient.delete()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .toBodilessEntity();
    }


    // OLX requires location to be acquired from their api - retrieving user specified coordinates from Google Maps api
    // and parsing them into the request body
    // needs reevaluation
    public Location getLocation(String lat, String lon) throws JsonProcessingException {

        User user = userService.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String data = OlxClient.get()
                .uri(String.format("/partner/locations?latitude=%s&longitude=%s", lat, lon))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(String.class);

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
        User user = userService.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*String data = OlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(String.class)
                ;

        JsonNode jsonData = objectMapper.readTree(data);*/

        OlxListWrapperClass<Category> response = OlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(new ParameterizedTypeReference<OlxListWrapperClass<Category>>() {
                });

        assert response != null;
        return response.getData().get(0).getId();
    }

    public List<CategoryAttribs> getCategoryAttributes(String id) {

        User user = userService.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String uri = String.format("/partner/categories/%s/attributes", id);

        /*JsonNode rootNode = OlxClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .body(JsonNode.class)
                ;

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
                .body(new ParameterizedTypeReference<OlxListWrapperClass<CategoryAttribs>>() {
                });

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
                .body(OlxTokenRequest.otRequestBuilder()
                        .grant_type("authorization_code")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .code(code)
                        .scope("v2 read write")
                        .build()
                )
                .retrieve()
                .body(GrantCodeResponse.class);
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

                userKeysService.saveKeys(keys);
                user.setKeys(keys);
                userService.saveUser(user);
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
                .body(OlxRefreshRequest.orRequestBuilder()
                        .grant_type("refresh_token")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .refresh_token(refreshToken)
                        .build())
                .retrieve()
                .body(OlxTokenResponse.class);
    }


    // Function that differentiates from getOlxToken is needed because of the differences in request body
    public OlxTokenResponse getApplicationToken() {
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(OlxClientRequest.ocRequestBuilder()
                        .grant_type("client_credentials")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .scope("v2 read write")
                        .build())
                .retrieve()
                .body(OlxTokenResponse.class);
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
