package za.co.wiizo.wiizoapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.wiizo.wiizoapi.entity.UserProfile;
import za.co.wiizo.wiizoapi.entity.VerificationCode;
import za.co.wiizo.wiizoapi.request.LoginRequest;
import za.co.wiizo.wiizoapi.response.ResponseHandler;
import za.co.wiizo.wiizoapi.service.UserProfileService;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserProfileService userProfileService;

    @Operation(summary = "New user", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = UserProfile.class)))})
    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody UserProfile userProfile) {
        userProfileService.createProfile(userProfile);
        return ResponseHandler.responseBuilder("Signed up successful ", HttpStatus.CREATED, null);
    }

    @Operation(summary = "login user", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LoginRequest.class)))})
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest) {

        UserProfile user = userProfileService.getUserByEmail(loginRequest.getEmail());
        if (user != null && !user.isActive()) {
            // User profile is not active, generate a new verification code
            String newVerificationCode = userProfileService.generateVerificationCode();
            VerificationCode userVerification = userProfileService.getUserVerificationByEmail(user);
            userVerification.setCode(newVerificationCode);
            LocalDateTime expirationTime = LocalDateTime.now().plusHours(24);
            userVerification.setExpirationTime(expirationTime);
            user.setVerificationCode(userVerification);

            // Save the updated user with the new verification code
            userProfileService.saveUser(user);

            // Return a response indicating that the profile is not active
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your profile is not yet activated. Please check your email for the verification code.");
        }


        return ResponseHandler.responseBuilder(null, HttpStatus.OK, userProfileService.login(loginRequest));
    }

    @Operation(summary = "user verification", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class)))})
    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<Object> verifyUser(@PathVariable String verificationCode) {

        UserProfile user = userProfileService.verifyUserByCode(verificationCode);
        if (user != null) {
            user.setActive(true);
            userProfileService.activateProfile(user);
            return ResponseHandler.responseBuilder("User verified and profile activated.", HttpStatus.OK, null);
        } else {
            return ResponseHandler.responseBuilder("Invalid or expired verification code.", HttpStatus.BAD_REQUEST, null);
        }

    }

    @Operation(summary = "user profile", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LoginRequest.class)))})
    @GetMapping("/user")
    public ResponseEntity<Object> getUserProfile(@Parameter String username) {

        return ResponseHandler.responseBuilder(null, HttpStatus.OK, userProfileService.getUserProfile(username));
    }

}
