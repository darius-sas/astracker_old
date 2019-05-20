library(dplyr)
library(gtools)
smellCharacteristics <- data.frame(rbind(c("size", "generic"),
                                        c("overlapRatio", "generic"),
                                        c("pageRankWeighted", "generic"),
                                        c("pageRankMax", "generic"),
                                        c("pageRankAvrg", "generic"),
                                        c("strength", "unstableDep"),
                                        c("instabilityGap", "unstableDep"),
                                        c("avrgEdgeWeight", "cyclicDep"),
                                        c("numOfEdges", "generic"),
                                        c("numOfInheritanceEdges", "cyclicDep")))
colnames(smellCharacteristics) <- c("name", "type")

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
#' @param smellCharcteristicTypes an array of smell characteristic types to use for the correlation analysis.
#'        The expected values are of maximum length of 2, in which case one of the values must be "generic".
#' @param minAge the minimun age to use for the analysis
#' @param method the correlation method name to pass to corr.test
computeCharacteristicCorrelation <- function(df, smellCharcteristicTypes = c("generic"), minAge = 3, method = "pearson"){
  characteristics <- subset(smellCharacteristics$name, smellCharacteristics$type %in% smellCharcteristicTypes)
  characteristics <- combinations(n=length(characteristics), r=2, v=as.character(characteristics))
  df.corr.all <- data.frame()
  df.corr <- df %>% filter(age >= minAge) %>% 
    group_by(project, uniqueSmellID) %>%
    arrange(project, uniqueSmellID, versionPosition)
  for(i in 1:nrow(characteristics)){
    v1 = characteristics[i, 1]
    v2 = characteristics[i, 2]
    df.tmp <- df.corr %>%
      filter(sd(!!sym(v1)) > 0 & sd(!!sym(v2)) > 0) %>%
      arrange(project, uniqueSmellID, versionPosition) %>%
      summarise(estimate = cor.test(!!sym(v1), !!sym(v2), exact = T, method = method)$estimate, 
                p.value  = cor.test(!!sym(v1), !!sym(v2), exact = T, method = method)$p.value)
    df.tmp$var1<- v1
    df.tmp$var2<- v2
    df.corr.all <- bind_rows(df.corr.all, df.tmp)
  }

  df.corr.all <- inner_join(df.corr.all, df[!duplicated(df[,c("uniqueSmellID", "project")]), 
                                                   c("uniqueSmellID", "project", "smellType", "age")], 
                                   by = c("uniqueSmellID", "project"))
  df.corr.all$var <- paste(df.corr.all$var1, "~", df.corr.all$var2, sep = "")
  return(df.corr.all)
}
