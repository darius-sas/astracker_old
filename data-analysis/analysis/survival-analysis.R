library(survival)
library(survminer)
library(dplyr)
library(tidyr)
library(purrr)

computeSurvivalAnalysis<- function(df, strata = "smellType", uniform.sampling = F){
  if (uniform.sampling) {
    df <- uniform_distr(df, by = strata)
  }
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
  
  df.dup <- df[!duplicated(df[, c("project", "uniqueSmellID")]), cbind(c("project", "uniqueSmellID", "age", "versionPosition"), strata)]
  df.surv <- left_join(df.smel, df.dup, by=c("project", "uniqueSmellID"))
  
  # We need to negate presentInLastVersion because event is whether the smell is 'dead' in the last version
  #surv <- Surv(time = age, event = !presentInLastVersion)
  model.formula <- as.formula(paste("Surv(time = age, event = !presentInLastVersion) ~", strata, "+ project"))
  fitmodel = survfit(model.formula, data = df.surv)
  fitmodel$call$formula <- model.formula
  
  return(list(model=fitmodel, data = df.surv))
}


uniform_distr <- function(df, by="smellType"){
  by <- sym(by)
  df.ret <- data.frame()
  for (proj in unique(df$project)) {
    df.samp <- df %>% filter(project == proj) %>% group_by(!!by)
    sample.size <- min((df.samp %>% tally())$n)
    df.samp <- df.samp %>% 
      nest() %>% 
      mutate(n = replicate(nrow(.), sample.size)) %>%
      mutate(samp = map2(data, n, sample_n)) %>%
      select(!!by, samp) %>%
      unnest()
    df.samp$project<-proj
    df.ret <- bind_rows(df.ret, df.samp)
  }
  return(df.ret)
}