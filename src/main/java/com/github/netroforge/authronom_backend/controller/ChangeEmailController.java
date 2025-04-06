package com.github.netroforge.authronom_backend.controller;

import com.github.netroforge.authronom_backend.controller.dto.ApiErrorResponseDto;
import com.github.netroforge.authronom_backend.repository.entity.*;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ChangeEmailController {
    private final UserService userService;

    public ChangeEmailController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Start email change process for user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserChangeEmailStartResponseDto.class)
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
    @PutMapping("/change-email/start")
    public ResponseEntity<?> startUserChangeEmail(
            @RequestBody UserChangeEmailStartRequestDto userChangeEmailStartRequestDto
    ) {
        try {
            UserChangeEmailStartResponseDto userChangeEmailStartResponseDto =
                    userService.startUserChangeEmail(userChangeEmailStartRequestDto);
            return ResponseEntity.ok(userChangeEmailStartResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(
            summary = "Finalize email change process for user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserChangeEmailFinalizeResponseDto.class)
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
    @PutMapping("/change-email/finalize")
    public ResponseEntity<?> finalizeUserChangeEmail(
            @RequestBody UserChangeEmailFinalizeRequestDto userChangeEmailFinalizeRequestDto
    ) {
        try {
            UserChangeEmailFinalizeResponseDto userChangeEmailFinalizeResponseDto =
                    userService.finalizeUserChangeEmail(userChangeEmailFinalizeRequestDto);
            return ResponseEntity.ok(userChangeEmailFinalizeResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }
}
