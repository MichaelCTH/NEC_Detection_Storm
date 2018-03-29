
import Bolt.*;

import Spout.LiveTwitterSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.tuple.Fields;

public class NEC_Detection_Topology {
    private static final String Twitter_SPOUT_ID = "twitter-spout";
    private static final String TweetExtractor_BOLT_ID = "extractor-bolt";
    private static final String Sentiment_Analysis_BOLT_ID = "analyzer-bolt";
    private static final String Tweet_Counter_BOLT_ID = "counter-bolt";
    private static final String Redis_BOLT_ID = "redis-bolt";
    private static final String TOPOLOGY_NAME = "Twitter-SentimentAnalysis-topology";


    public static void main(String[] args) throws Exception {
        LiveTwitterSpout spout = new LiveTwitterSpout();
        TweetExtractor extractor = new TweetExtractor();
        //AFINN_Sentiment_Analysis analyzer = new AFINN_Sentiment_Analysis();
        StanfordNLP analyzer = new StanfordNLP();
        Tweet_Count counter = new Tweet_Count();
        RedisBolt redis = new RedisBolt();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(Twitter_SPOUT_ID, spout,1);
        builder.setBolt(TweetExtractor_BOLT_ID, extractor,1).setNumTasks(2).shuffleGrouping(Twitter_SPOUT_ID);
        builder.setBolt(Sentiment_Analysis_BOLT_ID,analyzer, 1).setNumTasks(2).shuffleGrouping(TweetExtractor_BOLT_ID);
        builder.setBolt(Tweet_Counter_BOLT_ID, counter,1).fieldsGrouping( Sentiment_Analysis_BOLT_ID, new Fields("ori_tweet"));
        builder.setBolt(Redis_BOLT_ID, redis,1).globalGrouping(Tweet_Counter_BOLT_ID);

        Config config = new Config();


        //Submit
        //StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

        //or Locally===========================
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

        //Timeout==============================
        //Utils.sleep(10000);
        //cluster.killTopology(TOPOLOGY_NAME);
        //cluster.shutdown();
    }
}
