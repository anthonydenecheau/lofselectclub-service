echo "Tagging build with $BUILD_NAME"
export TARGET_URL="https://api.github.com/repos/anthonydenecheau/lofselectclub-service/releases?access_token=$GITHUB_TOKEN"

echo "###"
echo $TARGET_URL

body="{
  \"tag_name\": \"$BUILD_NAME\",
  \"target_commitish\": \"master\",
  \"name\": \"$BUILD_NAME\",
  \"body\": \"Tag version $BUILD_NAME\",
  \"draft\": false,
  \"prerelease\": false
}"

echo "###"
echo $body

curl -k -X POST \
  -H "Content-Type: application/json" \
  -d "$body" \
  $TARGET_URL
