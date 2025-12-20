#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script to parse Danish articles and generate exercises using Claude AI.
Generates 20 exercises per article (reading comprehension, vocabulary, grammar).
"""

import json
import os
import re
import sys
from pathlib import Path
from anthropic import Anthropic

# Set UTF-8 encoding for output
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# Initialize Anthropic client
# API key can be set via environment variable or passed directly
api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    print("Warning: ANTHROPIC_API_KEY not set in environment")
    print("You can set it with: set ANTHROPIC_API_KEY=your_key_here")
    print("Or the script will prompt you for it")
    api_key = input("Enter your Anthropic API key: ").strip()

client = Anthropic(api_key=api_key)

def parse_article(file_path):
    """Parse an article markdown file and extract Title, Pompadour, and Text."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract title
    title_match = re.search(r'^Title:\s*(.+)$', content, re.MULTILINE)
    title = title_match.group(1).strip() if title_match else ""

    # Extract pompadour
    pompadour_match = re.search(r'^Pompadour:\s*(.+)$', content, re.MULTILINE)
    pompadour = pompadour_match.group(1).strip() if pompadour_match else ""

    # Extract text (everything after "Text:")
    text_match = re.search(r'^Text:\s*$(.*)', content, re.MULTILINE | re.DOTALL)
    text = text_match.group(1).strip() if text_match else ""

    return {
        "title": title,
        "pompadour": pompadour,
        "text": text
    }

def generate_exercises(article, article_id):
    """Use Claude AI to generate 20 exercises for an article."""

    prompt = f"""You are a Danish language teacher creating exercises for B1-C1 level students.

Based on this Danish article, create EXACTLY 20 exercises that test reading comprehension, vocabulary, and grammar.

Article Title: {article['title']}
Article Pompadour: {article['pompadour']}
Article Text:
{article['text']}

Create a mix of:
- 10 multiple choice questions (reading comprehension and vocabulary)
- 5 open-ended questions (short answer, 1-3 words)
- 5 multiple choice grammar questions

For multiple choice: provide 4 options with the correct answer index (0-3)
For open-ended: provide the exact correct answer

Return ONLY a JSON array with this exact structure (no markdown, no explanation):
[
  {{
    "type": "multiple_choice",
    "question": "What is the main topic of the article?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctIndex": 0
  }},
  {{
    "type": "open_ended",
    "question": "How many Airbnb rentals are in Copenhagen?",
    "correctAnswer": "22684"
  }}
]

Ensure:
- Questions are in English for clarity
- Options/answers use Danish words when testing vocabulary
- Grammar questions test Danish grammar rules
- Mix difficulty levels within B1-C1 range
- Questions refer to specific content in the article
"""

    try:
        message = client.messages.create(
            model="claude-3-5-sonnet-20241022",
            max_tokens=8000,
            temperature=1,
            messages=[{"role": "user", "content": prompt}]
        )

        # Extract JSON from response
        response_text = message.content[0].text.strip()

        # Remove markdown code blocks if present
        if response_text.startswith("```"):
            response_text = re.sub(r'^```json?\n', '', response_text)
            response_text = re.sub(r'\n```$', '', response_text)

        exercises_data = json.loads(response_text)

        # Validate we have exactly 20 exercises
        if len(exercises_data) != 20:
            print(f"Warning: Generated {len(exercises_data)} exercises instead of 20 for {article_id}")

        return exercises_data

    except Exception as e:
        print(f"Error generating exercises for {article_id}: {e}")
        return []

def create_database_json(articles_dir):
    """Process all articles and create database.json."""

    articles_path = Path(articles_dir)
    article_files = sorted(articles_path.glob("*.md"))

    texts = {}
    exercises = []

    print(f"Found {len(article_files)} article files")

    for idx, article_file in enumerate(article_files, start=1):
        print(f"\nProcessing {article_file.name}...")

        # Parse article
        article = parse_article(article_file)
        article_id = f"article_{idx:03d}"

        # Add article to texts
        texts[article_id] = {
            "id": article_id,
            "type": "article",
            "title": article["title"],
            "pompadour": article["pompadour"],
            "content": article["text"],
            "translation": None
        }

        print(f"  Title: {article['title'][:50]}...")
        print(f"  Generating 20 exercises...")

        # Generate exercises using AI
        exercise_data = generate_exercises(article, article_id)

        # Split into 4 sets of 5 exercises each
        for set_idx in range(4):
            start_idx = set_idx * 5
            end_idx = start_idx + 5
            sub_exercises_batch = exercise_data[start_idx:end_idx]

            if not sub_exercises_batch:  # In case we have fewer than 20 exercises
                break

            exercise_id = f"ex_art_{idx:03d}_set{set_idx + 1}"

            exercises.append({
                "id": exercise_id,
                "type": "article",
                "text_id": article_id,
                "question": f"Answer questions about: {article['title'][:60]}... (Set {set_idx + 1}/4)",
                "level": "B1",  # Can adjust based on article difficulty
                "subExercises": sub_exercises_batch
            })

        print(f"  [OK] Generated {len(exercise_data)} exercises in 4 sets of 5")

    # Create final database structure
    database = {
        "texts": texts,
        "exercises": exercises
    }

    # Write to JSON file
    output_file = articles_path.parent / "database.json"
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(database, f, ensure_ascii=False, indent=2)

    print(f"\n[OK] Database created: {output_file}")
    print(f"  Total articles: {len(texts)}")
    print(f"  Total exercise sets: {len(exercises)}")
    print(f"  Total individual exercises: {sum(len(ex['subExercises']) for ex in exercises)}")

if __name__ == "__main__":
    articles_dir = Path(__file__).parent / "articles"

    if not articles_dir.exists():
        print(f"Error: Articles directory not found: {articles_dir}")
        exit(1)

    print("Danish Article Exercise Generator")
    print("=" * 50)

    create_database_json(articles_dir)
