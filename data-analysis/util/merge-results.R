library(methods) # avoid silly warnings from Rscript
library(dplyr)

args <- commandArgs(TRUE)

if (length(args) < 2) {
  write("Usage: Rscript merge-results.r <projects-dir> <output-dir> [--nonConsec]", stdout())
  quit()
}

merge.results <- function(dir, pattern){
  projectDirs <- list.files(dir, recursive = T, pattern = pattern)
  
  df <- data.frame()
  
  for (entry in projectDirs) {
    project <- strsplit(entry, "/")[[1]][1]
    df.pr <- read.table(paste(dir, entry, sep = "/"), header = T, sep = ",")
    df.pr$project <- project
    
    df <- rbind(df, df.pr)
  }
  df$project <- as.factor(df$project)
  df <- df %>% select(project, everything())
  return(df)
}

consecOnly = TRUE
if (length(args) == 3 & !is.null(args[3])) {
  consecOnly = FALSE
}

results.dir <- args[1]
output.dir <- args[2]
pattern <- ifelse(consecOnly,"-consecOnly.csv", "-nonConsec.csv")

# Create paths to output files
sc.dataset = file.path(output.dir, "smells.csv")
ps.dataset = file.path(output.dir, "projects.csv")
cc.dataset = file.path(output.dir, "components.csv")
af.dataset = file.path(output.dir, "affected.csv")

# Invoke merging and do postprocessing
df <- merge.results(results.dir, paste("*smell-characteristics", pattern, sep=""))
df.projects <- merge.results(results.dir, paste("*project-sizes", pattern, sep=""))
df <- left_join(df, df.projects, by = c("project", "version"))
df <- df %>% mutate(pageRankWeighted = ifelse(affectedComponentType=="package", 
                                              pageRankMax * nPackages, 
                                              pageRankMax * nClasses))
df.components <- merge.results(results.dir, paste("*components-characteristics", pattern, sep = ""))
df.affected <- merge.results(results.dir, paste("*affected-components", pattern, sep = ""))

# Write the datasets to file
write.csv(df, file = sc.dataset, row.names = F)
write.csv(df.projects, file = ps.dataset, row.names = F)
write.csv(df.components, file = cc.dataset, row.names = F)
write.csv(df.affected, file = af.dataset, row.names = F)