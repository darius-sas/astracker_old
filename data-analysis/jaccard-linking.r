library(methods) # avoid silly warnings from Rscript

args <- commandArgs(TRUE)

if (length(args) < 2) {
  write("Usage: Rscript jaccard-linking.r <similarity-score-file.csv> <output-file.ext>", stdout())
  quit()
}

draw_similarity_score_plots <- function(data_file, base_size = 12, print = F){
  library(ggplot2)
  library(stringr)
  library(gridExtra)
  
  df <- read.csv(data_file)
  df$currentLabel <- paste(as.character(df$currentVersion), as.character(df$curId), sep = '/' )
  df$nextLabel <- paste(as.character(df$nextVersion), as.character(df$nextId), sep = '/')
  
  plots <- c()
  
  #versions = sort(unique(c(levels(df$currentVersion), levels(df$nextVersion))));
  i = 1
  for (current_version in levels(df$currentVersion)) {
  
    df.curr <- df[df$currentVersion == current_version,]
    
    title = paste("Tracking of smells from version", current_version)
    
    # There is a diagonal of because the plotting function orders the data and arcan 
    # detects the smells in a similar order from version to version
    p <- ggplot(df.curr, aes(currentLabel, nextLabel)) +
      geom_tile(aes(fill = similarityScore), colour = "gray10") + 
      scale_fill_gradient(low = "white", high = "firebrick") +
      theme_gray(base_size = base_size) +
      labs(x = "This version smells", y = "Linked smells", title = title) +
      scale_x_discrete() + scale_y_discrete() +
      theme(#axis.ticks = element_blank(),
            axis.text.x = element_text(size = base_size *.7, angle = 90),
            axis.text.y = element_text(size = base_size *.7)) +
      geom_point(data = subset(df.curr, matched == "true"), colour="cyan3", shape=16, size=2, na.rm = T)
    if (print) {
      print(p)
    }
    plots[[i]] <- p
    i = i + 1
  }
  return(list(plots=plots, df=df))
}

# TODO complete plotting, check version 3.0 antlr what's happening

fin <- args[1]
fout <- args[2]
#fin <- "/home/p284098/git/trackas/test-data/output/trackASOutput/antlr/similarity-scores-nonConsec.csv"
plots <- draw_similarity_score_plots(fin, base_size = 16)$plots
pdf(fout, width = 20, height = 15)
invisible(lapply(plots, print))
dev.off()
# https://stackoverflow.com/questions/20500706/saving-multiple-ggplots-from-ls-into-one-and-separate-files-in-r
#do.call(grid.arrange, args=c(plots, ncol=1))


