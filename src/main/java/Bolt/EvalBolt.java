package Bolt;


import java.util.*;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;

/*
* EvaBolt
* Purpose: Get tuple to calculate the time different and store in redis
*/

public class EvalBolt extends BaseRichBolt{
    private Jedis jedis = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.jedis = new Jedis("redis",6379);
        jedis.flushAll();
    }

    public void execute(Tuple input) {
        Long ori_timestamp = (long)input.getValue(2);
        Long now = System.currentTimeMillis();
        jedis.hset("tweet_process_time",now.toString(),String.valueOf(now-ori_timestamp));

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // No output
    }
}
