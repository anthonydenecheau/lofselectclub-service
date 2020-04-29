#!/bin/bash

export GITHUB_USER=gcloud-repo
export GITHUB_REPO=github.com/centrale-canine/gcp-migration.git
export LOCAL_REPO=../gcp-migration

echo "checking secret env. variables..."

if [ "q${GITHUB_TOKEN}" == "q" ]
then
  echo "ERROR! Secret variable GITHUB_TOKEN is empty!"
  exit 204
fi

echo "clone git project: ${GITHUB_REPO}..."

git clone https://${GITHUB_USER}:${GITHUB_TOKEN}@${GITHUB_REPO} ${LOCAL_REPO} --branch ${GCP_ENV} --single-branch 2>&1

cd ${LOCAL_REPO}/terraform/code

echo "update .env file ..."
sed -i "s/export TAG_SCC_LOFSELECTCLUB_SERVICE=.*/export TAG_SCC_LOFSELECTCLUB_SERVICE=${BUILD_NAME}/" .env 2>&1

echo "push .env file to branch ${GCP_ENV}..."
git config user.email "anthony.denecheau@centrale-canine.fr"
git config user.name "${GITHUB_USER}"
git add .
git commit -m":rocket: :wrench: :arrow_up: changed application version" .
git push

