package br.com.oficinatrack.auth.api.dto;

public class TokenResponse {
    private String token;
    private Long expiraEm;

    public TokenResponse(String token, Long expiraEm) {
        this.token = token;
        this.expiraEm = expiraEm;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(Long expiraEm) {
        this.expiraEm = expiraEm;
    }
}