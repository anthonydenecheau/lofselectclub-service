#!/bin/bash
set -x

echo "checking secret env. variables..."
if [ "q${GITHUB_TOKEN}" == "q" ]
then 
  echo "ERROR! Variables github_user or github_token are empty!"
  exit 204
fi 

export GITHUB_REPO=github.com/centrale-canine/gcp-migration.git

echo "clone git project: ${GITHUB_REPO}..."

git clone https://${github_user}:${github_token}@${GITHUB_REPO} 2>&1

cd terraform/code

echo "update .env file ..."
sed -i "s/export TAG_SCC_LOFSELECTCLUB_SERVICE=.*/export TAG_SCC_LOFSELECTCLUB_SERVICE=${BUILD_NAME}/" .env 2>&1

echo "push .env file ..."
git config user.email "anthony.denecheau@centrale-canine.fr"
git config user.name "${github_user}"
git add .
git commit -m":rocket: :wrench: :arrow_up: changed application version" .
git push

echo "push to branch re7..."
git checkout --track origin/re7
git merge master

git push -u origin re7

