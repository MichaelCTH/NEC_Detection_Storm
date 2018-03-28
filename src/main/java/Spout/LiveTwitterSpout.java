package Spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import twitter4j.*;

public class LiveTwitterSpout extends BaseRichSpout{
    private SpoutOutputCollector  collector;
    private LinkedBlockingQueue<Status> queue;
    private TwitterStream twitterStream;

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;

        this.twitterStream = new TwitterStreamFactory().getInstance();
        this.queue = new LinkedBlockingQueue<>();
        this.twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                queue.offer(status);
            }
            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {}
            public void onTrackLimitationNotice(int i) { }
            public void onScrubGeo(long l, long l1) { }
            public void onException(Exception e) { }
            public void onStallWarning(StallWarning warning) { }
        });
        this.twitterStream.sample();
    }

    @Override
    public void nextTuple() {
        Status status = queue.poll();
        if (status == null) {
            Utils.sleep(50);
        } else {
            this.collector.emit(new Values(status));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet"));
    }

    @Override
    public void close() {
        this.twitterStream.shutdown();
    }
}
