#!/bin/bash
# Load environment variables from .env and start the application
export $(grep -v '^#' .env | grep -v '^$' | xargs)
mvn spring-boot:run
