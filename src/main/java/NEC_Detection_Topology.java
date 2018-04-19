
import Bolt.*;

import Spout.LiveTwitterSpout;
import Spout.Twitter_DB_Spout;
import edu.stanford.nlp.parser.metrics.Evalb;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.Nimbus;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class NEC_Detection_Topology {
    private static final String Twitter_SPOUT_ID = "twitter-spout";
    private static final String TweetExtractor_BOLT_ID = "extractor-bolt";
    private static final String Sentiment_Analysis_BOLT_ID = "analyzer-bolt";
    private static final String Tweet_Counter_BOLT_ID = "counter-bolt";
    private static final String Redis_BOLT_ID = "redis-bolt";
    private static final String TOPOLOGY_NAME = "Twitter-SentimentAnalysis-topology";

    private static String Mode = NEC_Mode.LIVE;

    public static void main(String[] args) throws Exception {
        LiveTwitterSpout spout = new LiveTwitterSpout();
        //Twitter_DB_Spout spout = new Twitter_DB_Spout();
        TweetExtractor extractor = new TweetExtractor();
        AFINN_Sentiment_Analysis analyzer = new AFINN_Sentiment_Analysis();
        //StanfordNLP analyzer = new StanfordNLP();
        Tweet_Count counter = new Tweet_Count();
        BaseRichBolt redis;

        if (NEC_Mode.equal(Mode,NEC_Mode.LIVE))
            redis = new DetectionRedisBolt();
        else if (NEC_Mode.equal(Mode,NEC_Mode.LOCAL))
            redis = new Local_Result();
        else
            redis = new EvalBolt();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(Twitter_SPOUT_ID, spout,1);
        builder.setBolt(TweetExtractor_BOLT_ID, extractor,1).shuffleGrouping(Twitter_SPOUT_ID);
        builder.setBolt(Sentiment_Analysis_BOLT_ID,analyzer, 2).shuffleGrouping(TweetExtractor_BOLT_ID);
        builder.setBolt(Tweet_Counter_BOLT_ID, counter,1).fieldsGrouping( Sentiment_Analysis_BOLT_ID, new Fields("ori_tweet"));
        builder.setBolt(Redis_BOLT_ID, redis,1).globalGrouping(Tweet_Counter_BOLT_ID);

        //Submit
        Config config = new Config();
        if(NEC_Mode.equal(Mode,NEC_Mode.LIVE) || NEC_Mode.equal(Mode,NEC_Mode.EVAL)){
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
            //Utils.sleep(300000);
            //Map conf = Utils.readStormConfig();
            //Nimbus.Client client = NimbusClient.getConfiguredClient(conf).getClient();
            //client.deactivate(TOPOLOGY_NAME);
        }else{
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
        }
    }
}
