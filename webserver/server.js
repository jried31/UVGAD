var http = require("http");
var url = require("url");

function start(route, handle) {
  function onRequest(request, response) {
    var pathname = url.parse(request.url).pathname;
    console.log("Request for " + pathname + " received.");
    
    var postData = "";
    request.addListener('data', function(postDataChunk) {
      postData += postDataChunk;
    });
    
    request.addListener('end', function() {
      route(handle, pathname, response, postData);
    });
  }
  
  http.createServer(onRequest).listen(8080);
  console.log("Server has started.");
}

exports.start = start;
