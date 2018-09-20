echo "Pushing service docker images to docker hub ...."
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker push anthonydenecheau/scc-lofselectclub-service:$BUILD_NAME