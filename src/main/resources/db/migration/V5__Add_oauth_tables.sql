-- =============================================
-- OAuth 2.0 Tables for MCP Authentication
-- =============================================

-- OAuth Clients (registered applications like ChatGPT Desktop)
CREATE TABLE oauth_client (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(100) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    redirect_uris TEXT NOT NULL,
    scopes VARCHAR(255) DEFAULT 'mcp:read mcp:write',
    is_confidential BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- OAuth Authorizations (user has granted access to a client)
CREATE TABLE oauth_authorization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES oauth_client(id) ON DELETE CASCADE,
    scopes VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, client_id)
);

-- OAuth Authorization Codes (temporary, for code exchange)
CREATE TABLE oauth_authorization_code (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code_hash VARCHAR(64) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES oauth_client(id) ON DELETE CASCADE,
    redirect_uri VARCHAR(500) NOT NULL,
    scopes VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- OAuth Refresh Tokens (long-lived tokens for getting new access tokens)
CREATE TABLE oauth_refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(64) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES oauth_client(id) ON DELETE CASCADE,
    scopes VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_oauth_client_client_id ON oauth_client(client_id);
CREATE INDEX idx_oauth_authorization_user ON oauth_authorization(user_id);
CREATE INDEX idx_oauth_authorization_client ON oauth_authorization(client_id);
CREATE INDEX idx_oauth_auth_code_hash ON oauth_authorization_code(code_hash);
CREATE INDEX idx_oauth_auth_code_expires ON oauth_authorization_code(expires_at);
CREATE INDEX idx_oauth_refresh_token_hash ON oauth_refresh_token(token_hash);
CREATE INDEX idx_oauth_refresh_token_user ON oauth_refresh_token(user_id);
CREATE INDEX idx_oauth_refresh_token_expires ON oauth_refresh_token(expires_at);
