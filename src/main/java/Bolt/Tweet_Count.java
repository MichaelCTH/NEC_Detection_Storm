package Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class Tweet_Count extends BaseRichBolt {
    private OutputCollector collector;
    private HashMap<String, Long> counts = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.counts = new HashMap<>();
    }

    @Override
    public void execute(Tuple input) {
        String ori_tweet = input.getStringByField("ori_tweet");
        Long count = this.counts.get(ori_tweet);
        if (count == null) {
            count = 0L;
        }
        count++;
        this.counts.put(ori_tweet, count);
        this.collector.emit(new Values(ori_tweet,count, input.getValue(1)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ori_tweet","count", "timestamp"));
    }
}
