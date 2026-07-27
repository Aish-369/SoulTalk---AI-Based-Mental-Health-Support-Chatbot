"""
Formats retrieved documents into text for prompt injection.

Kept separate from context_injection.py so the two retrieval tracks
(exemplars vs knowledge) stay clearly labeled and bounded in length before
they ever reach the LLM prompt - the RAG requirements explicitly call for
not blindly dumping full retrieved documents into the context window.
"""
from typing import Dict, List

from . import config


def _truncate(text: str, limit: int) -> str:
    text = text.strip()
    if len(text) <= limit:
        return text
    return text[:limit].rsplit(" ", 1)[0] + "..."


def format_retrieved_context(retrieved: Dict[str, List[Dict]]) -> str:
    """
    Build the RETRIEVED CONTEXT block. Returns an empty string if nothing
    was retrieved (in which case the caller should omit the section
    entirely rather than print an empty header).
    """
    exemplars = retrieved.get("exemplars") or []
    knowledge = retrieved.get("knowledge") or []

    if not exemplars and not knowledge:
        return ""

    sections = []
    used_chars = 0

    if exemplars:
        lines = ["Similar past exchanges (for tone and style reference only - "
                 "do not treat as facts about this user):"]
        for doc in exemplars:
            snippet = _truncate(doc.get("content", ""), config.MAX_CONTEXT_CHARS_PER_DOC)
            if used_chars + len(snippet) > config.MAX_TOTAL_CONTEXT_CHARS:
                break
            lines.append(f"- {snippet}")
            used_chars += len(snippet)
        sections.append("\n".join(lines))

    if knowledge:
        lines = ["Relevant supportive guidance (use only if it fits the conversation; "
                 "do not present as medical advice or diagnosis):"]
        for doc in knowledge:
            snippet = _truncate(doc.get("content", ""), config.MAX_CONTEXT_CHARS_PER_DOC)
            if used_chars + len(snippet) > config.MAX_TOTAL_CONTEXT_CHARS:
                break
            lines.append(f"- {snippet}")
            used_chars += len(snippet)
        sections.append("\n".join(lines))

    return "\n\n".join(sections)
