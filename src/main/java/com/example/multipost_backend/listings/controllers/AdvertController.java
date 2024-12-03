package com.example.multipost_backend.listings.controllers;


import com.example.multipost_backend.listings.adverts.AdvertNotFoundException;
import com.example.multipost_backend.listings.allegroModels.AllegroApiException;
import com.example.multipost_backend.listings.olxModels.OlxApiException;
import com.example.multipost_backend.listings.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/advert")
@RequiredArgsConstructor
public class AdvertController {

    private final ResponseHandlerService responseHandler;
    private final AdvertService advertService;
    private static final Logger logger = LoggerFactory.getLogger(AdvertController.class);

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAdvert(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(advertService.getAdvertById(id));
        } catch (AdvertNotFoundException e) {
            return new ResponseEntity<>("An advert with the provided id does not exist.", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("An internal server error has occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/user_adverts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAdverts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            return ResponseEntity.ok(advertService.getUserAdverts(authHeader));
        } catch(Exception e) {
            logger.error(String.valueOf(e));
            return responseHandler.generateResponse("Internal error while fetching adverts", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Object> changeAdvertPrice(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable Integer id, @RequestParam("newprice") int price) {
        try {

            advertService.changeAdvertPrice(authHeader, id, price);
            return responseHandler.generateResponse("Price of advert changed", HttpStatus.OK, null);

        } catch (AdvertNotFoundException e) {

            logger.error(String.valueOf(e));
            return new ResponseEntity<>("An advert with the provided id does not exist.", HttpStatus.NO_CONTENT);

        } catch (RuntimeException e) {

            logger.error(String.valueOf(e));
            return responseHandler.generateResponse("Error while fetching api data", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

        } catch (Exception e) {

            logger.error(String.valueOf(e));
            return responseHandler.generateResponse("Internal error while fetching adverts", HttpStatus.INTERNAL_SERVER_ERROR, e);

        }
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody JsonNode jsonData) {

        try {
            return responseHandler.generateResponse("Advert successfully posted", HttpStatus.OK, advertService.postAdvert(authHeader, jsonData));
        } catch (InvalidRequestStateException e) {
            return responseHandler.generateResponse("Error encountered while posting advert", HttpStatus.OK, e.getMessage());
        } catch (OlxApiException e) {
            return responseHandler.generateResponse("Error while posting the advert on Olx", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (AllegroApiException e) {
            return responseHandler.generateResponse("Error while posting the advert on Allegro", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return responseHandler.generateResponse("Internal error while posting advert", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable int id) throws AdvertNotFoundException {

        // advert will be deleted from database and delisted on allegro and olx

        try {
            advertService.deleteAdvert(authHeader, id);
            return responseHandler.generateResponse("Deleted advert successfully", HttpStatus.NO_CONTENT, null);
        }  catch (OlxApiException e) {
            return responseHandler.generateResponse("Error while deleting the advert on Olx", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (AllegroApiException e) {
            return responseHandler.generateResponse("Error while deleting the advert on Allegro", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return responseHandler.generateResponse("Internal error while processing your request", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

}