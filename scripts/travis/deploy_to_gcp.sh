
echo "clone gcp project ..."
git clone https://anthonydenecheau:${GITHUB_TOKEN}@https://github.com/centrale-canine/gcp-migration.git 2>&1
cd terraform/code

echo "update .env file ..."
sed -i "s/export TAG_SCC_LOFSELECTCLUB_SERVICE=.*/export TAG_SCC_LOFSELECTCLUB_SERVICE=${BUILD_NAME}/" .env 2>&1

echo "update .env file ..."
git add .
git commit -m":lock: changed version application" .
git push

echo "push to branch re7..."
git checkout -b re7
git pull origin re7
git merge origin/master

git push -u origin re7
