

import Bolt.HashTagCounter;
import Bolt.HashTagExtractor;
import Bolt.RedisBolt;
import Spout.LiveTwitterSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.tuple.Fields;

import org.apache.storm.utils.Utils;

public class NEC_Detection_Topology {
    private static final String Twitter_SPOUT_ID = "twitter-spout";
    private static final String HashTagExtractor_BOLT_ID = "extractor-bolt";
    private static final String HashTagCounter_BOLT_ID = "count-bolt";
    private static final String Redis_BOLT_ID = "redis-bolt";
    private static final String TOPOLOGY_NAME = "twitter-TrendingTag-topology";


    public static void main(String[] args) throws Exception {
        LiveTwitterSpout spout = new LiveTwitterSpout();
        HashTagExtractor extractor = new HashTagExtractor();
        HashTagCounter counter = new HashTagCounter();
        RedisBolt redis = new RedisBolt();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(Twitter_SPOUT_ID, spout,1);
        builder.setBolt(HashTagExtractor_BOLT_ID, extractor,2).setNumTasks(4).shuffleGrouping(Twitter_SPOUT_ID);
        builder.setBolt(HashTagCounter_BOLT_ID, counter,2).fieldsGrouping( HashTagExtractor_BOLT_ID, new Fields("hashtag"));
        builder.setBolt(Redis_BOLT_ID, redis).globalGrouping(HashTagCounter_BOLT_ID);

        Config config = new Config();
        LocalCluster cluster = new LocalCluster();

        cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

        //Utils.sleep(10000);
        //cluster.killTopology(TOPOLOGY_NAME);
        //cluster.shutdown();
    }
}
