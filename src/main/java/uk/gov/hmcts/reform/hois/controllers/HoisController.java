package uk.gov.hmcts.reform.hois.controllers;

import static org.springframework.http.ResponseEntity.ok;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hois.domain.entities.ErrorDetail;
import uk.gov.hmcts.reform.hois.domain.entities.HomeOfficeResponse;
import uk.gov.hmcts.reform.hois.domain.entities.IAhoisResponse;

@Api(
    value = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class HoisController {

    @Autowired
    private transient ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("classpath:homeoffice-api-response-mock.json")
    private transient Resource resourceResFile;

    @ApiOperation(
        value = "Call Home Office API for Appeal related data",
        response = IAhoisResponse.class
    )

    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Appeal related data, as provided by Home office",
            response = IAhoisResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request"
        ),
        @ApiResponse(
            code = 403,
            message = "Forbidden"
        ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type"
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error"
        )
    })

    @PostMapping(path = "/hois")
    public ResponseEntity<IAhoisResponse> getHoisdata(
        @ApiParam(value = "Asylum appeal reference", required = true) @NotNull @RequestBody String iaHomeOfficeReference
    ) {
        IAhoisResponse response = new IAhoisResponse();
        response.setIaHomeOfficeReference(iaHomeOfficeReference);
        HomeOfficeResponse homeOfficeResponse;
        try {
            String hoResponseJson =
                new String(Files.readAllBytes(Paths.get(resourceResFile.getURI())));
            homeOfficeResponse = objectMapper.readValue(
                hoResponseJson,
                HomeOfficeResponse.class
            );
            response.setDecisionStatus(homeOfficeResponse.getStatuses()[0].getDecisionStatus());
            response.setPerson(homeOfficeResponse.getStatuses()[0].getPerson());

        } catch (IOException ioe) {
            response.setErrorDetail(new ErrorDetail("404", "Mock response not created", false));
        }

        return ok(response);
    }
}
