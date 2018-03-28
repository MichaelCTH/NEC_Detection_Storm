package Bolt;

import com.google.common.base.CharMatcher;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import twitter4j.Status;

import java.util.Map;

public class TweetExtractor extends BaseRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector=collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Status status = (Status)tuple.getValue(0);
        String ori_tweet = status.getText();
        if(status.getRetweetedStatus() != null){
            ori_tweet = status.getRetweetedStatus().getText();
        }

        if(CharMatcher.ASCII.matchesAllOf(status.getText())) {
            this.collector.emit(new Values(status.getText(),ori_tweet));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet","ori_tweet"));
    }
}
