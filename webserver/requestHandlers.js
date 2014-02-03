var querystring = require("querystring");
var Parse = require('parse').Parse;
Parse.initialize("FJh8TX7N2OTxGVF2q48DurWCyDHLrwxnjw6M5Ode", "sdtkeolKRnqKVIm4pKF18gFANJtGqn9vdeG8Iuk7");

var UVData = Parse.Object.extend("UVData");
var query = new Parse.Query(UVData);

var html_body = "Hello World";

function render(response) {
  response.writeHead(200, {"Content-Type" : "text/html"});
  response.write(html_body);
  response.end(); 
}
var set=true;
// Control Variables
var START_HOUR=6;
var END_HOUR=19;
<<<<<<< HEAD

=======
var INITIAL_HOUR=6;
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
function upload(response, postData) {
  console.log("Request handler 'upload' was called.");
  var time = require('time');
  // Create a new Date instance, representing the current instant in time
  var current_date = new time.Date();

  current_date.setTimezone("America/Los_Angeles");
 
  
  //convert GMT to current time
  var current_hour=current_date.getHours();
  console.log(current_date+" "+current_hour);
  
  // check the time whether it is between 6 am to 6 pm
  if(current_hour>END_HOUR)
      set=true;
  if(current_hour>=START_HOUR && current_hour<=END_HOUR ){
    // send postData to parse
    var test = new UVData();
    try {
      var result = JSON.parse(postData);
      var data = parseFloat(result.uv);
<<<<<<< HEAD
      test.set("UID", 1);
      test.set("UV_index", data);
     
      if(current_hour==18 && set){
          var energy=data*25;
          test.set("UV_Accumulative_Energy",energy);
=======
      data=(Math.random()*100)%12;   
      if(current_hour==INITIAL_HOUR && set){
          var energy=data*25;
          test.set("UV_Accumulative_Energy",energy);
          test.set("UID",1);
          test.set("UV_index",data);
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
          console.log("once !! "+ energy);
          set =false;
         
      }else{
          // query constraints        
          console.log("later !!");
<<<<<<< HEAD
          query.descending("createdAt");
          query.limit(10);
          query.equalTo("UID", 1);
          query.find({
              success: function(results) {
              console.log("Successfully retrieved " + results.length + " scores.");
              var currEnergy=data*25+parseFloat(results[0].get('UV_Accumulative_Energy'));
              console.log("result "+currEnergy);
              test.set("UV_Accumulative_Energy",currEnergy);
=======
         
          query.equalTo("UID", 1);
          query.descending('updatedAt');
          query.first({
              success: function(results) {
              console.log("Successfully retrieved " + results.updatedAt+ " "+results.get('UV_Accumulative_Energy')+" ");
              var currEnergy=data*25+parseFloat(results.get('UV_Accumulative_Energy'));
              console.log("result "+currEnergy);
              test.set("UV_Accumulative_Energy",currEnergy);
              test.set("UID",1);
              test.set("UV_index",data);
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
              test.save();
          },
              error: function(error) {
                alert("Error: " + error.code + " " + error.message);
              }
          });
          
      }
      test.save(null, {
        success: function(test) {
          // Execute any logic that should take place after the object is saved.
          test.save();
          console.log(postData);
          render(response);
          
        },
          error: function(test, error) {
          // Execute any logic that should take place if the save fails.
          console.log("Failed: %s", postData);
          response.writeHead(404, {"Content-Type" : "text/html"});
          response.write("data uploading failed!");
          response.end(); 
        }
      }); 
    } catch(e) {
      console.log("Error parsing....");
      console.log(postData);
      console.log(e);
    }
  }
}

<<<<<<< HEAD
exports.upload = upload;
=======
exports.upload = upload;
>>>>>>> 41caeac9d001c06e34ff3e61ed5c48033055001e
