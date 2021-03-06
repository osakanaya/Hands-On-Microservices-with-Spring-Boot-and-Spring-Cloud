#!/usr/bin/env bash

# Print commands to the terminal before execution and stop the script if any error occurs
set -ex

aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com

docker tag hands-on/product-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:latest
docker tag hands-on/product-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:v1
docker tag hands-on/product-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:v2

docker tag hands-on/recommendation-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:latest
docker tag hands-on/recommendation-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:v1
docker tag hands-on/recommendation-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:v2

docker tag hands-on/review-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:latest
docker tag hands-on/review-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:v1
docker tag hands-on/review-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:v2

docker tag hands-on/product-composite-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:latest
docker tag hands-on/product-composite-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:v1
docker tag hands-on/product-composite-service:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:v2

docker tag hands-on/auth-server:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:latest
docker tag hands-on/auth-server:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:v1
docker tag hands-on/auth-server:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:v2

docker tag hands-on/gateway:latest 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/gateway:latest

docker tag hands-on/fluentd:v1 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/fluentd:v1

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:latest
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:v1
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-service:v2

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:latest
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:v1
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/recommendation-service:v2

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:latest
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:v1
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/review-service:v2

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:latest
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:v1
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/product-composite-service:v2

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:latest
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:v1
docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/auth-server:v2

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/gateway:latest

docker push 102132116425.dkr.ecr.ap-northeast-1.amazonaws.com/fluentd:v1

set +ex