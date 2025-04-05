package com.github.netroforge.authronom_backend.controller;

import com.github.netroforge.authronom_backend.controller.dto.ApiErrorResponseDto;
import com.github.netroforge.authronom_backend.repository.entity.UserChangePasswordFinalizeRequestDto;
import com.github.netroforge.authronom_backend.repository.entity.UserChangePasswordFinalizeResponseDto;
import com.github.netroforge.authronom_backend.repository.entity.UserChangePasswordStartResponseDto;
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
public class ChangePasswordController {
    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Start change password for user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserChangePasswordStartResponseDto.class)
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
    @PutMapping("/change-password/start")
    public ResponseEntity<?> startUserChangePassword() {
        try {
            UserChangePasswordStartResponseDto userChangePasswordStartResponseDto =
                    userService.startUserChangePassword();
            return ResponseEntity.ok(userChangePasswordStartResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(
            summary = "Finalize change password for user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserChangePasswordFinalizeResponseDto.class)
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
    @PutMapping("/change-password/finalize")
    public ResponseEntity<?> finalizeUserChangePassword(
            @RequestBody UserChangePasswordFinalizeRequestDto userChangePasswordFinalizeRequestDto
    ) {
        try {
            UserChangePasswordFinalizeResponseDto userChangePasswordFinalizeResponseDto =
                    userService.finalizeUserChangePassword(userChangePasswordFinalizeRequestDto);
            return ResponseEntity.ok(userChangePasswordFinalizeResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
    }
}
