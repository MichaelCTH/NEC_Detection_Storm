package Bolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class HashTagCounter extends BaseRichBolt{
    private OutputCollector collector;
    private HashMap<String, Long> counts = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.counts = new HashMap<String, Long>();
    }

    @Override
    public void execute(Tuple input) {
        String hashtage = input.getStringByField("hashtag");
        Long count = this.counts.get(hashtage);
        if (count == null) {
            count = 0L;
        }
        count++;
        this.counts.put(hashtage, count);
        this.collector.emit(new Values(hashtage,count));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag","count"));
    }
}
