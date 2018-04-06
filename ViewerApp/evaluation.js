const http = require('http');
var redis = require("redis");

const hostname = '127.0.0.1';
const port = 8181;
var client = redis.createClient("6379",hostname, null);

const server = http.createServer((req, res) => {
  res.statusCode = 200;
  res.setHeader('Content-Type', 'text/plain');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type');
  res.setHeader('Access-Control-Allow-Credentials', true);
  
  client.hgetall("tweet_process_time", function (err, object) {
	    var sum = 0;
	    var counter = 0;
		try {
		    var items = Object.keys(object).map(function(key) {
		    return [key, object[key]];
		    });
		      
		    for (var i = 0; i < items.length; i++){ 
		        sum += parseFloat(items[i][1]);
		        counter += 1;
		    }
		}catch(err){
			console.log(err);
		}
	    res.write((sum/counter).toString());
	  	res.end();
  });

	
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});