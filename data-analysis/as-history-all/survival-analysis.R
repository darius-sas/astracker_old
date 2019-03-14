library(survival)
library(survminer)
library(dplyr)


computeSurvivalAnalysis<- function(df){
  df.proj <- df %>% group_by(project) %>%
    summarise(firstVersion = version[which.min(versionPosition)], 
              lastVersion = version[which.max(versionPosition)],
              n.versions = max(versionPosition),
              n.smells = length(unique(uniqueSmellID)))
  df.smel <- df %>%
    group_by(uniqueSmellID, project) %>%
    summarise(lastVersion = max(versionPosition))
  df.smel <- left_join(df.smel, df.proj[,c("project","n.versions")], by="project")
  df.smel$presentInLastVersion <- df.smel$lastVersion == df.smel$n.versions
  
  df.dup <- df[!duplicated(df[, c("project", "uniqueSmellID")]), c("project", "uniqueSmellID", "smellType", "age", "versionPosition")]
  df.surv <- left_join(df.smel, df.dup, by=c("project", "uniqueSmellID"))
  # We need to negate presentInLastVersion because event is whether the smell is 'dead' in the last version
  fitmodel = survfit(Surv(time = age, event = !presentInLastVersion) ~ smellType + project, data = df.surv)
  
  return(list(model=fitmodel, data = df.surv))
}


computeAgeDensity <- function(df){
  
}