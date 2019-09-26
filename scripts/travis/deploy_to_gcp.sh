#!/usr/bin/env bash

GITHUB_USER=anthonydenecheau
GITHUB_REPO=github.com/centrale-canine/gcp-migration.git

if [ "q${GITHUB_BRANCH}" == "q" ]
then
    echo "ERROR! A specific branch indicating the environment to deploy to should be specified in the GITHUB_BRANCH env. variable!"
    exit 400
fi

echo "clone gcp project..."
echo "branch: ${GITHUB_BRANCH}"

git clone --single-branch --branch ${GITHUB_BRANCH} https://${GITHUB_USER}:${GITHUB_TOKEN}@${GITHUB_REPO} 2>&1

cd terraform/code

echo "update .env file ..."
sed -i "s/export TAG_SCC_LOFSELECTCLUB_SERVICE=.*/export TAG_SCC_LOFSELECTCLUB_SERVICE=${BUILD_NAME}/" .env 2>&1

echo "update .env file ..."

git config user.email "anthony.denecheau@centrale-canine.fr"
git config user.name "${GITHUB_USER}"
git add .
git commit -m":rocket: :wrench: :arrow_up: changed application version" .
git push

#echo "push to branch re7..."
#git checkout -b re7
#git pull origin re7
#git merge origin/master
#
#git push -u origin re7

