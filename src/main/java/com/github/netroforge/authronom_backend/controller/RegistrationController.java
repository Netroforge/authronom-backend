package com.github.netroforge.authronom_backend.controller;

import com.github.netroforge.authronom_backend.controller.dto.ApiErrorResponseDto;
import com.github.netroforge.authronom_backend.db.repository.primary.entity.UserRegistrationFinalizeRequestDto;
import com.github.netroforge.authronom_backend.db.repository.primary.entity.UserRegistrationFinalizeResponseDto;
import com.github.netroforge.authronom_backend.db.repository.primary.entity.UserRegistrationStartRequestDto;
import com.github.netroforge.authronom_backend.db.repository.primary.entity.UserRegistrationStartResponseDto;
import com.github.netroforge.authronom_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RegistrationController {
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Start user registration by email and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserRegistrationStartResponseDto.class)
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Fail",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponseDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Fail",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponseDto.class)
                            )
                    }
            )
    })
    @PostMapping("/registration/start")
    public ResponseEntity<?> startRegister(
            @RequestBody UserRegistrationStartRequestDto userRegistrationStartRequestDto
    ) {
        try {
            UserRegistrationStartResponseDto userRegistrationResponseDto =
                    userService.startUserRegistration(userRegistrationStartRequestDto);
            return ResponseEntity.ok(userRegistrationResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(
            summary = "Finalize user registration by email and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserRegistrationFinalizeResponseDto.class)
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Fail",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponseDto.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Fail",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponseDto.class)
                            )
                    }
            )
    })
    @PostMapping("/registration/finalize")
    public ResponseEntity<?> finalizeRegister(
            @RequestBody UserRegistrationFinalizeRequestDto userRegistrationFinalizeRequestDto
    ) {
        try {
            UserRegistrationFinalizeResponseDto userRegistrationFinalizeResponseDto =
                    userService.finalizeUserRegistration(userRegistrationFinalizeRequestDto);
            return ResponseEntity.ok(userRegistrationFinalizeResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }
}
