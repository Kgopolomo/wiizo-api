package za.co.wiizo.wiizoapi.response;


import za.co.wiizo.wiizoapi.entity.UserProfile;


public class JwtAuthenticationResponse {

    private String accessToken;
    private String tokenType;
    private UserProfile userProfile;

    public JwtAuthenticationResponse() {
    }

    public JwtAuthenticationResponse(String accessToken, String tokenType, UserProfile userProfile) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.userProfile = userProfile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
