# slack-reddit-bot

1) Zookeeper + Kafka installation tutorial: https://medium.com/@shaaslam/installing-apache-kafka-on-windows-495f6f2fd3c8
2) Start Zookeeper service: zkserver
3) Start Kafka service: .\bin\windows\kafka-server-start.bat .\config\server.properties
4) Create new topic: kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic filtered