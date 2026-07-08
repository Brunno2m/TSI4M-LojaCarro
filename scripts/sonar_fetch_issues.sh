#!/usr/bin/env bash
# Utility script to fetch Sonar issues and print simple suggested fixes.
# Usage: SONAR_HOST_URL=... SONAR_TOKEN=... SONAR_PROJECT_KEY=... ./scripts/sonar_fetch_issues.sh

if [ -z "$SONAR_HOST_URL" ] || [ -z "$SONAR_TOKEN" ] || [ -z "$SONAR_PROJECT_KEY" ]; then
  echo "Please set SONAR_HOST_URL, SONAR_TOKEN and SONAR_PROJECT_KEY environment variables"
  exit 2
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq not found. Please install jq to run this script."
  exit 2
fi

echo "Polling SonarQube for quality gate..."
for i in {1..30}; do
  resp=$(curl -s -u ${SONAR_TOKEN}: "${SONAR_HOST_URL}/api/qualitygates/project_status?projectKey=${SONAR_PROJECT_KEY}")
  status=$(echo "$resp" | jq -r '.projectStatus.status')
  if [ "$status" != "PENDING" ] && [ "$status" != "IN_PROGRESS" ]; then
    echo "Quality gate status: $status"
    break
  fi
  echo "Status is $status; sleeping 5s"
  sleep 5
done

if [ "$status" != "OK" ]; then
  echo "Quality gate failed or unstable — fetching issues..."
  curl -s -u ${SONAR_TOKEN}: "${SONAR_HOST_URL}/api/issues/search?componentKeys=${SONAR_PROJECT_KEY}&ps=500" -o sonar_issues.json
  echo "--- Sonar issues (summary) ---"
  jq -r '.issues[] | "[\(.severity)] \(.rule) at \(.component):\(.line) -> \(.message)"' sonar_issues.json

  echo "\n--- Suggested fixes (generic) ---"
  jq -r '.issues[] | "Rule: \(.rule) - Suggestion: Review the issue message and apply the recommended fix in Sonar rule documentation."' sonar_issues.json
  exit 1
else
  echo "Quality gate passed (OK)."
fi
