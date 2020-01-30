library(dtw)
library(dplyr)

# Signals column names
classifiableSignals <- data.frame(rbind(c("size", "generic"), 
                                     c("overlapRatio", "generic"),
                                     #c("pageRankAvrg", "generic"),
                                     c("pageRankWeighted", "generic"),
                                     #c("pageRankMax", "generic"),
                                     c("strength", "unstableDep"),
                                     c("instabilityGap", "unstableDep"),
                                     c("avrgEdgeWeight", "cyclicDep"),
                                     c("numOfEdges", "generic"),
                                     c("numOfInheritanceEdges", "cyclicDep")))
colnames(classifiableSignals) <- c("signal", "type")
signalNames <- classifiableSignals$signal

# Templates
tConstantA <- function(high, medium, low) c(high, high)
tIncreaseB <- function(high, medium, low) c(low, medium, high)
tIncreaseC <- function(high, medium, low) c(low, low, high, high)
tTempIncrD <- function(high, medium, low) c(low, high, low)
tTempDecrE <- function(high, medium, low) c(high, low, high)
tDecreaseF <- function(high, medium, low) c(high, high, low, low)
tDecreaseG <- function(high, medium, low) c(high, medium, low)

templateLevels = c("A-Constant", "B-Gradual Increase", "C-Sharp Increase", "D-Temporal Increase",
                   "E-Temporal Decrease", "F-Sharp Decrease", "G-Gradual Decrease")

#' Classify the signals of all the smells in the given data frame that are older than 1 version.
#' @param df the dataframe to use
#' @param colName the name of the column to use as a signal
#' @return a dataframe where where for each smell id a classification column is provided.
classifySignal <- function(df, colName){
  df.temp <- df %>% 
    filter(age > 1)  %>% # Filter smells that do not have enough data points
    select_at(c("uniqueSmellID", "project", colName)) %>% 
    group_by(project, uniqueSmellID) %>%
    mutate(scaledCol = scale_this(!!sym(colName)) %>% as.vector) %>% # Scale to save low, high, med
    summarise(high = max(scaledCol), low = min(scaledCol)) %>%
    mutate(med = (low + high) / 2)

  df.temp <- inner_join(df, df.temp, by = c("uniqueSmellID", "project")) %>%
    group_by(project, uniqueSmellID) %>%
    mutate(scaledCol = scale_this(!!sym(colName)) %>% as.vector) # Scale again to save whole scaled col
    
  
  df.sig <- df.temp %>% 
    group_by(project, uniqueSmellID) %>%
    filter(!is.na(scaledCol) & !is.na(med)) %>%
    arrange(project, uniqueSmellID, versionPosition) %>%
    summarise(dtwA = dtw(scaledCol, tConstantA(high, med, low))$normalizedDistance,
              dtwB = dtw(scaledCol, tIncreaseB(high, med, low))$normalizedDistance,
              dtwC = dtw(scaledCol, tIncreaseC(high, med, low))$normalizedDistance,
              dtwD = dtw(scaledCol, tTempIncrD(high, med, low))$normalizedDistance,
              dtwE = dtw(scaledCol, tTempDecrE(high, med, low))$normalizedDistance,
              dtwF = dtw(scaledCol, tDecreaseF(high, med, low))$normalizedDistance,
              dtwG = dtw(scaledCol, tDecreaseG(high, med, low))$normalizedDistance)
  
  offset <- 2 # non-numeric columns: project and uniqueSmellID
  df.sig$min <- apply(df.sig, 1, function(x) which.min(x[(offset + 1):length(x)]) + offset)
  df.sig$classification <- factor(templateLevels[df.sig$min - offset], levels = templateLevels)
  
  df.sig <- inner_join(df.sig, df, by = c("uniqueSmellID", "project"))
  df.sig <- df.sig[!duplicated(df.sig[,c("uniqueSmellID", "project")]), c("uniqueSmellID", "smellType", "affectedComponentType",
                                                    "project", "classification", "age", 
                                                    "dtwA", "dtwB", "dtwC", "dtwD", "dtwE", "dtwF", "dtwG")]
  df.sig$characteristic <- colName
  return(df.sig)
}

#' Scales the given vector with center 0
#' @param y vector to scale
#' @return A scaled vector where the data in y is expressed by its variation from the mean and scaled.
#' If there is no variation (sd(y) = 0), then a 0-vector of the same length as y is returned.
scale_this <- function(y)(y - mean(y)) / sd(y) ^ as.logical(sd(y))



###### FUNCTIONS USED FOR TESTING ######
test <- function(file, project){
  df <- read.csv(file)
  df.p <- df[df$project==project,]
  
  res <- classifySignal(df.p, "size")
  return(res)
}

# TODO: Finish visualization then compare it with a line plot as used for PR

test_plot <- function(df, colName, smellID){
  query = scale_this(df[df$uniqueSmellID == smellID, colName])
  high = max(query)
  low  = min(query)
  med  = (low + high) / 2
  plot(dtw(query, tConstantA(high, med, low), keep = T), main = "Constant A", type="twoway",offset=-2)
  plot(dtw(query, tIncreaseB(high, med, low), keep = T), main = "Gradual increase B", type="twoway",offset=-2)
  plot(dtw(query, tIncreaseC(high, med, low), keep = T), main = "Sharp increase C", type="twoway",offset=-2)
  plot(dtw(query, tTempIncrD(high, med, low), keep = T), main = "Temporary increase D", type="twoway",offset=-2)
  plot(dtw(query, tTempDecrE(high, med, low), keep = T), main = "Temporary decrease E", type="twoway",offset=-2)
  plot(dtw(query, tDecreaseF(high, med, low), keep = T), main = "Sharp decrease F", type="twoway",offset=-2)
  plot(dtw(query, tDecreaseG(high, med, low), keep = T), main = "Gradual decrease G", type="twoway",offset=-2)
}


