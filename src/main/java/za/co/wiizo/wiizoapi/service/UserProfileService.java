package za.co.wiizo.wiizoapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.wiizo.wiizoapi.entity.Role;
import za.co.wiizo.wiizoapi.entity.RoleName;
import za.co.wiizo.wiizoapi.entity.UserProfile;
import za.co.wiizo.wiizoapi.entity.VerificationCode;
import za.co.wiizo.wiizoapi.repository.RoleRepository;
import za.co.wiizo.wiizoapi.repository.UserProfileRepository;
import za.co.wiizo.wiizoapi.repository.VerificationCodeRepository;
import za.co.wiizo.wiizoapi.request.LoginRequest;
import za.co.wiizo.wiizoapi.response.JwtAuthenticationResponse;
import za.co.wiizo.wiizoapi.utils.JWTUtil;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserProfileService implements UserDetailsService {
    @Autowired private UserProfileRepository userProfileRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private JWTUtil jwtUtil;

//    @Autowired
//    private JavaMailSender javaMailSender;

    public UserProfile updateProfile(UserProfile userProfile) {
        UserProfile existingUser = userProfileRepository.findByEmail(userProfile.getEmail());
        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            throw new UsernameNotFoundException("User not found with username: " + userProfile.getEmail());
        }
        // Update user fields
        existingUser.setFirstName(userProfile.getFirstName());
        existingUser.setLastName(userProfile.getLastName());
        existingUser.setEmail(userProfile.getEmail());
        existingUser.setPhoto(userProfile.getPhoto());
        existingUser.setActive(userProfile.isActive());

        // Encrypt updated user password before storing in database
        if (userProfile.getPassword() != null && !userProfile.getPassword().isEmpty()) {
            existingUser.setPassword(new BCryptPasswordEncoder().encode(userProfile.getPassword()));
        }

        return userProfileRepository.save(userProfile);
    }

    public UserProfile activateProfile(UserProfile userProfile) {
        UserProfile existingUser = userProfileRepository.findByEmail(userProfile.getEmail());
        if (existingUser.equals(null)) {
            throw new UsernameNotFoundException("User not found with username: " + userProfile.getEmail());
        }
        existingUser.setActive(userProfile.isActive());

        return userProfileRepository.save(userProfile);
    }

    public UserProfile getProfile(Long userId) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(userId);
        return userProfile.orElse(null);
    }

    public UserProfile getUserByEmail(String email) {
        UserProfile userProfile = userProfileRepository.findByEmail(email);
        return userProfile;
    }

    public VerificationCode getUserVerificationByEmail(UserProfile user) {
        VerificationCode verification = verificationCodeRepository.findByUserProfile(user);
        return verification;
    }

    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public void deleteProfile(Long userId) {
        userProfileRepository.deleteById(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserProfile userProfile = userProfileRepository.findByEmail(email);
        if (userProfile.equals(null)) {
            throw new UsernameNotFoundException("User not found with username: " + email);
        }
        return new org.springframework.security.core.userdetails.User(userProfile.getEmail(), userProfile.getPassword(),
                new ArrayList<>());
    }

    public UserProfile createProfile(UserProfile userProfile) {

        // Generate verification code (you can use a library to generate random codes)
        String verificationCodeValue = generateVerificationCode();

        VerificationCode verificationCode = new VerificationCode();

        // Validate user input and ensure that the user does not already exist
        if (userProfile.getEmail() == null || userProfile.getPassword() == null || userProfile.getEmail() == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }

        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            throw new IllegalArgumentException("User with username " + userProfile.getEmail() + " already exists");
        }


        // Set default user role to ROLE_USER
        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        Set<Role> role =new HashSet<>();
        role.add(userRole);
        userProfile.setRoles(role);
        roleRepository.save(userRole);


        // Encrypt user password before storing in database
        userProfile.setPassword(bCryptPasswordEncoder.encode(userProfile.getPassword()));

        verificationCode.setUserProfile(userProfile);
        verificationCode.setCode(verificationCodeValue);
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(24);
        verificationCode.setExpirationTime(expirationTime);

        userProfile.setVerificationCode(verificationCode);
        userProfile.setActive(false);

        return userProfileRepository.save(userProfile);

    }

    public void saveUser(UserProfile user){
        userProfileRepository.save(user);
    }

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = loadUserByUsername(loginRequest.getEmail());
            UserProfile userProfile = userProfileRepository.findByEmail(userDetails.getUsername());
            userProfile.setPassword(null);
            String jwt = jwtUtil.generateToken(userDetails);
            JwtAuthenticationResponse token = new JwtAuthenticationResponse(jwt, "Bearer", userProfile);
            return token;
        } catch (AuthenticationException e) {
            // Handle authentication failure (e.g., invalid credentials)
            throw new BadCredentialsException("Invalid email or password");
        }
    }







public UserProfile getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserProfile profile = userProfileRepository.findByEmail(userDetails.getUsername());
        return profile;
    }

    public UserProfile verifyUserByCode(String verificationCode) {
        VerificationCode verificationCodeEntity = verificationCodeRepository.findByCode(verificationCode);
        if (verificationCodeEntity != null) {
            LocalDateTime now = LocalDateTime.now();
            if (verificationCodeEntity.getExpirationTime().isAfter(now)) {
                return verificationCodeEntity.getUserProfile();
            }
        }
        return null;
    }
    public UserProfile getUserProfile(String email) {
        return  userProfileRepository.findByEmail(email);
    }

    public String generateVerificationCode() {
        // Generate a random UUID
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a string and remove hyphens to get a 32-character string
        String uuidString = uuid.toString().replaceAll("-", "");
        // Take the first 20 characters to get a 20-character verification code
        String verificationCode = uuidString.substring(0, 20);

        return verificationCode;
    }
}
