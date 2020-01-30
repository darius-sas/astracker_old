

computeSmellsGenerationCorrelation <- function(df, df.affected, df.components){
  df.min <- df %>% select(project, versionPosition, uniqueSmellID, smellType)
  df.aff <- left_join(df.affected, df.min, by=c("affectedBy" = "uniqueSmellID", "project"="project"))
  
  df.j.count <- df.aff %>% 
    distinct(project, name, smellType, type) %>%
    group_by(project, smellType, type) %>%
    tally()
  
  df.comp.tot <- df.components %>% 
    filter(componentType %in% c("SystemPackage", "SystemClass")) %>%
    distinct(project, name, type) %>%
    group_by(type) %>%
    tally()
}