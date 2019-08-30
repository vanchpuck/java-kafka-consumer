$(aws ecr get-login --no-include-email)
docker tag pot7-consumer-runner $1/pot7-consumer-runner
docker push $1/pot7-consumer-runner
docker logout https://$1
