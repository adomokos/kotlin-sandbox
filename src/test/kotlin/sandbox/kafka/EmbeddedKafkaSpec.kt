package sandbox.kafka

import com.salesforce.kafka.test.KafkaBroker
import com.salesforce.kafka.test.KafkaTestCluster
import com.salesforce.kafka.test.KafkaTestUtils
import io.kotlintest.specs.DescribeSpec
import mu.KotlinLogging

// https://github.com/salesforce/kafka-junit/tree/master/kafka-junit-core

class EmbeddedKafkaSpec : DescribeSpec() {
    private val logger = KotlinLogging.logger {}

    init {
        describe("Embedded Kafka") {
            it("can broker offline") {
                // If no argument, default to cluster size of 1.
                val clusterSize = 1

                logger.info("hello")
                println("Starting up kafka cluster with $clusterSize brokers")

                // Create a test cluster
                val kafkaTestCluster = KafkaTestCluster(clusterSize)

                // Start the cluster.
                kafkaTestCluster.start()

                // Create a topic
                val topicName = "TestTopicA"
                val utils = KafkaTestUtils(kafkaTestCluster)
                utils.createTopic(topicName, clusterSize, clusterSize.toShort())

                // Publish some data into that topic
                // Publish some data into that topic
                for (partition in 0 until clusterSize) {
                    utils.produceRecords(1000, topicName, partition)
                }

                kafkaTestCluster
                    .kafkaBrokers
                    .stream()
                    .forEach { broker: KafkaBroker ->
                        println(
                            "Started broker with Id " + broker.brokerId + " at " + broker.connectString
                        )
                    }

                println("Cluster started at: " + kafkaTestCluster.kafkaConnectString)

                println("Shutting down cluster...")
                kafkaTestCluster.close()
            }
        }
    }
}
