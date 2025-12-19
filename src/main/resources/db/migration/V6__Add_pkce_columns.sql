-- Add PKCE (Proof Key for Code Exchange) columns to oauth_authorization_code
-- Required by MCP spec for secure OAuth flow with ChatGPT and other clients

ALTER TABLE oauth_authorization_code
ADD COLUMN code_challenge VARCHAR(128),
ADD COLUMN code_challenge_method VARCHAR(10);

COMMENT ON COLUMN oauth_authorization_code.code_challenge IS 'PKCE code challenge (S256 hash of code_verifier)';
COMMENT ON COLUMN oauth_authorization_code.code_challenge_method IS 'PKCE challenge method (S256 or plain)';
