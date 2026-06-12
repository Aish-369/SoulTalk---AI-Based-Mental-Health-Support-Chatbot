import time
import logging
from typing import Dict
from fastapi import HTTPException, Request
from functools import wraps

# Configure logging
logger = logging.getLogger(__name__)

# Rate limiting storage (in-memory for simplicity, use Redis for production)
rate_limit_store: Dict[str, list] = {}

# Rate limiting configuration
RATE_LIMIT_REQUESTS = 100  # requests per window
RATE_LIMIT_WINDOW = 60  # seconds per window

def rate_limit(request: Request, user_id: str = None):
    """Rate limiting middleware to prevent API abuse."""
    client_ip = request.client.host
    key = f"{client_ip}:{user_id}" if user_id else client_ip
    current_time = time.time()
    
    # Clean old entries
    if key in rate_limit_store:
        rate_limit_store[key] = [t for t in rate_limit_store[key] if current_time - t < RATE_LIMIT_WINDOW]
    else:
        rate_limit_store[key] = []
    
    # Check rate limit
    if len(rate_limit_store[key]) >= RATE_LIMIT_REQUESTS:
        logger.warning(f"Rate limit exceeded for {key}")
        raise HTTPException(
            status_code=429,
            detail="Too many requests. Please try again later."
        )
    
    # Add current request
    rate_limit_store[key].append(current_time)
    logger.debug(f"Rate limit check passed for {key}: {len(rate_limit_store[key])}/{RATE_LIMIT_REQUESTS}")

def secure_error_handler(func):
    """Decorator to handle errors securely without exposing system details."""
    @wraps(func)
    async def wrapper(*args, **kwargs):
        try:
            return await func(*args, **kwargs)
        except HTTPException as e:
            # Re-raise HTTP exceptions as-is (they're already secure)
            raise e
        except Exception as e:
            logger.error(f"Unexpected error in {func.__name__}: {str(e)}", exc_info=True)
            raise HTTPException(
                status_code=500,
                detail="Something went wrong. Please try again."
            )
    return wrapper

def sanitize_log_data(data: str, max_length: int = 50) -> str:
    """Sanitize sensitive data before logging."""
    if not data:
        return ""
    return data[:max_length] + "..." if len(data) > max_length else data

def log_security_event(event_type: str, user_id: str = None, details: str = None):
    """Log security events without exposing sensitive data."""
    log_message = f"Security Event: {event_type}"
    if user_id:
        log_message += f" | User: {user_id}"
    if details:
        log_message += f" | Details: {sanitize_log_data(details)}"
    
    if event_type in ["FAILED_LOGIN", "INVALID_TOKEN", "RATE_LIMIT_EXCEEDED"]:
        logger.warning(log_message)
    else:
        logger.info(log_message)
