"""
Central configuration for the RAG subsystem. Everything is overridable via
environment variables so behavior can be tuned without touching code.
"""
import os

# --- Embedding provider ---
# Local sentence-embedding model (sentence-transformers) - no external API,
# no API key. "paraphrase-multilingual-MiniLM-L12-v2" is the default: the
# dataset is a Roman-Marathi/English mix, and this model has broader
# multilingual subword coverage than an English-only MiniLM model, which
# matters for matching transliterated Marathi queries against the exemplar
# dataset. Swap via RAG_EMBEDDING_MODEL if a different local model is
# preferred (any sentence-transformers-compatible model name or local path).
EMBEDDING_MODEL = os.getenv("RAG_EMBEDDING_MODEL", "paraphrase-multilingual-MiniLM-L12-v2")
EMBEDDING_DEVICE = os.getenv("RAG_EMBEDDING_DEVICE", "cpu")
EMBEDDING_BATCH_SIZE = int(os.getenv("RAG_EMBEDDING_BATCH_SIZE", "32"))

# --- Retrieval ---
EXEMPLAR_TOP_K = int(os.getenv("RAG_EXEMPLAR_TOP_K", "3"))
KNOWLEDGE_TOP_K = int(os.getenv("RAG_KNOWLEDGE_TOP_K", "2"))
EXEMPLAR_SIMILARITY_THRESHOLD = float(os.getenv("RAG_EXEMPLAR_THRESHOLD", "0.55"))
KNOWLEDGE_SIMILARITY_THRESHOLD = float(os.getenv("RAG_KNOWLEDGE_THRESHOLD", "0.50"))

# Hard caps so a bad query can never blow up the prompt.
MAX_CONTEXT_CHARS_PER_DOC = int(os.getenv("RAG_MAX_CONTEXT_CHARS_PER_DOC", "300"))
MAX_TOTAL_CONTEXT_CHARS = int(os.getenv("RAG_MAX_TOTAL_CONTEXT_CHARS", "2000"))

# --- Storage paths (in-process index persisted to disk, no external DB) ---
_RAG_DIR = os.path.dirname(os.path.abspath(__file__))
INDEX_DIR = os.getenv("RAG_INDEX_DIR", os.path.join(_RAG_DIR, "index"))
EXEMPLAR_INDEX_PATH = os.path.join(INDEX_DIR, "exemplar_index.json")
KNOWLEDGE_INDEX_PATH = os.path.join(INDEX_DIR, "knowledge_index.json")

# --- Dataset source paths ---
_DATASET_DIR = os.path.join(os.path.dirname(_RAG_DIR), "dataset")
CONVERSATIONS_PATH = os.path.join(_DATASET_DIR, "conversations.json")
CONVERSATION_CHAINS_PATH = os.path.join(_DATASET_DIR, "conversation_chains.json")

# Sliding-window size when chunking multi-turn chains (in turns), so chunks
# carry conversational context instead of being sliced by character count.
CHAIN_WINDOW_SIZE = int(os.getenv("RAG_CHAIN_WINDOW_SIZE", "3"))
CHAIN_WINDOW_STRIDE = int(os.getenv("RAG_CHAIN_WINDOW_STRIDE", "2"))

# Category name normalization - the source dataset has near-duplicate labels
# (e.g. "onesided_love" / "one_sided_love") from being generated in batches.
CATEGORY_ALIASES = {
    "one_sided_love": "onesided_love",
    "anger_issue": "anger",
    "past_regrets": "past_regret",
    "decision_making_confusion": "decision_making",
    "daily_conversation": "daily_chat",
}
