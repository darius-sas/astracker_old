library(dtw)
library(dplyr)

tConstantA <- function(high, medium, low) c(high, high)
tIncreaseB <- function(high, medium, low) c(low, medium, high)
tIncreaseC <- function(high, medium, low) c(low, low, high, high)
tTempIncrD <- function(high, medium, low) c(low, high, low)
tTempDecrE <- function(high, medium, low) c(high, low, high)
tDecreaseF <- function(high, medium, low) c(high, high, low, low)
tDecreaseG <- function(high, medium, low) c(high, medium, low)


classifySignal <- function(df, colName){
  df.temp <- df %>% select_at(c("uniqueSmellID", "version", colName)) %>% 
    group_by(uniqueSmellID) %>%
    summarise(high = max(!!sym(colName)), low = min(!!sym(colName))) %>%
    mutate(med = (low + high) / 2)
  
  df <- inner_join(df, df.temp, by = "uniqueSmellID")
  
  df.sig <- df %>% mutate_at(colName, funs(scale(.) %>% as.vector)) %>% 
    group_by(uniqueSmellID) %>%
    summarise(dtwA = dtw(!!sym(colName), tConstantA(high, med, low))$normalizedDistance,
              dtwB = dtw(!!sym(colName), tIncreaseB(high, med, low))$normalizedDistance,
              dtwC = dtw(!!sym(colName), tIncreaseC(high, med, low))$normalizedDistance,
              dtwD = dtw(!!sym(colName), tTempIncrD(high, med, low))$normalizedDistance,
              dtwE = dtw(!!sym(colName), tTempDecrE(high, med, low))$normalizedDistance,
              dtwF = dtw(!!sym(colName), tDecreaseF(high, med, low))$normalizedDistance,
              dtwG = dtw(!!sym(colName), tDecreaseG(high, med, low))$normalizedDistance)
  return(df.sig)
}

test <- function(file, project){
  df <- read.csv(file)
  df.p <- df[df$project==project,]
  
  res <- classifySignal(df.p, "size")
  return(res)
}

# TODO: Finish visualization then compare it with a line plot as used for PR

test_plot <- function(file, project, colName, smellID){
  df <- read.csv(file)
  df.p <- df[df$project==project,]
  
  high = max(df[df$uniqueSmellID == smellID, colName])
  low  = min(df[df$uniqueSmellID == smellID, colName])
  med  = (low + high) / 2
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tConstantA(high, med, low), keep = T), main = "Constant A", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tIncreaseB(high, med, low), keep = T), main = "Gradual increase B", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tIncreaseC(high, med, low), keep = T), main = "Sharp increase C", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tTempIncrD(high, med, low), keep = T), main = "Temporary increase D", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tTempDecrE(high, med, low), keep = T), main = "Temporary decrease E", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tDecreaseF(high, med, low), keep = T), main = "Sharp decrease F", type="twoway",offset=-2)
  plot(dtw(df.p[df.p$uniqueSmellID == smellID, colName], tDecreaseG(high, med, low), keep = T), main = "Gradual decrease F", type="twoway",offset=-2)
}

# Normalizes the given vector within 0 and 1.
normalize<-function(y) {
  x<-y[!is.na(y)]
  x<-(x - min(x)) / (max(x) - min(x))
  y[!is.na(y)]<-x
  
  return(y)
}


