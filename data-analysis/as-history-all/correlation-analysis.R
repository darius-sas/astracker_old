
computeCorrelMatrix <- function(df.sig){
  targetCols = c("age", "classification")
  df.icc <- df.sig[, targetCols]
  df.icc$classification <- as.numeric(df.sig$classification)
  icc <- as.data.frame(psych::ICC(df.icc)$results)
  return(icc)
}

