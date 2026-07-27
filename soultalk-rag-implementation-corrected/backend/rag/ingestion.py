"""
Ingestion pipeline for the "exemplar" retrieval track.

Loads conversations.json (single-turn user/companion pairs) and
conversation_chains.json (multi-turn chains), cleans and normalizes them,
and produces a flat list of chunk documents ready for embedding.

Chunking strategy (deliberately not character-count based):
- conversations.json entries are already atomic single-turn exchanges -
  each one is one chunk.
- conversation_chains.json entries are multi-turn - chunked with a sliding
  window over turns (see rag.config.CHAIN_WINDOW_SIZE/STRIDE) so each chunk
  keeps a few turns of conversational context together instead of being
  split turn-by-turn or dumped as one giant chain.

Note on metadata: the source dataset carries no timestamps (it's a static,
bundled training set, not live user data), so no timestamp field is added -
inventing one would misrepresent the data.
"""
import hashlib
import json
import logging
from typing import Dict, List

from . import config

logger = logging.getLogger(__name__)


class DatasetLoadError(Exception):
    """Raised when the source dataset files can't be read or parsed."""


def _normalize_category(raw_category: str) -> str:
    category = (raw_category or "uncategorized").strip().lower().replace(" ", "_")
    return config.CATEGORY_ALIASES.get(category, category)


def _content_hash(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def _load_json(path: str) -> list:
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError as e:
        raise DatasetLoadError(f"Dataset file not found: {path}") from e
    except json.JSONDecodeError as e:
        raise DatasetLoadError(f"Dataset file is not valid JSON: {path} ({e})") from e


def load_single_turn_exemplars(path: str = None) -> List[Dict]:
    """Load conversations.json into one chunk per exchange."""
    path = path or config.CONVERSATIONS_PATH
    raw_items = _load_json(path)
    if not isinstance(raw_items, list):
        raise DatasetLoadError(f"Expected a JSON array in {path}, got {type(raw_items)}")

    seen_hashes = set()
    documents = []
    skipped_malformed = 0
    skipped_duplicate = 0

    for i, item in enumerate(raw_items):
        user_text = (item.get("user") or "").strip()
        bot_text = (item.get("bot") or "").strip()
        if not user_text or not bot_text:
            skipped_malformed += 1
            continue

        content = f"User: {user_text}\nCompanion: {bot_text}"
        content_hash = _content_hash(content.lower())
        if content_hash in seen_hashes:
            skipped_duplicate += 1
            continue
        seen_hashes.add(content_hash)

        documents.append({
            "id": f"conv_{i}",
            "type": "exemplar",
            "content": content,
            "embedding_text": user_text,  # embed the user side - that's what queries match against
            "source": "conversations.json",
            "category": _normalize_category(item.get("category")),
            "emotion": (item.get("emotion") or "unknown").strip().lower(),
            "content_hash": content_hash,
        })

    logger.info(
        "Loaded %d single-turn exemplars from %s (skipped %d malformed, %d duplicate)",
        len(documents), path, skipped_malformed, skipped_duplicate,
    )
    return documents


def load_chain_exemplars(path: str = None) -> List[Dict]:
    """Load conversation_chains.json into sliding-window multi-turn chunks."""
    path = path or config.CONVERSATION_CHAINS_PATH
    raw_chains = _load_json(path)
    if not isinstance(raw_chains, list):
        raise DatasetLoadError(f"Expected a JSON array in {path}, got {type(raw_chains)}")

    window = max(1, config.CHAIN_WINDOW_SIZE)
    stride = max(1, config.CHAIN_WINDOW_STRIDE)

    seen_hashes = set()
    documents = []
    skipped_malformed = 0
    skipped_duplicate = 0

    for chain in raw_chains:
        chain_id = chain.get("chain_id", "unknown")
        topic = _normalize_category(chain.get("topic"))
        turns = chain.get("turns") or []
        clean_turns = [
            t for t in turns
            if (t.get("user") or "").strip() and (t.get("bot") or "").strip()
        ]
        if not clean_turns:
            skipped_malformed += 1
            continue

        for start in range(0, len(clean_turns), stride):
            window_turns = clean_turns[start:start + window]
            if not window_turns:
                continue

            lines = []
            for t in window_turns:
                lines.append(f"User: {t['user'].strip()}")
                lines.append(f"Companion: {t['bot'].strip()}")
            content = "\n".join(lines)
            content_hash = _content_hash(content.lower())
            if content_hash in seen_hashes:
                skipped_duplicate += 1
                continue
            seen_hashes.add(content_hash)

            # Embed on the concatenation of user turns in this window - that's
            # the part a new incoming user message should semantically match.
            embedding_text = " ".join(t["user"].strip() for t in window_turns)

            end_idx = start + len(window_turns) - 1
            documents.append({
                "id": f"chain_{chain_id}_{start}_{end_idx}",
                "type": "exemplar",
                "content": content,
                "embedding_text": embedding_text,
                "source": "conversation_chains.json",
                "category": topic,
                "emotion": "unknown",
                "content_hash": content_hash,
            })

            if start + window >= len(clean_turns):
                break

    logger.info(
        "Loaded %d chain exemplar chunks from %s (skipped %d malformed chains, %d duplicate chunks)",
        len(documents), path, skipped_malformed, skipped_duplicate,
    )
    return documents


def load_all_exemplar_documents() -> List[Dict]:
    """Load and combine both exemplar sources. Raises DatasetLoadError on failure."""
    docs = load_single_turn_exemplars()
    docs.extend(load_chain_exemplars())
    return docs
