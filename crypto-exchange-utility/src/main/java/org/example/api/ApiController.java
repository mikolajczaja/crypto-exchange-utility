package org.example.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.Valid;
import lombok.Getter;
import org.example.exception.NoResultsFoundException;
import org.example.model.CurrencyExchangeRequestBody;
import org.example.model.CurrencyExchangeResponseBody;
import org.example.model.GetCurrenciesThirdPartyResponse;
import org.example.service.CommService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("currencies")
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    // it's not like every random internet crypto api shares an arbitrary list of "top currencies" 😅
    @Value("${default-currency-filters}")
    @Getter
    private List<String> defaultFilters = List.of("USD","EUR","BTC","ETH","PLN"); //for easier testing (instead of relying on @Value default resolution)

    //customization points: extract to a separate class and inject where needed
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final CommService commService;

    @Autowired
    public ApiController(CommService commService) {
        this.commService = commService;
    }

    /**
     * unique id of a crypto is required bc symbols are not (e.g. batcat - btc) - for list of ids check json file in "data" folder
     * <br><br>so the usage would be:
     * <br><code>currencies/bitcoin?filter[]=USDT&filter[]=ETH</code>
     * <br>instead of:
     * <br><code>currencies/BTC?filter[]=USDT&filter[]=ETH</code>
     * <h1>dont forget to encode the square brackets to conform to ✨the rules✨</h1>
     */
    @GetMapping("/{id}")
    ResponseEntity<String> getCurrencies(@PathVariable String id, @RequestParam(name = "filter[]", required = false) List<String> filters) {

        //customization points: change log levels (e.g. this one feels like it could be debug)
        log.info("called getCurrencies with id: {}, filters: {}", id, filters);

        if (CollectionUtils.isEmpty(filters)) {
            filters = defaultFilters;
            log.info("defaulted to default filters: {}", filters);
        }

        String thirdPartyResponseJson = commService.callSimplePrice(id, String.join(",", filters));
        log.info("received: {}", thirdPartyResponseJson);

        try {
            //customization points: potentially unnecessary conversion back and forth
            GetCurrenciesThirdPartyResponse thirdPartyResponse = GetCurrenciesThirdPartyResponse.fromJsonString(thirdPartyResponseJson, objectMapper);
            String resultJson = objectMapper.writeValueAsString(thirdPartyResponse);

            log.info("returning: {}", resultJson);
            return ResponseEntity.ok(resultJson);

        } catch (JsonProcessingException e) {
            log.error("got an error on json procesing: ", e);
            return ResponseEntity.internalServerError().build();
        } catch (NoResultsFoundException e) {
            log.error("no results returned from 3rd party for id: {} and filters: {}", id, filters);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("our source returned no results 😿");
        }
    }

    /**
     * Expects the "to" key to have an array as value, e.g.: <code>"to": ["eth","usd"]</code>
     */
    @PostMapping("/exchange")
    ResponseEntity<String> exchangeCurrency(@Valid @RequestBody CurrencyExchangeRequestBody requestBody) {
        //customization points:
        // parallel processing (sending this request + calculating ExchangeData from response) would make sense if that 3rd API wouldn't process multiple currencies at once, but it does
        // or maybe in case if said calculations would be complex,
        // or in case if we'd like to process multiple "from" currencies, and it wouldn't be supported by that 3rd party, but it is

        log.info("called exchangeCurrency with body: {}", requestBody);

        String thirdPartyResponseJson = commService.callSimplePrice(requestBody.from(), String.join(",", requestBody.to()));
        log.info("received: {}", thirdPartyResponseJson);

        try {
            CurrencyExchangeResponseBody responseBody = CurrencyExchangeResponseBody.fromJsonString(thirdPartyResponseJson, objectMapper, requestBody.from(), requestBody.amount());
            String resultJson = objectMapper.writeValueAsString(responseBody);

            log.info("returning: {}", resultJson);
            return ResponseEntity.ok(resultJson);

        } catch (JsonProcessingException e) {
            log.error("got an error on json procesing: ", e);
            return ResponseEntity.internalServerError().build();
        } catch (NoResultsFoundException e) {
            log.error("no results returned from 3rd party for id: {} and filters: {}", requestBody.from(), requestBody.to());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("our source returned no results 😿");
        }
    }

}