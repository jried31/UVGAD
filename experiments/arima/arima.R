#ARIMA R Tutorial
# Using Beer Data
# http://robjhyndman.com/tsdldata/data/beer.dat
files<-c(
  '~/Desktop/experiments/arima/beer.dat')
beer1 <-read.csv(files[1],header=TRUE) #read.csv(file.choose(),header=TRUE)

#plot beer data
plot(beer1$Production,type="l")

plot(1:ndifbeer1,xlab="Differenced Value",ylab="Varience of Error",dbeer1,type="l")
plot(1:ndifbeer1,logdbeer1,xlab="Log Differenced Value",ylab="Variance of Error",type="l")
#plot the Autocorrelation funciton and Partial ACF on graph
par(mfrow=c(2,1))
acf(beer1$Production)
pacf(beer1$Production)

#Takes the difference between difference between
#each n - (n-1) point
dbeer1<-diff(beer1$Production)
logdbeer1<-log(dbeer1)
logdbeer1[is.nan(logdbeer1)]<-0#replace NaN with 0 by limit rule
ndifbeer1 <-length(dbeer1)

par(mfrow=c(2,1))

#Plot the Autocorrelation funciton and Partial ACF 
#of the differenced data
par(mfrow=c(2,1))
acf(dbeer1)
pacf(dbeer1)

#considering we see a seasonal trend, we fit the model based upon our findings
beer1.fit<-arima(beer1$Production,#1. We use the original data because ARIMA will difference the data automatically
            order=c(1,1,0),#2. Standard P order on the AR, D is the # of differnces, Q is order of ma term
            seasonal=list(order=c(1,1,0), period=12,include.mean=FALSE))##3. order of component, and the period (which differneces it by 12)
#NOTE:  Putting include.MEAN=false tells R not to assume that the next trend should continue on as if it were in past (ie: positive autocorrelation) 
beer1.fit
#-----------Forward Prediction---------
#Generate the predictions
beer1.pred<-predict(beer1.fit,n.ahead=12)
plot(beer1,type="l",xlim=c(400,488),ylim=c(50,250))
lines(beer1.pred$pred,col="blue")
lines(beer1.pred$pred+2*beer1.pred$se,col="red")
lines(beer1.pred$pred-2*beer1.pred$se,col="red")

