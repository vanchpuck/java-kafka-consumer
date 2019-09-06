# ECS Kafka consumer

This Kafka consumer is aimed to measure cross-region consumer delay and check whether any message was duplicated or missing.

The corresponding docker container is already loaded to ECR, if some changes in source code required the container could be redeployed using the `deploy.sh` script. The docker container will be built and sent to ECR.

## Consumer configuration
The Consumer could be configured in `docker-compose.yml` file. Here are the configuration options:
* CONSUMER_GROUP - consumer group name
* BOOTSTRAP_SERVERS - kafka bootstrap servers
* TOPIC_NAME - topic to read messages from
* POLL_WINDOW_MILLISECONDS - consumer poll window in milliseconds

ECS infrastructure is currently set up to distribute the loading between several ECS clusters. Each cluster reads messages only from one specific topic. So if messages should be read from the multiple topics, the multiple clusters will be used.
By default tasks are distributed across 10 clusters. Use the `run-tasks.sh` script to run the tasks. This script receives one parameter - the number of tasks to lunch on the cluster. The script will override the `TOPIC_NAME` environment variable in the `docker-compose.yml` file, so be aware than if the topic name was changed in `docker-compose.yml` file, the template for the `sed` command in `run-scripts.sh` should be also modified.
