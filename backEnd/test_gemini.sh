#!/bin/bash

# Navigate to the directory where the script is located
cd "$(dirname "$0")"

# Source the .env file if it exists
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
else
  echo "Error: .env file not found in $(pwd)"
  exit 1
fi

if [ -z "$GEMINI_API_KEY" ]; then
  echo "Error: GEMINI_API_KEY is not set in your .env file."
  exit 1
fi

echo "Testing Gemini API with a 'Hello' message..."
echo "Using API Key: ${GEMINI_API_KEY:0:5}***************************"
echo "----------------------------------------"

curl -s "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{
    "contents": [{
      "parts":[{
        "text": "Hello! Please reply with a short greeting to confirm you are working."
      }]
    }]
  }'

echo -e "\n----------------------------------------"
echo "If you see a \"429 Too Many Requests\" error here, your API key or project has been rate-limited by Google."