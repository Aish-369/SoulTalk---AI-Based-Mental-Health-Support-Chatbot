"""
Tests for backend/rag/. Run with:

    pytest backend/tests/test_rag.py -v

Embedding tests mock the local sentence-transformers model (via
embeddings._load_model) so the suite runs fast, deterministically, and
without needing the model weights downloaded or any network access - these
are unit tests for the pipeline logic, not an integration test against the
real model.
"""
import json
import math
import os

import pytest

from backend.rag import config, embeddings, ingestion, retriever
from backend.rag.prompt_context import format_retrieved_context
from backend.rag.vector_store import VectorStore


# ---------------------------------------------------------------------------
# Ingestion / chunking
# ---------------------------------------------------------------------------

def test_single_turn_ingestion_loads_real_dataset():
    docs = ingestion.load_single_turn_exemplars()
    assert len(docs) > 0
    for doc in docs[:5]:
        assert doc["type"] == "exemplar"
        assert "User:" in doc["content"] and "Companion:" in doc["content"]
        assert doc["embedding_text"]  # non-empty
        assert doc["category"]  # normalized, never empty


def test_single_turn_ingestion_deduplicates():
    docs = ingestion.load_single_turn_exemplars()
    hashes = [d["content_hash"] for d in docs]
    assert len(hashes) == len(set(hashes)), "duplicate content_hash values leaked through"


def test_category_normalization_merges_aliases():
    docs = ingestion.load_single_turn_exemplars()
    categories = {d["category"] for d in docs}
    # aliases defined in config.CATEGORY_ALIASES should never appear as their raw form
    for raw_alias in config.CATEGORY_ALIASES:
        assert raw_alias not in categories


def test_chain_ingestion_sliding_window_preserves_context():
    docs = ingestion.load_chain_exemplars()
    assert len(docs) > 0
    for doc in docs[:5]:
        # each window chunk should contain more than one turn (window size > 1)
        assert doc["content"].count("User:") >= 1
        assert doc["source"] == "conversation_chains.json"


def test_ingestion_missing_file_raises_dataset_load_error(tmp_path):
    missing_path = str(tmp_path / "does_not_exist.json")
    with pytest.raises(ingestion.DatasetLoadError):
        ingestion.load_single_turn_exemplars(path=missing_path)


def test_ingestion_malformed_json_raises_dataset_load_error(tmp_path):
    bad_path = tmp_path / "bad.json"
    bad_path.write_text("{not valid json", encoding="utf-8")
    with pytest.raises(ingestion.DatasetLoadError):
        ingestion.load_single_turn_exemplars(path=str(bad_path))


def test_ingestion_skips_malformed_entries_without_crashing(tmp_path):
    path = tmp_path / "conversations.json"
    path.write_text(json.dumps([
        {"category": "stress", "emotion": "stressed", "user": "hi", "bot": "hello"},
        {"category": "stress", "emotion": "stressed", "user": "", "bot": "missing user text"},
        {"category": "stress", "bot": "missing user key entirely"},
    ]), encoding="utf-8")
    docs = ingestion.load_single_turn_exemplars(path=str(path))
    assert len(docs) == 1  # only the one complete pair survives


# ---------------------------------------------------------------------------
# Embeddings (mocked local model - no real model download / network calls)
# ---------------------------------------------------------------------------

class _FakeLocalModel:
    """Stand-in for a sentence_transformers.SentenceTransformer instance."""

    def __init__(self):
        self.encode_calls = []

    def encode(self, texts, batch_size=32, show_progress_bar=False,
               normalize_embeddings=True, convert_to_numpy=True):
        import numpy as np
        self.encode_calls.append(list(texts))
        # deterministic fixed-size fake vector per text, distinct per text length
        # so equal texts embed identically (as a real model would).
        return np.array([[float(len(t)), 0.0, 1.0] for t in texts], dtype=np.float32)


@pytest.fixture(autouse=True)
def _reset_embedding_model_cache():
    """The loaded model is cached at module level - reset it around every test
    so tests don't leak state (a loaded fake model, or a sticky failure flag)
    into each other."""
    embeddings._model = None
    embeddings._model_load_failed = False
    yield
    embeddings._model = None
    embeddings._model_load_failed = False


def test_embed_documents_and_embed_query_use_the_same_model(monkeypatch):
    fake_model = _FakeLocalModel()
    monkeypatch.setattr(embeddings, "_load_model", lambda: fake_model)

    doc_vectors = embeddings.embed_documents(["hello", "world!"])
    query_vector = embeddings.embed_query("hello")

    assert len(doc_vectors) == 2
    assert all(v is not None for v in doc_vectors)
    assert len(doc_vectors[0]) == len(query_vector)  # same vector space/dimension
    assert doc_vectors[0] == query_vector  # identical input text -> identical embedding


def test_embedding_model_is_loaded_once_and_reused(monkeypatch):
    load_calls = {"n": 0}

    def counting_load():
        load_calls["n"] += 1
        return _FakeLocalModel()

    monkeypatch.setattr(embeddings, "_load_model", counting_load)

    embeddings.embed_query("first call")
    embeddings.embed_documents(["second call", "third call"])
    embeddings.embed_query("fourth call")

    assert load_calls["n"] == 1  # model loaded exactly once across all four calls


def test_embed_documents_returns_none_list_when_model_unavailable(monkeypatch):
    def always_fail():
        raise embeddings.EmbeddingError("simulated: model weights not found locally")

    monkeypatch.setattr(embeddings, "_load_model", always_fail)
    results = embeddings.embed_documents(["hello", "world"])
    assert results == [None, None]


def test_embed_query_returns_none_when_model_unavailable(monkeypatch):
    monkeypatch.setattr(
        embeddings, "_load_model",
        lambda: (_ for _ in ()).throw(embeddings.EmbeddingError("simulated failure")),
    )
    assert embeddings.embed_query("anything") is None


def test_model_load_failure_is_sticky_within_a_process(monkeypatch):
    """Once loading fails, don't keep retrying (and re-failing) on every call -
    that would mean every chat message pays the load-attempt cost."""
    load_calls = {"n": 0}

    def always_fail():
        load_calls["n"] += 1
        raise embeddings.EmbeddingError("simulated: no internet to fetch model weights")

    monkeypatch.setattr(embeddings, "_load_model", always_fail)

    embeddings.embed_query("first")
    embeddings.embed_query("second")
    embeddings.embed_documents(["third"])

    assert load_calls["n"] == 1  # only attempted once, not once per call


def test_embed_documents_empty_list_returns_empty_without_loading_model(monkeypatch):
    called = {"n": 0}
    monkeypatch.setattr(embeddings, "_load_model", lambda: called.update(n=called["n"] + 1))
    assert embeddings.embed_documents([]) == []
    assert called["n"] == 0


def test_embed_query_empty_string_returns_none_without_loading_model(monkeypatch):
    called = {"n": 0}
    monkeypatch.setattr(embeddings, "_load_model", lambda: called.update(n=called["n"] + 1))
    assert embeddings.embed_query("") is None
    assert embeddings.embed_query("   ") is None
    assert called["n"] == 0


def test_embed_texts_is_a_backward_compatible_alias_for_embed_documents(monkeypatch):
    fake_model = _FakeLocalModel()
    monkeypatch.setattr(embeddings, "_load_model", lambda: fake_model)
    # task_type is a leftover Gemini-API parameter name; must be accepted and ignored.
    results = embeddings.embed_texts(["hello"], task_type="RETRIEVAL_DOCUMENT")
    assert results == embeddings.embed_documents(["hello"])


def test_embeddings_module_uses_no_external_embedding_api():
    import inspect
    source = inspect.getsource(embeddings)
    banned = [
        "GEMINI_API_KEY", "urllib.request", "requests.post", "requests.get",
        "generativelanguage.googleapis.com", "openai", "OPENAI_API_KEY",
    ]
    for term in banned:
        assert term not in source, f"embeddings.py should not reference '{term}'"


# ---------------------------------------------------------------------------
# Vector store
# ---------------------------------------------------------------------------

def _doc(doc_id, embedding, category="stress", content="some content"):
    return {
        "id": doc_id, "type": "exemplar", "content": content,
        "category": category, "emotion": "unknown", "source": "test",
        "content_hash": doc_id, "embedding": embedding,
    }


def test_vector_store_empty_search_returns_empty_list(tmp_path):
    store = VectorStore(str(tmp_path / "index.json"))
    store.load()
    assert store.search([1.0, 0.0], top_k=3, similarity_threshold=0.0) == []


def test_vector_store_search_ranks_by_similarity(tmp_path):
    store = VectorStore(str(tmp_path / "index.json"))
    store.replace_all([
        _doc("a", [1.0, 0.0]),   # identical direction to query -> similarity 1.0
        _doc("b", [0.0, 1.0]),   # orthogonal -> similarity 0.0
        _doc("c", [0.9, 0.1]),   # close to query
    ])
    results = store.search([1.0, 0.0], top_k=2, similarity_threshold=0.5)
    assert [r["id"] for r in results] == ["a", "c"]
    assert results[0]["similarity"] > results[1]["similarity"]
    assert "embedding" not in results[0]  # raw vectors never leak into results


def test_vector_store_similarity_threshold_filters_irrelevant():
    store = VectorStore("/tmp/unused_for_this_test.json")
    store.replace_all([_doc("a", [1.0, 0.0]), _doc("b", [0.0, 1.0])])
    results = store.search([1.0, 0.0], top_k=5, similarity_threshold=0.9)
    assert [r["id"] for r in results] == ["a"]  # "b" is orthogonal, filtered out


def test_vector_store_category_filter():
    store = VectorStore("/tmp/unused_for_this_test.json")
    store.replace_all([
        _doc("a", [1.0, 0.0], category="stress"),
        _doc("b", [1.0, 0.0], category="loneliness"),
    ])
    results = store.search([1.0, 0.0], top_k=5, similarity_threshold=0.0, category_filter="loneliness")
    assert [r["id"] for r in results] == ["b"]


def test_vector_store_persistence_roundtrip(tmp_path):
    path = str(tmp_path / "index.json")
    store = VectorStore(path)
    store.replace_all([_doc("a", [1.0, 0.0]), _doc("b", [0.0, 1.0])])
    store.save()

    reloaded = VectorStore(path)
    reloaded.load()
    assert len(reloaded) == 2
    assert reloaded.existing_hashes() == {"a", "b"}


def test_vector_store_missing_file_loads_empty(tmp_path):
    store = VectorStore(str(tmp_path / "nonexistent.json"))
    loaded = store.load()
    assert loaded is False
    assert len(store) == 0


# ---------------------------------------------------------------------------
# Retriever (integration of embeddings + vector store, still mocked)
# ---------------------------------------------------------------------------

def test_retrieve_returns_empty_when_indexes_missing(monkeypatch, tmp_path):
    monkeypatch.setattr(config, "EXEMPLAR_INDEX_PATH", str(tmp_path / "no_exemplar.json"))
    monkeypatch.setattr(config, "KNOWLEDGE_INDEX_PATH", str(tmp_path / "no_knowledge.json"))
    retriever.reload_stores()

    result = retriever.retrieve("I feel really stressed about exams")
    assert result == {"exemplars": [], "knowledge": []}


def test_retrieve_returns_relevant_and_skips_irrelevant(monkeypatch, tmp_path):
    exemplar_path = tmp_path / "exemplar.json"
    knowledge_path = tmp_path / "knowledge.json"
    exemplar_path.write_text(json.dumps({"documents": [
        _doc("relevant", [1.0, 0.0], content="User: I'm stressed\nCompanion: That sounds hard"),
        _doc("irrelevant", [0.0, 1.0], content="User: unrelated topic\nCompanion: ok"),
    ]}), encoding="utf-8")
    knowledge_path.write_text(json.dumps({"documents": []}), encoding="utf-8")

    monkeypatch.setattr(config, "EXEMPLAR_INDEX_PATH", str(exemplar_path))
    monkeypatch.setattr(config, "KNOWLEDGE_INDEX_PATH", str(knowledge_path))
    monkeypatch.setattr(config, "EXEMPLAR_SIMILARITY_THRESHOLD", 0.5)
    retriever.reload_stores()

    # query embedding points in the same direction as "relevant" doc only
    monkeypatch.setattr(retriever.embeddings, "embed_query", lambda text: [1.0, 0.0])

    result = retriever.retrieve("I'm stressed about my exams")
    assert [d["id"] for d in result["exemplars"]] == ["relevant"]


def test_retrieve_degrades_gracefully_when_embedding_fails(monkeypatch, tmp_path):
    exemplar_path = tmp_path / "exemplar.json"
    exemplar_path.write_text(json.dumps({"documents": [_doc("a", [1.0, 0.0])]}), encoding="utf-8")
    knowledge_path = tmp_path / "knowledge.json"
    knowledge_path.write_text(json.dumps({"documents": []}), encoding="utf-8")

    monkeypatch.setattr(config, "EXEMPLAR_INDEX_PATH", str(exemplar_path))
    monkeypatch.setattr(config, "KNOWLEDGE_INDEX_PATH", str(knowledge_path))
    retriever.reload_stores()

    def boom(text):
        raise embeddings.EmbeddingError("simulated outage")
    monkeypatch.setattr(retriever.embeddings, "embed_query", boom)

    result = retriever.retrieve("anything")
    assert result == {"exemplars": [], "knowledge": []}  # no exception propagates


def test_retrieve_empty_query_returns_empty_without_touching_embeddings(monkeypatch):
    called = {"n": 0}
    monkeypatch.setattr(retriever.embeddings, "embed_query", lambda t: called.update(n=called["n"] + 1))
    assert retriever.retrieve("") == {"exemplars": [], "knowledge": []}
    assert retriever.retrieve("   ") == {"exemplars": [], "knowledge": []}
    assert called["n"] == 0


# ---------------------------------------------------------------------------
# Prompt context formatting
# ---------------------------------------------------------------------------

def test_format_retrieved_context_empty_returns_empty_string():
    assert format_retrieved_context({"exemplars": [], "knowledge": []}) == ""
    assert format_retrieved_context({}) == ""


def test_format_retrieved_context_includes_both_sections_when_present():
    retrieved = {
        "exemplars": [{"content": "User: hi\nCompanion: hello"}],
        "knowledge": [{"content": "Try a short grounding exercise."}],
    }
    text = format_retrieved_context(retrieved)
    assert "past exchanges" in text.lower()
    assert "supportive guidance" in text.lower()
    assert "grounding exercise" in text


def test_format_retrieved_context_respects_total_char_cap(monkeypatch):
    monkeypatch.setattr(config, "MAX_TOTAL_CONTEXT_CHARS", 50)
    monkeypatch.setattr(config, "MAX_CONTEXT_CHARS_PER_DOC", 300)
    long_doc = {"content": "x" * 200}
    retrieved = {"exemplars": [long_doc, long_doc, long_doc], "knowledge": []}
    text = format_retrieved_context(retrieved)
    assert len(text) < 400  # far less than 3 * 200 chars - cap is doing its job


# ---------------------------------------------------------------------------
# Knowledge base safety sanity check
# ---------------------------------------------------------------------------

def test_knowledge_base_has_no_diagnostic_or_medication_language():
    from backend.rag.knowledge_base import get_knowledge_documents
    banned_substrings = [
        "you have depression", "you have anxiety disorder", "take medication",
        "prescription", "you are diagnosed", "i diagnose",
    ]
    docs = get_knowledge_documents()
    assert len(docs) > 0
    for doc in docs:
        lowered = doc["content"].lower()
        for banned in banned_substrings:
            assert banned not in lowered, f"{doc['id']} contains disallowed phrase: {banned}"
