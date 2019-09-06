#!/usr/bin/env bash
for i in {1..10}
do
        ecs-cli compose scale $1 --cluster pot7-consumerCluster-$i --region ap-south-1
        next=`expr "$i" + 1`
        sed -i "s/topic$i/topic$next/g" docker-compose.yml
done
sed -i "s/topic$next/topic1/g" docker-compose.yml
