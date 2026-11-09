import re
import json
import warnings
import shutil
from pathlib import Path

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import plotly.express as px
import joblib

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestRegressor
from sklearn.cluster import KMeans
from sklearn.metrics import (
    mean_absolute_error,
    mean_squared_error,
    r2_score,
    silhouette_score
)

warnings.filterwarnings("ignore")
np.random.seed(42)
sns.set_theme(style="whitegrid")
pd.set_option("display.max_colwidth", 120)


SKILL_ALIASES = {
    "py": "python",
    "python3": "python",
    "js": "javascript",
    "ml": "machine learning",
    "ai": "artificial intelligence",
    "dl": "deep learning",
    "nlp": "natural language processing",
    "powerbi": "power bi",
    "ms excel": "excel",
    "cpp": "c++",
    "sklearn": "scikit-learn",
    "reactjs": "react",
    "node": "node.js"
}

SKILL_CATALOG = [
    "python", "java", "c", "c++", "javascript", "typescript",
    "html", "css", "react", "node.js", "sql", "mysql",
    "postgresql", "mongodb", "excel", "power bi", "tableau",
    "statistics", "data analysis", "data cleaning", "pandas",
    "numpy", "matplotlib", "seaborn", "plotly",
    "machine learning", "deep learning", "artificial intelligence",
    "natural language processing", "computer vision",
    "scikit-learn", "tensorflow", "pytorch", "generative ai",
    "prompt engineering", "cloud computing", "aws", "azure",
    "google cloud", "linux", "networking", "cybersecurity",
    "ethical hacking", "docker", "kubernetes", "git", "github",
    "rest api", "fastapi", "flask", "django", "streamlit",
    "problem solving", "communication", "teamwork", "leadership",
    "project management", "critical thinking", "figma", "ui design"
]


def normalize_skill(value):
    value = re.sub(r"\s+", " ", str(value).strip().lower())
    value = value.replace("_", " ").replace("-", " ")
    return SKILL_ALIASES.get(value, value)


def normalize_skill_list(values):
    if isinstance(values, str):
        values = re.split(r"[,;|\n]", values)

    return sorted({
        normalize_skill(value)
        for value in values
        if str(value).strip()
    })


CAREER_ROLES = [
    {
        "role": "Data Analyst",
        "category": "Data",
        "skills": ["sql", "excel", "python", "pandas",
                   "statistics", "power bi", "data visualization"],
        "interests": ["data", "business", "research"],
        "description": "Turns raw data into reports and business insights.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 7.0,
        "learning": [
            "Excel and SQL fundamentals",
            "Python with Pandas",
            "Statistics and visualization",
            "Build a dashboard portfolio"
        ]
    },
    {
        "role": "Data Scientist",
        "category": "Data",
        "skills": ["python", "sql", "statistics", "machine learning",
                   "pandas", "numpy", "scikit-learn"],
        "interests": ["data", "research", "mathematics"],
        "description": "Uses statistics and machine learning to solve data problems.",
        "salary_min_lpa": 5.0,
        "salary_max_lpa": 12.0,
        "learning": [
            "Python, SQL and statistics",
            "Exploratory data analysis",
            "Supervised machine learning",
            "Model deployment"
        ]
    },
    {
        "role": "Machine Learning Engineer",
        "category": "AI",
        "skills": ["python", "machine learning", "scikit-learn",
                   "tensorflow", "pytorch", "rest api", "docker", "git"],
        "interests": ["ai", "engineering", "research"],
        "description": "Builds, evaluates and deploys machine-learning systems.",
        "salary_min_lpa": 5.0,
        "salary_max_lpa": 14.0,
        "learning": [
            "Python and software engineering",
            "ML algorithms and evaluation",
            "Deep learning framework",
            "APIs and deployment"
        ]
    },
    {
        "role": "AI Application Developer",
        "category": "AI",
        "skills": ["python", "artificial intelligence", "generative ai",
                   "prompt engineering", "rest api", "streamlit", "git"],
        "interests": ["ai", "building", "creativity"],
        "description": "Creates practical applications using AI models and APIs.",
        "salary_min_lpa": 3.5,
        "salary_max_lpa": 9.0,
        "learning": [
            "Python and API basics",
            "Prompt design and evaluation",
            "Build an AI assistant",
            "Deploy and document an application"
        ]
    },
    {
        "role": "Frontend Developer",
        "category": "Software",
        "skills": ["html", "css", "javascript", "react",
                   "typescript", "git", "figma"],
        "interests": ["design", "building", "web"],
        "description": "Builds responsive and accessible user interfaces.",
        "salary_min_lpa": 2.8,
        "salary_max_lpa": 7.0,
        "learning": [
            "HTML and semantic structure",
            "CSS and responsive design",
            "JavaScript fundamentals",
            "React portfolio projects"
        ]
    },
    {
        "role": "Backend Developer",
        "category": "Software",
        "skills": ["python", "java", "sql", "rest api",
                   "fastapi", "django", "git", "docker"],
        "interests": ["engineering", "building", "systems"],
        "description": "Builds server-side logic, APIs and data services.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 8.0,
        "learning": [
            "One backend language",
            "Databases and SQL",
            "REST API development",
            "Testing and deployment"
        ]
    },
    {
        "role": "Full Stack Developer",
        "category": "Software",
        "skills": ["html", "css", "javascript", "react", "node.js",
                   "sql", "mongodb", "rest api", "git"],
        "interests": ["web", "building", "engineering"],
        "description": "Develops both frontend and backend components.",
        "salary_min_lpa": 4.0,
        "salary_max_lpa": 10.0,
        "learning": [
            "Frontend fundamentals",
            "Backend and API development",
            "Database design",
            "Deploy a complete application"
        ]
    },
    {
        "role": "Cybersecurity Analyst",
        "category": "Security",
        "skills": ["linux", "networking", "cybersecurity",
                   "ethical hacking", "python", "problem solving"],
        "interests": ["security", "investigation", "systems"],
        "description": "Monitors systems, investigates threats and improves security.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 8.0,
        "learning": [
            "Networking and Linux",
            "Security fundamentals",
            "Log analysis and tools",
            "Labs and reports"
        ]
    },
    {
        "role": "Cloud Engineer",
        "category": "Cloud",
        "skills": ["cloud computing", "aws", "azure", "linux",
                   "networking", "docker", "kubernetes", "git"],
        "interests": ["cloud", "systems", "engineering"],
        "description": "Builds and maintains cloud infrastructure and services.",
        "salary_min_lpa": 4.0,
        "salary_max_lpa": 11.0,
        "learning": [
            "Linux and networking",
            "Choose one cloud platform",
            "Containers and infrastructure",
            "Deploy and monitor a cloud project"
        ]
    },
    {
        "role": "Business Intelligence Developer",
        "category": "Data",
        "skills": ["sql", "power bi", "excel", "data analysis",
                   "data cleaning", "statistics"],
        "interests": ["business", "data", "communication"],
        "description": "Designs dashboards and data models for business decisions.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 8.0,
        "learning": [
            "Excel and SQL",
            "Data modeling",
            "Power BI dashboards",
            "Business case-study portfolio"
        ]
    },
    {
        "role": "UI/UX Designer",
        "category": "Design",
        "skills": ["figma", "ui design", "communication",
                   "critical thinking", "html", "css"],
        "interests": ["design", "creativity", "people"],
        "description": "Researches user needs and designs usable digital experiences.",
        "salary_min_lpa": 2.5,
        "salary_max_lpa": 7.0,
        "learning": [
            "Design principles and Figma",
            "User research",
            "Wireframes and prototypes",
            "Usability testing"
        ]
    },
    {
        "role": "QA Automation Engineer",
        "category": "Software",
        "skills": ["python", "javascript", "problem solving",
                   "git", "rest api", "sql"],
        "interests": ["quality", "engineering", "testing"],
        "description": "Automates software tests and improves product quality.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 8.0,
        "learning": [
            "Testing fundamentals",
            "Python or JavaScript",
            "Automation framework",
            "CI workflow"
        ]
    },
    {
        "role": "DevOps Engineer",
        "category": "Cloud",
        "skills": ["linux", "git", "docker", "kubernetes",
                   "cloud computing", "aws", "python", "networking"],
        "interests": ["automation", "systems", "engineering"],
        "description": "Automates software delivery and infrastructure operations.",
        "salary_min_lpa": 4.5,
        "salary_max_lpa": 12.0,
        "learning": [
            "Linux, Git and scripting",
            "CI/CD pipelines",
            "Docker and Kubernetes",
            "Cloud deployment"
        ]
    },
    {
        "role": "Python Developer",
        "category": "Software",
        "skills": ["python", "git", "sql", "rest api",
                   "fastapi", "django", "problem solving"],
        "interests": ["programming", "automation", "building"],
        "description": "Builds software, automation scripts and backend services with Python.",
        "salary_min_lpa": 3.0,
        "salary_max_lpa": 8.5,
        "learning": [
            "Python fundamentals and OOP",
            "Data structures and testing",
            "Build APIs or automation tools",
            "Deploy and document projects"
        ]
    },
    {
        "role": "NLP Engineer",
        "category": "AI",
        "skills": ["python", "natural language processing",
                   "machine learning", "deep learning",
                   "pytorch", "generative ai"],
        "interests": ["language", "ai", "research"],
        "description": "Builds systems that process and understand human language.",
        "salary_min_lpa": 6.0,
        "salary_max_lpa": 16.0,
        "learning": [
            "Python and ML foundations",
            "Text processing and embeddings",
            "Transformers and evaluation",
            "Build an NLP application"
        ]
    },
    {
        "role": "Technical Support Engineer",
        "category": "IT Operations",
        "skills": ["networking", "linux", "problem solving",
                   "communication", "sql"],
        "interests": ["troubleshooting", "people", "systems"],
        "description": "Diagnoses technical issues and supports users and systems.",
        "salary_min_lpa": 2.2,
        "salary_max_lpa": 5.5,
        "learning": [
            "Computer and network fundamentals",
            "Operating systems",
            "Troubleshooting methods",
            "Communication practice"
        ]
    },
    {
        "role": "Product Analyst",
        "category": "Business",
        "skills": ["sql", "excel", "statistics", "data analysis",
                   "communication", "power bi"],
        "interests": ["business", "data", "product"],
        "description": "Analyzes product usage and recommends improvements.",
        "salary_min_lpa": 4.0,
        "salary_max_lpa": 10.0,
        "learning": [
            "SQL and analytics",
            "Product metrics",
            "Experiment analysis",
            "Communicate insights"
        ]
    },
    {
        "role": "IT Project Coordinator",
        "category": "Business",
        "skills": ["project management", "communication", "excel",
                   "teamwork", "problem solving"],
        "interests": ["planning", "people", "business"],
        "description": "Coordinates schedules, tasks, communication and project delivery.",
        "salary_min_lpa": 2.5,
        "salary_max_lpa": 6.0,
        "learning": [
            "Project lifecycle fundamentals",
            "Agile and Scrum basics",
            "Planning and reporting tools",
            "Coordinate a portfolio project"
        ]
    }
]

career_df = pd.DataFrame(CAREER_ROLES)

career_df["profile_text"] = career_df.apply(
    lambda row: " ".join([
        row["role"],
        row["category"],
        row["description"],
        " ".join(row["skills"]),
        " ".join(row["interests"])
    ]),
    axis=1
)

print(career_df[
    ["role", "category", "salary_min_lpa", "salary_max_lpa"]
])
