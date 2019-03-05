library(dplyr)

#' Computes the correlation between signal classification and smell's age
#' @param df.sig A classification data frame (computed by classifySignal())
computeCorrelMatrix <- function(df.sig){
  targetCols = c("age", "classification")
  df.icc <- df.sig[, targetCols]
  df.icc$classification <- as.numeric(df.sig$classification)
  icc <- as.data.frame(psych::ICC(df.icc)$results)
  return(icc)
}


#' Computes a correlation matrix using given the smell-generic characteristics.
#' @param df the dataframe to use
#' @param characteristics the characteristics for the correlation analysis
#' @param minAge the minimun age to use for the analysis
#' @param method the correlation method name to pass to corr.test
computeCharacteristicCorrelation <- function(df, characteristics = c("size", "pageRankMax", "pageRankAvrg", "overlapRatio"), minAge = 3, method = "pearson"){
  df.corr.all <- data.frame()
  for (v1 in characteristics) {
    for (v2 in characteristics) {
      if (v1 != v2) {
        df.corr <- df %>% group_by(uniqueSmellID, project) %>%
          filter(sd(!!sym(v1)) > 0 & sd(!!sym(v2)) > 0 & age >= minAge) %>% 
          summarise(rho = cor.test(!!sym(v1), !!sym(v2), exact = T, method = method)$estimate, 
                    rho.p.value = cor.test(!!sym(v1), !!sym(v2), exact = T, method = method)$p.value)
        df.corr$var1<- v1
        df.corr$var2<- v2
        df.corr.all <- bind_rows(df.corr.all, df.corr)
      }
    }
  }
  df.corr.all <- inner_join(df.corr.all, df[!duplicated(df[,c("uniqueSmellID", "project")]), 
                                                   c("uniqueSmellID", "project", "smellType")], 
                                   by = c("uniqueSmellID", "project"))
  return(df.corr.all)
  #df.corr.validity <- df.corr %>% mutate(isValid = p.value >= 0.05) %>% group_by(isValid) %>% tally()
  #ggplot(df.corr.validity, aes(isValid, n)) + geom_bar(aes(color = isValid, fill=isValid), stat = "identity")
}

