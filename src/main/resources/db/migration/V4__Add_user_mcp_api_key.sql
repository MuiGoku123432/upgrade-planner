-- Add MCP API key fields to app_user table for user-specific MCP authentication
ALTER TABLE app_user ADD COLUMN mcp_api_key VARCHAR(64) UNIQUE;
ALTER TABLE app_user ADD COLUMN mcp_api_key_created_at TIMESTAMP WITH TIME ZONE;

-- Create partial index for API key lookups (only on non-null keys)
CREATE INDEX idx_app_user_mcp_api_key ON app_user(mcp_api_key) WHERE mcp_api_key IS NOT NULL;
