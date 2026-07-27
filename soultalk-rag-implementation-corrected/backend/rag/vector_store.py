"""
A small in-process vector store, persisted to a JSON file on disk.

Deliberately not pgvector / a hosted vector DB: the corpus here is a few
thousand short documents bundled with the repo, not per-user live data, so
an external vector database is unneeded infrastructure. Cosine similarity
over an in-memory numpy matrix is fast enough at this scale and loads once
at process startup.

Each index file is a JSON object: {"documents": [...]} where each entry has
the document's own fields (id, type, content, category, ...) plus an
"embedding" list. Re-running the index build skips embedding any document
whose "content_hash" already matches what's on disk.
"""
import json
import logging
import os
from typing import Dict, List, Optional

import numpy as np

logger = logging.getLogger(__name__)


class VectorStore:
    def __init__(self, path: str):
        self.path = path
        self.documents: List[Dict] = []
        self._matrix: Optional[np.ndarray] = None
        self._norms: Optional[np.ndarray] = None

    def load(self) -> bool:
        """Load a persisted index from disk. Returns False if none exists yet."""
        if not os.path.exists(self.path):
            logger.info("No existing index at %s - starting empty.", self.path)
            self.documents = []
            self._rebuild_matrix()
            return False
        try:
            with open(self.path, "r", encoding="utf-8") as f:
                data = json.load(f)
            self.documents = data.get("documents", [])
        except (json.JSONDecodeError, OSError) as e:
            logger.error("Failed to load index at %s (%s) - starting empty.", self.path, e)
            self.documents = []
        self._rebuild_matrix()
        return True

    def save(self) -> None:
        os.makedirs(os.path.dirname(self.path), exist_ok=True)
        tmp_path = self.path + ".tmp"
        with open(tmp_path, "w", encoding="utf-8") as f:
            json.dump({"documents": self.documents}, f, ensure_ascii=False)
        os.replace(tmp_path, self.path)  # atomic on POSIX - avoids a half-written index

    def existing_hashes(self) -> set:
        return {doc.get("content_hash") for doc in self.documents if doc.get("content_hash")}

    def replace_all(self, documents: List[Dict]) -> None:
        """Replace the whole document set (used after a full ingestion pass)."""
        self.documents = documents
        self._rebuild_matrix()

    def _rebuild_matrix(self) -> None:
        embeddings = [doc.get("embedding") for doc in self.documents]
        if not embeddings or any(e is None for e in embeddings):
            self._matrix = None
            self._norms = None
            return
        self._matrix = np.array(embeddings, dtype=np.float32)
        self._norms = np.linalg.norm(self._matrix, axis=1)
        self._norms[self._norms == 0] = 1e-8  # guard against divide-by-zero

    def search(
        self,
        query_embedding: List[float],
        top_k: int,
        similarity_threshold: float,
        category_filter: Optional[str] = None,
    ) -> List[Dict]:
        """Return up to top_k documents above the similarity threshold, best first."""
        if self._matrix is None or len(self.documents) == 0:
            return []

        query_vec = np.array(query_embedding, dtype=np.float32)
        query_norm = np.linalg.norm(query_vec)
        if query_norm == 0:
            return []

        similarities = (self._matrix @ query_vec) / (self._norms * query_norm)

        candidate_indices = list(range(len(self.documents)))
        if category_filter:
            candidate_indices = [
                i for i in candidate_indices
                if self.documents[i].get("category") == category_filter
            ]
            if not candidate_indices:
                candidate_indices = list(range(len(self.documents)))  # fall back to unfiltered

        scored = sorted(
            ((float(similarities[i]), i) for i in candidate_indices),
            key=lambda pair: pair[0],
            reverse=True,
        )

        results = []
        for score, i in scored:
            if score < similarity_threshold:
                break
            doc = dict(self.documents[i])
            doc.pop("embedding", None)  # never send raw vectors back out
            doc["similarity"] = score
            results.append(doc)
            if len(results) >= top_k:
                break
        return results

    def __len__(self) -> int:
        return len(self.documents)
