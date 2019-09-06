mvn clean package
docker build --network host --no-cache -t pot7-consumer-runner .
$(aws ecr get-login --no-include-email --region ap-south-1)
docker tag pot7-consumer-runner 311630568628.dkr.ecr.ap-south-1.amazonaws.com/pot7-consumer-runner
docker push 311630568628.dkr.ecr.ap-south-1.amazonaws.com/pot7-consumer-runner
docker logout https://311630568628.dkr.ecr.ap-south-1.amazonaws.com