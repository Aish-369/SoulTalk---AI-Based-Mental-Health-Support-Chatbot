import os
import logging
import time
from typing import Generator
from sqlalchemy import create_engine, pool, text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.exc import OperationalError, DatabaseError
from dotenv import load_dotenv

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load SoulTalk environment parameters
load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), "../.env"))

DATABASE_URL = os.getenv("DATABASE_URL")

# Production-grade resilient database engine creation for Neon PostgreSQL
# Uses SQLite as a local fallback for offline-first local testing environments
if not DATABASE_URL:
    logger.warning("DATABASE_URL not found in environment variables. Using SQLite fallback.")
    DATABASE_URL = "sqlite:///./local_soultalk.db"

# Create proper engine arguments based on DB driver type
if DATABASE_URL.startswith("sqlite"):
    engine = create_engine(
        DATABASE_URL,
        connect_args={"check_same_thread": False},
        echo=False
    )
    logger.info("Using SQLite database engine")
elif DATABASE_URL.startswith("postgresql"):
    # Neon PostgreSQL with connection pooling and SSL enforcement
    if "sslmode" not in DATABASE_URL:
        DATABASE_URL = f"{DATABASE_URL}&sslmode=require"
    
    engine = create_engine(
        DATABASE_URL,
        poolclass=pool.QueuePool,
        pool_size=10,
        max_overflow=20,
        pool_pre_ping=True,  # Verify connections before using
        pool_recycle=3600,  # Recycle connections after 1 hour
        echo=False
    )
    logger.info("Using Neon PostgreSQL with connection pooling and SSL")
else:
    raise ValueError(f"Unsupported database URL scheme: {DATABASE_URL}")

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

def test_connection(max_retries: int = 3) -> bool:
    """Test database connection with retry mechanism."""
    for attempt in range(max_retries):
        try:
            with engine.connect() as connection:
                result = connection.execute(text("SELECT 1"))
                logger.info(f"Database connection test successful (attempt {attempt + 1}/{max_retries})")
                return True
        except OperationalError as e:
            logger.warning(f"Database connection failed (attempt {attempt + 1}/{max_retries}): {e}")
            if attempt < max_retries - 1:
                time.sleep(2 ** attempt)  # Exponential backoff
            else:
                logger.error("All database connection attempts failed")
                return False
    return False

def get_db() -> Generator[Session, None, None]:
    """Get database session with error handling and retry."""
    max_retries = 3
    for attempt in range(max_retries):
        try:
            db = SessionLocal()
            yield db
            db.commit()
            return
        except (OperationalError, DatabaseError) as e:
            logger.error(f"Database error (attempt {attempt + 1}/{max_retries}): {e}")
            if attempt < max_retries - 1:
                time.sleep(2 ** attempt)  # Exponential backoff
                continue
            else:
                logger.error("All database retry attempts failed")
                raise
        finally:
            db.close()
