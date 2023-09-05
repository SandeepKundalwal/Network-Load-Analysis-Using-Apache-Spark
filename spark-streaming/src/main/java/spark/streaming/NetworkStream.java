package spark.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.serializer.KryoSerializer;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Serializable;
import scala.Tuple2;
import spark.streaming.config.SocketConfig;
import spark.streaming.model.IPPacket;
import spark.streaming.service.LogReceiver;

import java.util.Arrays;

public class NetworkStream implements Serializable {
    public static SocketConfig socketConfig;
    public static ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void main(String[] args) throws Exception {

        socketConfig = new SocketConfig(Integer.parseInt(args[2]));

        new Thread(socketConfig).start();

        SparkConf conf = new SparkConf().setMaster("local[2]")
                .setAppName("NetworkStream")
                .set("spark.serializer", KryoSerializer.class.getName());

        //      Batch interval 5sec
        JavaStreamingContext jssc = new JavaStreamingContext(conf, new Duration(5000));

        //      Define the socket where the system will listen
        //      Lines is not a rdd but a sequence of rdd, not static, constantly changing

        JavaReceiverInputDStream<String> lines = jssc.receiverStream(new LogReceiver(args[0], Integer.parseInt(args[1])));
        //      Split each line into words
        JavaDStream<IPPacket> packets = lines.flatMap(x -> Arrays.stream(parseLogLine(x)).iterator());

        JavaPairDStream<Long, IPPacket> pairs = packets.mapToPair((PairFunction<IPPacket, Long, IPPacket>) s -> new Tuple2<>(s.time, s));

        //      Cumulate the sum from each batch
        JavaPairDStream<Long, IPPacket> outputDStream = pairs.reduceByKey(
                (Function2<IPPacket, IPPacket, IPPacket>) (packet1, packet2) -> {
                    IPPacket packet = new IPPacket();
                    packet.time = packet1.time;
                    packet.ipSet.addAll(packet1.ipSet);
                    packet.ipSet.addAll(packet2.ipSet);
                    packet.totalSize = packet1.totalSize + packet2.totalSize;
                    return packet;
                }
        );

        outputDStream.foreachRDD(rdd -> {
            if(!rdd.isEmpty()){
                JavaRDD tempRdd = rdd.map(x -> {
                    String json = objectWriter.writeValueAsString(x._2);
                    socketConfig.sendData(json);
                    return x._2;
                });
                tempRdd.count();
            }
        });

        outputDStream.print();

        jssc.start();
        jssc.awaitTermination();

    }

    private static IPPacket[] parseLogLine(String line) {
        if(!line.isEmpty()) {
            IPPacket[] packets = new IPPacket[1];
            IPPacket ipPacket = new IPPacket();
            String[] logLineArr = line.split("\\s+");
            ipPacket.time = Long.parseLong((logLineArr[0].split("\\."))[0]);
            ipPacket.totalSize = Integer.parseInt(logLineArr[4]);
            ipPacket.ipSet.add(logLineArr[2]);
            packets[0] = ipPacket;
            return packets;
        }
        return null;
    }
}
