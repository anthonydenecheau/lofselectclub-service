#!/usr/bin/env bash

export GITHUB_USER=gcloud-repo
export GITHUB_REPO=github.com/centrale-canine/gcp-migration.git
export LOCAL_REPO=../gcp-migration
export myConfigFile=./terraform/services-vars.tf


echo "INFO- Checking secret env. variables..."
if [ "q${GITHUB_TOKEN}" == "q" ]
then
  echo "ERROR- Secret variable GITHUB_TOKEN is empty!"
  exit 204
fi

myTagRoot=`echo ${BUILD_NAME} | sed  's/^\([a-z-]*\)[0-9-]*$/\1/g'`

echo "INFO- Clone git project: ................... ${GITHUB_REPO}"
git clone https://${GITHUB_USER}:${GITHUB_TOKEN}@${GITHUB_REPO} ${LOCAL_REPO} --branch ${GCP_ENV} --single-branch 2>&1

echo "INFO- Update docker image tag in file: ..... ${myConfigFile}"
echo "INFO- New docker image tag: ................ ${BUILD_NAME}"

cd ${LOCAL_REPO}

sed -i "s/${myTagRoot}[0-9-]*/${BUILD_NAME}/" ${myConfigFile} 2>&1

echo "INFO- Push file: ......... ${myConfigFile}"
echo "INFO- ... to branch: ..... ${GCP_ENV}"

git config user.email "anthony.denecheau@centrale-canine.fr"
git config user.name "${GITHUB_USER}"
git add .
git commit -m":rocket: :wrench: :arrow_up: changed application version" .
git push
