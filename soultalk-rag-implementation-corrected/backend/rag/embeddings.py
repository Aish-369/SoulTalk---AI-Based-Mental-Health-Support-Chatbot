"""
Local embedding generation via sentence-transformers.

No external API, no API key, no per-request network call. The model weights
are fetched once (standard huggingface_hub cache, ~/.cache/huggingface by
default) the first time this process runs on a machine; every run after
that loads from local disk. This is the only network activity involved, and
it happens at model-load time, not per query/document.

The same model instance is used for both documents (ingestion) and queries
(retrieval), so both sides land in the same vector space. The model is
loaded lazily on first use and cached for the lifetime of the process (see
_get_model()) so requests don't pay a multi-second model-load cost each time.
"""
import logging
import threading
from typing import List, Optional

from . import config

logger = logging.getLogger(__name__)


class EmbeddingError(Exception):
    """Raised when the local embedding model can't be loaded or run."""


_model = None
_model_lock = threading.Lock()
_model_load_failed = False  # sticky - avoid re-attempting a broken/missing model on every call


def _load_model():
    """Import sentence-transformers and construct the configured model. Raises EmbeddingError."""
    try:
        from sentence_transformers import SentenceTransformer
    except ImportError as e:
        raise EmbeddingError(
            "sentence-transformers is not installed. Run: "
            "pip install sentence-transformers"
        ) from e

    try:
        logger.info(
            "Loading local embedding model '%s' (device=%s) - this only happens once per process...",
            config.EMBEDDING_MODEL, config.EMBEDDING_DEVICE,
        )
        model = SentenceTransformer(config.EMBEDDING_MODEL, device=config.EMBEDDING_DEVICE)
        logger.info("Local embedding model '%s' loaded successfully.", config.EMBEDDING_MODEL)
        return model
    except Exception as e:
        raise EmbeddingError(
            f"Failed to load local embedding model '{config.EMBEDDING_MODEL}': {e}"
        ) from e


def _get_model():
    """Lazily load and cache the embedding model. Thread-safe; loads at most once per process."""
    global _model, _model_load_failed
    if _model is not None:
        return _model
    if _model_load_failed:
        raise EmbeddingError(
            f"Local embedding model '{config.EMBEDDING_MODEL}' previously failed to load - "
            "not retrying within this process."
        )
    with _model_lock:
        if _model is None:  # re-check inside the lock
            try:
                _model = _load_model()
            except EmbeddingError:
                _model_load_failed = True
                raise
    return _model


def _encode(texts: List[str], batch_size: int) -> List[List[float]]:
    model = _get_model()
    try:
        vectors = model.encode(
            texts,
            batch_size=batch_size,
            show_progress_bar=False,
            normalize_embeddings=True,  # unit vectors - cosine similarity downstream is well-behaved
            convert_to_numpy=True,
        )
    except Exception as e:
        raise EmbeddingError(f"Local embedding model failed to encode text: {e}") from e
    return [vec.tolist() for vec in vectors]


def embed_documents(texts: List[str], batch_size: Optional[int] = None) -> List[Optional[List[float]]]:
    """
    Embed a batch of documents for ingestion.

    Returns a list the same length as `texts`. On total failure (model
    unavailable), every entry is None so callers (build_index.py) skip
    those documents instead of crashing the whole index build.
    """
    if not texts:
        return []
    batch_size = batch_size or config.EMBEDDING_BATCH_SIZE
    try:
        return _encode(list(texts), batch_size)
    except EmbeddingError as e:
        logger.error("Local embedding failed for a batch of %d document(s): %s", len(texts), e)
        return [None] * len(texts)


def embed_query(text: str) -> Optional[List[float]]:
    """Embed a single user query. Returns None on failure (caller must handle)."""
    if not text or not text.strip():
        return None
    try:
        return _encode([text], batch_size=1)[0]
    except EmbeddingError as e:
        logger.error("Local embedding failed for query: %s", e)
        return None


def embed_texts(
    texts: List[str],
    task_type: Optional[str] = None,
    batch_size: Optional[int] = None,
) -> List[Optional[List[float]]]:
    """
    Backward-compatible alias for embed_documents(), kept so build_index.py
    doesn't need to change. `task_type` is accepted and ignored - it was a
    Gemini-embedding-API-specific parameter (RETRIEVAL_DOCUMENT vs
    RETRIEVAL_QUERY); the local model encodes documents and queries the
    same way, which is why embed_documents/embed_query share _encode().
    """
    return embed_documents(texts, batch_size=batch_size)
