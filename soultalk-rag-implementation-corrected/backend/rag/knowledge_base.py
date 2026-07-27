"""
Authored knowledge-base documents for the "knowledge" retrieval track.

These are short, generic, non-diagnostic coping/psychoeducation notes -
NOT a substitute for the conversational exemplar dataset, and not clinical
guidance. They exist so Wolfie's suggestions are grounded in something
written deliberately, instead of generated fresh every time.

Explicit rules this content follows (kept in sync with safety_layer.py):
- No diagnosis language, no medical claims, no medication references.
- No content here is a crisis response - crisis is handled entirely by
  safety_layer.py before retrieval ever runs.
- Every note is a *suggestion*, never an instruction, and stays generic
  enough to apply across a user base rather than any one person's history.

This file was authored to fill a real gap: the training dataset only had
conversational tone exemplars, no actual coping-technique content, so this
half of RAG did not exist before this pipeline. Treat it as a v1 starter
set - it should grow with clinician review over time, not stay hardcoded.
"""

# category: matches (loosely, via config.CATEGORY_ALIASES normalization)
# the category vocabulary already used in conversations.json, so the same
# retrieval call can surface both an exemplar and a knowledge note for the
# same underlying topic.
KNOWLEDGE_DOCUMENTS = [
    {
        "id": "kb_stress_grounding",
        "category": "stress",
        "content": (
            "A short grounding exercise can help when stress feels like too much at once: "
            "naming 5 things you can see, 4 you can hear, 3 you can touch, 2 you can smell, "
            "and 1 you can taste. It works by shifting attention out of racing thoughts and "
            "into the present moment, even for a minute or two."
        ),
    },
    {
        "id": "kb_exam_stress_breakdown",
        "category": "exam_stress",
        "content": (
            "When exam pressure feels overwhelming, breaking study material into small, "
            "specific blocks (e.g. '25 minutes on one topic, then a short break') tends to "
            "reduce the sense of an impossible mountain more than trying to plan the whole "
            "syllabus at once."
        ),
    },
    {
        "id": "kb_college_pressure_prioritize",
        "category": "college_pressure",
        "content": (
            "When assignments, exams, and projects all land together, it can help to write "
            "everything down in one place and sort by deadline, not by how stressful each "
            "one feels - anxiety about a task doesn't always match how urgent it actually is."
        ),
    },
    {
        "id": "kb_loneliness_small_steps",
        "category": "loneliness",
        "content": (
            "Loneliness often improves gradually through small, low-pressure social contact "
            "rather than one big effort - a short message to one person, or sitting in a "
            "shared space without needing to talk, both count."
        ),
    },
    {
        "id": "kb_overthinking_externalize",
        "category": "overthinking",
        "content": (
            "Writing down a spiraling thought exactly as it occurs, rather than just turning "
            "it over mentally, can make it easier to notice which parts are facts and which "
            "parts are worst-case predictions the mind is filling in."
        ),
    },
    {
        "id": "kb_breakup_pacing",
        "category": "breakup",
        "content": (
            "After a breakup, it's common for grief to come in waves rather than steadily "
            "decreasing - a harder day after several easier ones isn't a sign of going "
            "backward, it's a normal part of how people process loss."
        ),
    },
    {
        "id": "kb_family_issues_boundaries",
        "category": "family_issues",
        "content": (
            "When home feels tense or full of conflict, having a small, private routine that "
            "is entirely one's own - a walk, music, a notebook - can create a bit of steady "
            "ground that doesn't depend on how the household is doing that day."
        ),
    },
    {
        "id": "kb_job_search_rejection",
        "category": "job_search",
        "content": (
            "Repeated silence or rejection during a job search is discouraging by nature, not "
            "a reliable measure of someone's ability - most hiring processes reject far more "
            "qualified candidates than they accept, for reasons that often have nothing to do "
            "with the applicant."
        ),
    },
    {
        "id": "kb_social_anxiety_exposure",
        "category": "social_anxiety",
        "content": (
            "Social anxiety tends to ease with small, repeated, low-stakes exposure rather "
            "than avoidance - a short interaction that goes okay does more to update the "
            "anxious prediction than avoiding the situation entirely."
        ),
    },
    {
        "id": "kb_burnout_rest_vs_distraction",
        "category": "burnout",
        "content": (
            "Burnout often needs actual rest - sleep, quiet, unscheduled time - rather than "
            "just a change of activity; scrolling or bingeing something can feel like a break "
            "but doesn't always restore energy the way stepping away completely does."
        ),
    },
    {
        "id": "kb_self_doubt_evidence",
        "category": "self_doubt",
        "content": (
            "Self-doubt tends to generalize from a single event ('I failed this' becomes 'I'm "
            "a failure'). Gently separating the specific situation from a judgment about "
            "one's whole worth can make the feeling more manageable without dismissing it."
        ),
    },
    {
        "id": "kb_anger_pause",
        "category": "anger",
        "content": (
            "A short pause before responding when anger spikes - even just a few slow "
            "breaths or stepping out of the room - gives the initial surge of reactivity time "
            "to settle before deciding how to respond."
        ),
    },
    {
        "id": "kb_homesickness_connection",
        "category": "homesickness",
        "content": (
            "Homesickness usually eases as new routines and relationships form in the new "
            "place - keeping light, regular contact with home (a short call, a photo) while "
            "still building local connections tends to work better than choosing one over "
            "the other."
        ),
    },
    {
        "id": "kb_sleep_problems_winddown",
        "category": "sleep_problems",
        "content": (
            "Racing thoughts at bedtime often get worse the more someone tries to force sleep "
            "- a short wind-down routine before bed (dim lights, no screens, a few minutes of "
            "quiet) signals to the body that it's time to slow down rather than trying to "
            "control the thoughts directly."
        ),
    },
    {
        "id": "kb_comparison_with_others",
        "category": "comparison_with_others",
        "content": (
            "Comparing one's own full, ordinary life to someone else's edited highlights "
            "(social media, a friend's best moments) is an uneven comparison by design - it "
            "often says more about what gets shared than about how that person is actually doing."
        ),
    },
    {
        "id": "kb_fear_of_failure_reframe",
        "category": "fear_of_failure",
        "content": (
            "Fear of failure often shrinks once the worst realistic outcome is spelled out "
            "concretely, rather than left as a vague dread - most 'worst case' scenarios, "
            "written out plainly, turn out to be survivable even if unwanted."
        ),
    },
    {
        "id": "kb_trust_issues_pace",
        "category": "trust_issues",
        "content": (
            "Rebuilding trust after being hurt tends to go better gradually, testing it in "
            "small, low-risk ways over time, rather than either withholding it completely or "
            "extending it fully all at once."
        ),
    },
    {
        "id": "kb_toxic_friendship_signals",
        "category": "toxic_friendship",
        "content": (
            "A friendship that consistently leaves someone feeling smaller, anxious, or "
            "constantly at fault - even when nothing 'big' has happened - is worth paying "
            "attention to; consistent patterns matter more than any single incident."
        ),
    },
    {
        "id": "kb_motivation_smallest_step",
        "category": "motivation",
        "content": (
            "When motivation is completely gone, starting with the smallest possible version "
            "of a task (opening the laptop, writing one sentence) often does more than waiting "
            "to feel motivated first - momentum tends to follow action, not the other way "
            "around."
        ),
    },
    {
        "id": "kb_public_speaking_fear",
        "category": "public_speaking_fear",
        "content": (
            "Fear of public speaking is one of the most common fears and doesn't reflect a "
            "person's actual competence - preparing the opening 30 seconds thoroughly is "
            "often enough to reduce the worst of the anxiety, since starting is usually the "
            "hardest part."
        ),
    },
]


def get_knowledge_documents():
    """Return the static knowledge-base documents as plain dicts."""
    return [dict(doc) for doc in KNOWLEDGE_DOCUMENTS]
