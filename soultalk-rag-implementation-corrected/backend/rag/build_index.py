"""
Builds (or incrementally updates) both retrieval indexes:
  - exemplar_index.json   <- conversations.json + conversation_chains.json
  - knowledge_index.json  <- knowledge_base.py authored documents

Run manually whenever the source dataset or knowledge base changes:

    python -m backend.rag.build_index

Incremental behavior: documents whose content_hash already exists in the
on-disk index are NOT re-embedded (saves local compute - embedding is a
local model forward pass, not an API call); only new or changed content
triggers embedding. Deleted source documents are dropped from the rebuilt
index.
"""
import hashlib
import logging
import sys

from . import config
from . import embeddings
from . import ingestion
from .knowledge_base import get_knowledge_documents
from .vector_store import VectorStore

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)


def _build_one_index(fresh_documents: list, index_path: str, label: str) -> None:
    store = VectorStore(index_path)
    store.load()
    existing_by_hash = {
        doc["content_hash"]: doc for doc in store.documents if doc.get("content_hash")
    }

    to_embed_texts = []
    to_embed_positions = []  # index into fresh_documents
    final_documents = []

    for doc in fresh_documents:
        cached = existing_by_hash.get(doc["content_hash"])
        if cached and cached.get("embedding"):
            new_doc = dict(doc)
            new_doc["embedding"] = cached["embedding"]
            final_documents.append(new_doc)
        else:
            to_embed_positions.append(len(final_documents))
            final_documents.append(dict(doc))  # embedding filled in below
            to_embed_texts.append(doc["embedding_text"])

    if to_embed_texts:
        logger.info("%s: embedding %d new/changed documents (of %d total)",
                    label, len(to_embed_texts), len(fresh_documents))
        new_embeddings = embeddings.embed_texts(to_embed_texts, task_type="RETRIEVAL_DOCUMENT")
        for pos, emb in zip(to_embed_positions, new_embeddings):
            final_documents[pos]["embedding"] = emb
    else:
        logger.info("%s: no new/changed documents - reused %d cached embeddings",
                    label, len(final_documents))

    # Drop any document that ended up without an embedding (API failure) so
    # the store never silently returns garbage on search.
    kept = [d for d in final_documents if d.get("embedding")]
    dropped = len(final_documents) - len(kept)
    if dropped:
        logger.warning("%s: dropping %d documents that failed to embed", label, dropped)

    for d in kept:
        d.pop("embedding_text", None)  # only needed during embedding, not at query time

    store.replace_all(kept)
    store.save()
    logger.info("%s: index saved to %s (%d documents)", label, index_path, len(kept))


def main() -> int:
    logger.info("Building RAG indexes with local embedding model: %s", config.EMBEDDING_MODEL)
    try:
        exemplar_docs = ingestion.load_all_exemplar_documents()
    except ingestion.DatasetLoadError as e:
        logger.error("Could not load exemplar dataset, aborting exemplar index build: %s", e)
        exemplar_docs = None

    if exemplar_docs is not None:
        _build_one_index(exemplar_docs, config.EXEMPLAR_INDEX_PATH, "exemplar")

    knowledge_docs = get_knowledge_documents()
    for doc in knowledge_docs:
        doc["type"] = "knowledge"
        doc["embedding_text"] = doc["content"]
        doc.setdefault("emotion", "unknown")
        doc.setdefault("source", "knowledge_base.py")
        doc["content_hash"] = hashlib.sha256(doc["content"].encode("utf-8")).hexdigest()
    _build_one_index(knowledge_docs, config.KNOWLEDGE_INDEX_PATH, "knowledge")

    return 0


if __name__ == "__main__":
    sys.exit(main())
