"""
Top-level retrieval entry point used by the chat flow.

retrieve(query_text, category_hint) never raises - if embeddings are
unavailable, the index hasn't been built yet, or anything else goes wrong,
it logs and returns empty results so the chatbot falls back to generating
without retrieved context instead of failing the whole request. RAG is an
enhancement here, not a dependency the chat endpoint should crash without.
"""
import logging
from typing import Dict, List, Optional

from . import config
from . import embeddings
from .vector_store import VectorStore

logger = logging.getLogger(__name__)

_exemplar_store: Optional[VectorStore] = None
_knowledge_store: Optional[VectorStore] = None


def _get_stores():
    global _exemplar_store, _knowledge_store
    if _exemplar_store is None:
        _exemplar_store = VectorStore(config.EXEMPLAR_INDEX_PATH)
        _exemplar_store.load()
    if _knowledge_store is None:
        _knowledge_store = VectorStore(config.KNOWLEDGE_INDEX_PATH)
        _knowledge_store.load()
    return _exemplar_store, _knowledge_store


def reload_stores() -> None:
    """Force both stores to reload from disk (e.g. after build_index.py runs)."""
    global _exemplar_store, _knowledge_store
    _exemplar_store = None
    _knowledge_store = None
    _get_stores()


def retrieve(query_text: str, category_hint: Optional[str] = None) -> Dict[str, List[Dict]]:
    """
    Returns {"exemplars": [...], "knowledge": [...]}, each possibly empty.
    Never raises.
    """
    empty_result = {"exemplars": [], "knowledge": []}

    if not query_text or not query_text.strip():
        return empty_result

    try:
        exemplar_store, knowledge_store = _get_stores()
    except Exception as e:
        logger.error("RAG: failed to load vector stores, skipping retrieval: %s", e)
        return empty_result

    if len(exemplar_store) == 0 and len(knowledge_store) == 0:
        logger.info("RAG: indexes are empty (has build_index.py been run?) - skipping retrieval.")
        return empty_result

    try:
        query_embedding = embeddings.embed_query(query_text)
    except Exception as e:
        logger.error("RAG: query embedding failed, skipping retrieval: %s", e)
        return empty_result

    if query_embedding is None:
        logger.warning("RAG: no embedding returned for query, skipping retrieval.")
        return empty_result

    try:
        exemplars = exemplar_store.search(
            query_embedding,
            top_k=config.EXEMPLAR_TOP_K,
            similarity_threshold=config.EXEMPLAR_SIMILARITY_THRESHOLD,
            category_filter=category_hint,
        )
    except Exception as e:
        logger.error("RAG: exemplar search failed: %s", e)
        exemplars = []

    try:
        knowledge = knowledge_store.search(
            query_embedding,
            top_k=config.KNOWLEDGE_TOP_K,
            similarity_threshold=config.KNOWLEDGE_SIMILARITY_THRESHOLD,
            category_filter=category_hint,
        )
    except Exception as e:
        logger.error("RAG: knowledge search failed: %s", e)
        knowledge = []

    return {"exemplars": exemplars, "knowledge": knowledge}
