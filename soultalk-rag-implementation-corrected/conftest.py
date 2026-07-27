import os
import sys

# Repo root on sys.path so tests can `import backend.rag...` regardless of
# where pytest is invoked from.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
