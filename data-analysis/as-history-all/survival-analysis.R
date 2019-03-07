library(survival)
library(survminer)
library(dplyr)


computeSurvivalAnalysis<- function(df){
  df.proj <- df %>% group_by(project) %>% summarise(firstVersion = sort(version)[1], lastVersion = sort(version)[length(version)])
  df.smel <- df %>%
    group_by(uniqueSmellID, project) %>%
    summarise(appearedIn = sort(version)[1], deletedIn = sort(version)[length(version)])
  df.dup <- df[!duplicated(df[, c("project", "uniqueSmellID")]), c("project", "uniqueSmellID", "smellType", "age")]
  df.surv <- left_join(df.smel, df.dup, by=c("project", "uniqueSmellID"))
  df.surv <- df.surv %>% 
    mutate(isInLastVersion = df.proj[df.proj$project==project, "lastVersion"] == as.character(deletedIn))
  
  ggsurv <- ggsurvplot(survfit(Surv(time = age, event = isInLastVersion) ~ smellType + project, data = df.surv), 
                       data = df.surv)
  ggsurv$plot + facet_grid(rows=vars(project))
  
  plots <- list()
  i = 1
  for (project in levels(df.surv$project)) {
    df.tmp <- df.surv[df.surv$project==project,]
    surv_object <- Surv(time = df.tmp$age, event = df.tmp$isInLastVersion)
    fit1 <- survfit(surv_object ~ smellType, data = df.tmp)
    
    plots[[i]] <- ggsurvplot(fit1, data = df.tmp, pval = TRUE, conf.int = TRUE) + facet_wrap(~project)
    i = i + 1
  }
}