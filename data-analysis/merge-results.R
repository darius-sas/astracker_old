library(methods) # avoid silly warnings from Rscript
library(dplyr)

args <- commandArgs(TRUE)

if (length(args) < 2) {
  write("Usage: Rscript merge-results.r <projects-dir> <output-dir> [--nonConsec]", stdout())
  quit()
}

merge.dataset <- function(dir, pattern){
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

pattern <- ifelse(consecOnly,"-consecOnly.csv", "-nonConsec.csv")

sc.dataset = file.path(args[2], "dataset.csv")
ps.dataset = file.path(args[2], "project-sizes.csv")

write.csv(merge.dataset(args[1], paste("*smell-characteristics", pattern, sep="")), file = sc.dataset, row.names = F)
write.csv(merge.dataset(args[1], paste("*project-sizes", pattern, sep="")), file = ps.dataset, row.names = F)
