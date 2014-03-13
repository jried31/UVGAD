files<-c(
  #outdoor artificial photolamp experiments
  '~/Desktop/experiments/2_19_2014/lightClassification1.txt',#1 rotate left, right around gravity, from close
  '~/Desktop/experiments/2_19_2014/lightClassification2.txt', #start close, move far, and back
  '~/Desktop/experiments/2_19_2014/lightClassification3.txt', #still, tilt up, down, back to middle
  '~/Desktop/experiments/2_19_2014/lightClassification4.txt', #straight with up and down motion, backwards, and retur

  '~/Desktop/experiments/2_23_2014/still/lightClassificationbuilfingblocking.txt',#5
  '~/Desktop/experiments/2_23_2014/still/lightClassificationshadeawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/still/lightClassificationshadefacingsun.txt', 
  '~/Desktop/experiments/2_23_2014/still/lightClassificationstillawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/still/lightClassificationstillfacing.txt', #9
  
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationbuildingshadesunfacing.txt',#10
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationbuilfingshadeawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationshadetreeawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationshadetreefacingsun.txt', 
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationtiltawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/tilt/lightClassificationtiltfacing.txt', #15
  
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationshadebuildingawayfacing.txt',#16
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationshadebuildingfacingdun.txt', 
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationshadetreefacingsun.txt', 
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationsunawayfacing.txt', 
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationsunfacing.txt', 
  '~/Desktop/experiments/2_23_2014/shake/lightClassificationtreeshadeawayfacing.txt' #21
  )

data<-read.table(files[16])
#names(data)=c("Timestamp","Reading","Angle")#,"UV")
names(data)=c("Timestamp","Reading","Angle1", "Angle2", "Angle3")#,"UV")

x <- data$Reading
y = x[x > 0]
par(mfrow=c(2,1))
plot(y,type="l")
plot(data$Angle3,type="l")
par(mfrow=c(1,1))
c(min(y),max(y),mean(y),median(y),var(y));



val<-array()
val2<-array()
for(i in 1:(length(data$Reading)-12)){
  val<-c(val,max(data$Reading[i:(i+12)]))
  val2<-c(val2,sqrt(var(data$Reading[i:(i+12)])))
}
val2

max(val[!is.na(val)])
mean(val[!is.na(val)])
min(val[!is.na(val)])


#Plot the Autocorrelation funciton and Partial ACF 
#of the differenced data
par(mfrow=c(2,1))
acf(data$Reading)
pacf(data$Reading)
par(mfrow=c(1,1))


logDiff<-diff(data$Reading,lag=1)
diffLen<-length(logDiff)

#Plot the Autocorrelation funciton and Partial ACF 
#of the differenced data
par(mfrow=c(2,1))
acf(logDiff)
pacf(logDiff)
par(mfrow=c(1,1))



logData<-log(logDiff)
x<-c(1:length(logData))
logData[is.nan(logData)]<-0#replace NaN with 0 by limit rule

plot(1:diffLen,logDiff,xlab="Log Differenced Value",ylab="Auto Correlation Coefficient",type="l",main="Differencing at Lag=5")

# basic straight line of fit
fit <- glm(logDiff ~ x)
co <- coef(fit)
abline(fit, col="blue", lwd=2)

# logarithmic
f <- function(x,a,b) {a * log(x) + b}
fit <- nls(logDiff ~ f(x,a,b), start = c(a=1, b=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="orange", lwd=2) 

# polynomial
f <- function(x,a,b,d) {(a*x^2) + (b*x) + d}
fit <- nls(logDiff ~ f(x,a,b,d), start = c(a=1, b=1, d=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2], d=co[3]), add = TRUE, col="pink", lwd=2) 

#exponential
f <- function(x,a,b) {a * exp(b * x)}
fit <- nls(logDiff ~ f(x,a,b), start = c(a=4, b=2)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="green", lwd=2) 

# legend
legend("topleft",
       legend=c("linear","exponential","logarithmic","polynomial"),
       col=c("blue","green","orange","pink"),
       lwd=2,
)

#Plot the Autocorrelation funciton and Partial ACF 
#of the differenced data
par(mfrow=c(2,1))
acf(logDiff)
pacf(logDiff)
par(mfrow=c(1,1))


par(mfrow=c(2,1))
plot(data$Reading,type="l")
x<-c(1:length(data$Reading))

# basic straight line of fit
fit <- glm(data$Reading ~ x)
co <- coef(fit)
abline(fit, col="blue", lwd=2)

# logarithmic
f <- function(x,a,b) {a * log(x) + b}
fit <- nls(data$Reading ~ f(x,a,b), start = c(a=1, b=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="orange", lwd=2) 

# polynomial
f <- function(x,a,b,d) {(a*x^2) + (b*x) + d}
fit <- nls(data$Reading ~ f(x,a,b,d), start = c(a=1, b=1, d=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2], d=co[3]), add = TRUE, col="pink", lwd=2) 

#exponential
f <- function(x,a,b) {a * exp(b * x)}
fit <- nls(data$Reading ~ f(x,a,b), start = c(a=4, b=2)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="green", lwd=2) 


# legend
legend("topleft",
       legend=c("linear","exponential","logarithmic","polynomial"),
       col=c("blue","green","orange","pink"),
       lwd=2,
)


#####################Plot the Log Data

plot(logData,type="l")

# basic straight line of fit
fit <- glm(logData ~ x)
co <- coef(fit)
abline(fit, col="blue", lwd=2)

# logarithmic
f <- function(x,a,b) {a * log(x) + b}
fit <- nls(logData ~ f(x,a,b), start = c(a=1, b=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="orange", lwd=2) 

# polynomial
f <- function(x,a,b,d) {(a*x^2) + (b*x) + d}
fit <- nls(logData ~ f(x,a,b,d), start = c(a=1, b=1, d=1)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2], d=co[3]), add = TRUE, col="pink", lwd=2) 

#exponential
f <- function(x,a,b) {a * exp(b * x)}
fit <- nls(logData ~ f(x,a,b), start = c(a=4, b=2)) 
co <- coef(fit)
curve(f(x, a=co[1], b=co[2]), add = TRUE, col="green", lwd=2) 


# legend
legend("topleft",
       legend=c("linear","exponential","logarithmic","polynomial"),
       col=c("blue","green","orange","pink"),
       lwd=2,
)



par(mfrow=c(1,1))


par(mfrow=c(2,1))
acf(data$Reading)
pacf(data$Reading)

`<-diff(data$Reading,type="l")
plot(diffTimestamp)
readingIntervalMillisec<-mean(diffTimestamp)/1000000 # 
readingIntervalMillisec
#Looking at the data a time window of 22 readings is sufficient, tehrefore
# if average reading interval is 10msx22 = 221.2542 ms
#plot beer data
plot(data$Reading,type="l")

#plot the Autocorrelation funciton and Partial ACF on graph
par(mfrow=c(2,1))
acf(data$Reading)
pacf(data$Reading)
par(mfrow=c(1,1))
# It seems as though the data tales quickly, therefore is stationary (ie: constant variance, mean, etc)
# So we can fit a model

#considering we see a seasonal trend, we fit the model based upon our findings
data.fit<-arima(data$Reading,#1. We use the original data because ARIMA will difference the data automatically
                order=c(1,1,0),#2. Standard P order on the AR, D is the # of differnces, Q is order of ma term
                seasonal=list(order=c(1,1,0), period=22,include.mean=FALSE))##3. order of component, and the period (which differneces it by 12)
#NOTE:  Putting include.MEAN=false tells R not to assume that the next trend should continue on as if it were in past (ie: positive autocorrelation) 
data.fit