package za.co.wiizo.wiizoapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import za.co.wiizo.wiizoapi.dto.UpdatePreferencesDTO;
import za.co.wiizo.wiizoapi.dto.UpdateUserDTO;
import za.co.wiizo.wiizoapi.entity.UserProfile;
import za.co.wiizo.wiizoapi.response.ResponseHandler;
import za.co.wiizo.wiizoapi.service.UserProfileService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserProfileService userProfileService;

    @Operation(summary = "update user", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = UpdateUserDTO.class)))})
    @PutMapping("")
    public ResponseEntity<Object> updateProfile(@RequestBody UpdateUserDTO userProfile) {
        try {
            UserProfile updatedUser = userProfileService.updateProfile(userProfile);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during user profile update.");
        }
    }

    @Operation(summary = "update user", description = "", tags = {"auth"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = UpdateUserDTO.class)))})
    @PutMapping("/preferences")
    public ResponseEntity<Object> updatePreferences(@RequestBody UpdatePreferencesDTO updatePreferences) {
        try {
            userProfileService.updatePreferences(updatePreferences);

            return ResponseEntity.status(HttpStatus.OK).body("User Preferences updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during user profile update.");
        }
    }
}
