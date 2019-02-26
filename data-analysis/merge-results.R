library(methods) # avoid silly warnings from Rscript

args <- commandArgs(TRUE)

if (length(args) < 2) {
  write("Usage: Rscript merge-results.r <projects-dir> <output-file-name> [--nonConsec]", stdout())
  quit()
}

merge.dataset <- function(dir, consecOnly = T){
  pattern <- if(consecOnly){"*smell-characteristics-consecOnly.csv"}else{"*smell-characteristics-nonConsec.csv"} 
  projectDirs <- list.files(dir, recursive = T, pattern = pattern)
  
  df <- data.frame()
  
  for (entry in projectDirs) {
    project <- strsplit(entry, "/")[[1]][1]
    df.pr <- read.table(paste(dir, entry, sep = "/"), header = T, sep = ",")
    df.pr$project <- project
    
    df <- rbind(df, df.pr)
  }
  df$project <- as.factor(df$project)
  return(df)
}

consecOnly = TRUE
if (length(args) == 3 & !is.null(args[3])) {
  consecOnly = FALSE
}

write.csv(merge.dataset(args[1]), file = args[2], row.names = F)

