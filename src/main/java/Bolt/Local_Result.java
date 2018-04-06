package Bolt;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

/*
* Local Result
* Purpose: Get tuple to store in local memory, then calculate the avg time difference
* and print out with the TOP 10 tweets with the most references.
*/
public class Local_Result extends BaseRichBolt{
    private HashMap<String, Long> counts = null;
    private Hashtable<String,Long> TopN = null;
    private LinkedBlockingQueue<Long> process_time = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.counts = new HashMap<>();
        this.TopN = new Hashtable<>();
        this.process_time = new LinkedBlockingQueue<>();
    }

    public void execute(Tuple input) {
        String hashtag = input.getStringByField("ori_tweet");
        Long newcount = input.getLongByField("count");

        Long ori_timestamp = (long)input.getValue(2);
        Long now = System.currentTimeMillis();
        this.process_time.add(now - ori_timestamp);

        this.counts.put(hashtag, newcount);
        if (TopN(hashtag,newcount)) {
            try {
                Iterator it = TopN.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    System.out.println("Ave(ms): "+AvgDelay()+" | "+pair.getKey().toString()+" | "+ pair.getValue().toString());
                }
                System.out.println("==================================================================");
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // No output
    }

    private boolean TopN(String hashtag, long newcount){
        boolean fr = false;
        if(this.TopN.isEmpty() || this.TopN.size() < 10){
            TopN.put(hashtag,newcount);
            return true;
        }else{
            Iterator it = TopN.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();

                if(Long.parseLong(pair.getValue().toString()) < newcount){
                    it.remove();
                    fr = true;
                    break;
                }
            }

            if(fr) {TopN.put(hashtag,newcount);return true;}
            else{return false;}
        }
    }

    private String AvgDelay(){
        double sum = 0;
        long count = 0;
        Iterator<Long> i = this.process_time.iterator();
        while(i.hasNext()){
            sum += i.next();
            count += 1;
        }
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        return numberFormat.format(sum/count);
    }
}
