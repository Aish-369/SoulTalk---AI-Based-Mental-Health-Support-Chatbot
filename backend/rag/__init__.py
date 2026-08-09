"""
RAG (Retrieval-Augmented Generation) subsystem for SoulTalk.

Two retrieval tracks, kept separate on purpose:

- "exemplar" documents: past user/companion exchanges from the training
  dataset, retrieved to keep Wolfie's Roman-Marathi tone and conversational
  rhythm consistent. These are style references, not facts.
- "knowledge" documents: short, authored, non-diagnostic psychoeducation /
  coping-technique notes, retrieved to ground supportive suggestions in
  something more considered than free generation.

Nothing here participates in crisis handling. Crisis detection in
safety_layer.py runs before retrieval and short-circuits the response
entirely when triggered - RAG only ever sees messages that already
passed that check.
"""
