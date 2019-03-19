library(survival)
library(survminer)
library(dplyr)


computeSurvivalAnalysis<- function(df, strata = "smellType"){
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
  
  df.dup <- df[!duplicated(df[, c("project", "uniqueSmellID")]), c("project", "uniqueSmellID", strata, "age", "versionPosition")]
  df.surv <- left_join(df.smel, df.dup, by=c("project", "uniqueSmellID"))
  # We need to negate presentInLastVersion because event is whether the smell is 'dead' in the last version
  #surv <- Surv(time = age, event = !presentInLastVersion)
  model.formula <- as.formula(paste("Surv(time = age, event = !presentInLastVersion) ~", strata, "+ project"))
  fitmodel = survfit(model.formula, data = df.surv)
  fitmodel$call$formula <- model.formula
  
  return(list(model=fitmodel, data = df.surv))
}


computeAgeDensity <- function(df){
  
}