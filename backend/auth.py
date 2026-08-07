"""
Authentication utilities for SoulTalk backend.

Provides password hashing/verification and JWT access/refresh token
issuance + validation. This module is intentionally self-contained
(no dependency on other backend modules) so it can be imported early
in main.py without circular imports.

Contract (matches existing call sites in main.py):
- hash_password(password: str) -> str
- verify_password(plain_password: str, hashed_password: str) -> bool
- create_access_token(data: dict) -> str
- create_refresh_token(data: dict) -> str      # embeds {"type": "refresh"}
- verify_token(...) -> dict                     # FastAPI dependency, returns decoded payload
- SECRET_KEY, ALGORITHM                         # re-used directly by /auth/refresh in main.py
"""

import os
from datetime import datetime, timedelta, timezone

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt
from passlib.context import CryptContext

# --- Configuration -----------------------------------------------------
# Read from environment (see .env.example: SECRET_KEY, ALGORITHM).
# Fallback values are for local/dev only — .env.example already warns
# these must be overridden in production, and main.py's config loading
# follows the same pattern used elsewhere in this backend.
SECRET_KEY = os.environ.get("SECRET_KEY", "dev-secret-change-in-production")
ALGORITHM = os.environ.get("ALGORITHM", "HS256")

ACCESS_TOKEN_EXPIRE_MINUTES = int(os.environ.get("ACCESS_TOKEN_EXPIRE_MINUTES", "60"))
REFRESH_TOKEN_EXPIRE_DAYS = int(os.environ.get("REFRESH_TOKEN_EXPIRE_DAYS", "30"))

# --- Password hashing ---------------------------------------------------
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(password: str) -> str:
    """Hash a plaintext password for storage."""
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Check a plaintext password against a stored hash.

    Returns False (never raises) on malformed/missing hashes so a bad
    stored value can't turn into a 500 on login.
    """
    if not hashed_password:
        return False
    try:
        return pwd_context.verify(plain_password, hashed_password)
    except Exception:
        return False


# --- JWT issuance ---------------------------------------------------------
def _create_token(data: dict, token_type: str, expires_delta: timedelta) -> str:
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + expires_delta
    to_encode.update({"exp": expire, "type": token_type})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def create_access_token(data: dict) -> str:
    """Issue a short-lived access token. `data` should include at least
    {"sub": <email>, "id": <user_id>} — matches every call site in main.py.
    """
    return _create_token(data, "access", timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))


def create_refresh_token(data: dict) -> str:
    """Issue a long-lived refresh token. Embeds type=refresh, which
    /auth/refresh in main.py explicitly checks for before re-issuing
    credentials — don't remove that field.
    """
    return _create_token(data, "refresh", timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS))


# --- JWT verification (FastAPI dependency) --------------------------------
_bearer_scheme = HTTPBearer(auto_error=True)


def verify_token(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer_scheme),
) -> dict:
    """FastAPI dependency used across main.py as `Depends(verify_token)`.

    Extracts the Bearer token, validates signature + expiry, rejects
    refresh tokens presented as access tokens, and returns the decoded
    payload dict (callers read token_data.get("id") / .get("sub")).
    """
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Session expired or invalid. Please log in again.",
        )

    if payload.get("type") != "access":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token type for this request.",
        )

    if not payload.get("id") or not payload.get("sub"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Malformed authentication token.",
        )

    return payload
