// authConfig.js
export const authConfig = {
    clientId: 'oauth2-pkce-demo', // Must match 'Client ID' in Keycloak
    // The 'issuer' is often preferred over individual endpoints
    issuer: 'http://localhost:8081/realms/oauth2-demos',
    authorizationEndpoint: 'http://localhost:8081/realms/oauth2-demos/protocol/openid-connect/auth',
    tokenEndpoint: 'http://localhost:8081/realms/oauth2-demos/protocol/openid-connect/token',
    redirectUri: 'http://localhost:5173', // Must match 'Valid Redirect URIs' in Keycloak
};