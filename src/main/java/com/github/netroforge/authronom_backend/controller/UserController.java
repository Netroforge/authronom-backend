package com.github.netroforge.authronom_backend.controller;

import com.github.netroforge.authronom_backend.controller.dto.ApiErrorResponseDto;
import com.github.netroforge.authronom_backend.controller.dto.UserInfoResponseDto;
import com.github.netroforge.authronom_backend.db.repository.entity.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get info about current user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = UserInfoResponseDto.class)
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
    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo() {
        try {
            UserInfoResponseDto userInfoResponseDto =
                    userService.getUserInfo();
            return ResponseEntity.ok(userInfoResponseDto);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiErrorResponseDto(e.getMessage()));
        }
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
    @PutMapping("/user/change-email/start")
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
    @PutMapping("/user/change-email/finalize")
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
    @PutMapping("/user/change-password/start")
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
    @PutMapping("/user/change-password/finalize")
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
