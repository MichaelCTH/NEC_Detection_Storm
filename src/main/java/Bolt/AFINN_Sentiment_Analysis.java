package Bolt;

import com.google.common.base.Splitter;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AFINN_Sentiment_Analysis extends BaseRichBolt {

    private OutputCollector collector;
    private HashMap<String, Long> AFINN = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector=collector;
        this.AFINN = new HashMap<>();
        AFINN_Score_Load();
    }

    @Override
    public void execute(Tuple tuple) {
        String tweet = tuple.getValue(0).toString();
        String ori_tweet = tuple.getValue(1).toString();

        tweet = tweet.replaceAll("\\p{Punct}|\\n", " ").toLowerCase();
        final Iterable<String> words = Splitter.on(' ').trimResults().omitEmptyStrings().split(tweet);
        int score = 0;

        for(String word : words){
            if(this.AFINN.containsKey(word))
                score += this.AFINN.get(word);
        }

        if (score <= 0)
            this.collector.emit(new Values(ori_tweet,tuple.getValue(2)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ori_tweet","timestamp"));
    }

    private void AFINN_Score_Load(){
        String fileName = "AFINN-111.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(" ");
                this.AFINN.put(tmp[0], Long.parseLong(tmp[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
