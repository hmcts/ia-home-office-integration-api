package uk.gov.hmcts.reform.hois.controllers;

import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hois.domain.entities.HomeOfficeAppealData;
import uk.gov.hmcts.reform.hois.domain.entities.HomeOfficeRequest;

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

    @ApiOperation(
        value = "Call Home Office API for Appeal related data",
        response = HomeOfficeAppealData.class
    )

    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Appeal related data, as provided by Home office",
            response = HomeOfficeAppealData.class
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
    public ResponseEntity<HomeOfficeAppealData> getHoisdata(
        @ApiParam(value = "Asylum appeal reference", required = true) @NotNull @RequestBody HomeOfficeRequest request
    ) {
        return ok(new HomeOfficeAppealData());
    }
}
