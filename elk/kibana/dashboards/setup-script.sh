#!/bin/bash

# Wait for Kibana to be ready
echo "Waiting for Kibana to be ready..."
until curl -s http://kibana:5601/api/status | grep -q "available"; do
  echo "Kibana is unavailable - sleeping"
  sleep 5
done

echo "Kibana is ready!"

# Import index patterns
echo "Creating index patterns..."
curl -X POST "kibana:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -T /usr/share/kibana/dashboards/index-patterns.json

# Import dashboards
echo "Creating dashboards..."
curl -X POST "kibana:5601/api/saved_objects/_import" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -T /usr/share/kibana/dashboards/microservices-overview.json

echo "Dashboard setup completed!"