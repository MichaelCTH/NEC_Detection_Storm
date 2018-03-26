package Bolt;

import java.util.Map;

import com.google.common.base.CharMatcher;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import twitter4j.HashtagEntity;
import twitter4j.Status;

public class HashTagExtractor extends BaseRichBolt{
    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector=collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Status status = (Status)tuple.getValue(0);

        for (HashtagEntity hashtag : status.getHashtagEntities()) {
            //Emit each hashtag
            if(CharMatcher.ASCII.matchesAllOf(hashtag.getText())) {
                this.collector.emit(new Values(hashtag.getText()));
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag"));
    }
}
